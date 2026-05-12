#!/usr/bin/env python3
"""
Rewrite user tables in a SQLite file so declared column types BOOLEAN -> INTEGER.

Room maps Kotlin Boolean? to INTEGER affinity; Eve SDE exports often declare BOOLEAN.
This tool detects BOOLEAN via PRAGMA table_info, rebuilds only those tables using a
CREATE TABLE derived from sqlite_master (with BOOLEAN replaced by INTEGER), copies
rows, then re-applies non-auto indexes from the pre-rebuild catalog.

Does not copy into Android assets; sync_sde_release.py passes --output next to the
extracted upstream DB.
"""

from __future__ import annotations

import argparse
import re
import shutil
import sqlite3
import sys
from pathlib import Path

_BOOLEAN_TYPE = re.compile(r"\bBOOLEAN\b", re.IGNORECASE)


def user_tables(con: sqlite3.Connection) -> list[str]:
    cur = con.execute(
        "SELECT name FROM sqlite_master WHERE type='table' "
        "AND name NOT LIKE 'sqlite_%' ORDER BY name"
    )
    return [r[0] for r in cur.fetchall()]


def table_has_boolean_column(con: sqlite3.Connection, table: str) -> bool:
    for row in con.execute(f'PRAGMA table_info("{table}")'):
        declared = (row[2] or "").strip().upper()
        if declared == "BOOLEAN":
            return True
    return False


def create_sql(con: sqlite3.Connection, table: str) -> str | None:
    row = con.execute(
        "SELECT sql FROM sqlite_master WHERE type='table' AND name=?",
        (table,),
    ).fetchone()
    return row[0] if row else None


def index_sqls(con: sqlite3.Connection, table: str) -> list[str]:
    out: list[str] = []
    for name, sql in con.execute(
        "SELECT name, sql FROM sqlite_master WHERE type='index' AND tbl_name=?",
        (table,),
    ):
        if not sql:
            continue
        if name.startswith("sqlite_autoindex"):
            continue
        out.append(sql)
    return out


def column_names(con: sqlite3.Connection, table: str) -> list[str]:
    return [r[1] for r in con.execute(f'PRAGMA table_info("{table}")')]


def boolean_ddl_to_integer(sql: str) -> str:
    return _BOOLEAN_TYPE.sub("INTEGER", sql)


def rebuild_table_for_integer_types(
    con: sqlite3.Connection, table: str, bak_suffix: str
) -> bool:
    """Return True if a rebuild was performed."""
    ddl = create_sql(con, table)
    if not ddl:
        raise RuntimeError(f"No CREATE SQL in sqlite_master for table {table!r}")

    new_ddl = boolean_ddl_to_integer(ddl)
    if new_ddl == ddl:
        return False

    cols = column_names(con, table)
    idx_before = index_sqls(con, table)
    bak = f"{table}{bak_suffix}"

    con.execute(f'ALTER TABLE "{table}" RENAME TO "{bak}"')
    try:
        con.execute(new_ddl)
        col_list = ", ".join(f'"{c}"' for c in cols)
        con.execute(f'INSERT INTO "{table}" ({col_list}) SELECT {col_list} FROM "{bak}"')
        for idx_sql in idx_before:
            con.execute(idx_sql)
        con.execute(f'DROP TABLE "{bak}"')
    except Exception:
        try:
            con.execute(f'ALTER TABLE "{bak}" RENAME TO "{table}"')
        except sqlite3.Error:
            pass
        raise
    return True


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument(
        "--source",
        type=Path,
        required=True,
        help="Upstream SQLite (e.g. item_db_zh.sqlite from sde.zip)",
    )
    p.add_argument(
        "--output",
        type=Path,
        required=True,
        help="Output path (must differ from source unless --in-place)",
    )
    p.add_argument(
        "--in-place",
        action="store_true",
        help="Overwrite --source (copies to a temp file first).",
    )
    args = p.parse_args()

    src_path = args.source.resolve()
    if not src_path.is_file():
        print(f"Missing source: {src_path}", file=sys.stderr)
        return 1

    tmp_path: Path | None = None
    if args.in_place:
        out_path = src_path
        tmp_path = src_path.with_suffix(src_path.suffix + ".tmp_bool_norm")
        shutil.copy2(src_path, tmp_path)
        work_uri = f"file:{tmp_path}?mode=rwc"
    else:
        out_path = args.output.resolve()
        if out_path == src_path:
            print("Refusing to write output on top of source without --in-place.", file=sys.stderr)
            return 1
        shutil.copy2(src_path, out_path)
        work_uri = f"file:{out_path}?mode=rwc"

    con = sqlite3.connect(work_uri, uri=True)
    inplace_swap_after_close: Path | None = None
    try:
        to_rebuild = [t for t in user_tables(con) if table_has_boolean_column(con, t)]
        if not to_rebuild:
            print("No BOOLEAN column types found; output is a copy of source.")
            if tmp_path is not None:
                tmp_path.unlink(missing_ok=True)
                print(f"Done (unchanged types). Left in place: {src_path}")
            else:
                print(f"Done (unchanged types). Wrote: {out_path}")
            return 0

        print("Tables with BOOLEAN columns to rebuild:", ", ".join(sorted(to_rebuild)))
        bak_suffix = "__bool_norm_bak"
        con.execute("PRAGMA foreign_keys=OFF")
        any_done = False
        for t in sorted(to_rebuild):
            print(f"  rebuilding {t} (BOOLEAN -> INTEGER)...")
            if rebuild_table_for_integer_types(con, t, bak_suffix):
                any_done = True
        if not any_done:
            print("No DDL changes applied (unexpected).", file=sys.stderr)
            if tmp_path is not None:
                tmp_path.unlink(missing_ok=True)
            return 1
        con.commit()

        prev_iso = con.isolation_level
        con.isolation_level = None
        try:
            con.execute("VACUUM")
        except sqlite3.Error as exc:
            print(f"[w] VACUUM failed ({exc}); leaving committed database as-is.", file=sys.stderr)
        finally:
            con.isolation_level = prev_iso

        if tmp_path is not None:
            inplace_swap_after_close = tmp_path
    except Exception:
        if tmp_path is not None and inplace_swap_after_close is None:
            tmp_path.unlink(missing_ok=True)
        raise
    finally:
        con.close()

    if inplace_swap_after_close is not None:
        inplace_swap_after_close.replace(src_path)
        print(f"Done. Updated in place: {src_path}")
    else:
        print(f"Done. Wrote: {out_path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
