#!/usr/bin/env python3
"""
Download latest EstamelGG/EveSDE_2.0 release (sde.zip + icons.zip), refresh
E:\\Marshall\\eve_sde.

**Phase A (update check only):** Compare remote build metadata (``metadata.json``,
release ``body`` JSON, or tag) to app ``assets/latest.txt``. No SQLite file diff;
no comparing two DBs to decide whether to download.

**Phase B (after a real update):** Unzip upstream DB, then run
``normalize_sqlite_boolean_to_integer`` on ``item_db_zh.sqlite`` (declared
**BOOLEAN** column types -> **INTEGER** for Room affinity). **``--source``** is
always the file from ``sde.zip``; output is a copy with rewritten DDL only where
needed. Then replace debug assets (same filenames; no ``.bak.*``).

Cloud upload: not implemented (placeholder message).

Requires: public GitHub for downloads. Use --yes to allow wiping eve_sde root.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import subprocess
import tempfile
import time
import sys
import urllib.error
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
NORMALIZE_SCRIPT = REPO_ROOT / "tools" / "normalize_sqlite_boolean_to_integer.py"

DEFAULT_EVE_SDE_ROOT = Path(r"E:\Marshall\eve_sde")
GITHUB_API_LATEST = "https://api.github.com/repos/EstamelGG/EveSDE_2.0/releases/latest"
GITHUB_HTML_LATEST = "https://github.com/EstamelGG/EveSDE_2.0/releases/latest"
USER_AGENT = "Pyertie-SDE-Sync/1.0 (github.com/EstamelGG/EveSDE_2.0 consumer)"

ASSET_SDE = "sde.zip"
ASSET_ICONS_PRIMARY = "icons.zip"
ASSET_ICONS_ALT = "icon.zip"
ASSET_METADATA = "metadata.json"

APP_DEBUG_ASSETS_DIR = REPO_ROOT / "app" / "src" / "debug" / "assets"
APP_DEBUG_DB_DIR = APP_DEBUG_ASSETS_DIR / "db"
APP_DEBUG_ICONS_DIR = APP_DEBUG_ASSETS_DIR / "icons"
APP_DB = APP_DEBUG_DB_DIR / "item_db_zh.sqlite"
# Bundled release metadata for future sync compares (JSON, same schema as upstream latest.txt / latest.log).
APP_LATEST = APP_DEBUG_ASSETS_DIR / "latest.txt"
# Older syncs wrote metadata next to the DB; still read for version compare until replaced.
LEGACY_APP_LATEST = APP_DEBUG_DB_DIR / "latest.txt"
APP_ICONS_ZIP = APP_DEBUG_ICONS_DIR / "icons.zip"


def find_remote_db_and_meta(eve: Path) -> tuple[Path, Path]:
    """Paths to item_db_zh.sqlite and build metadata after *sde.zip* is extracted into *eve*."""
    nested_db = eve / "sde" / "db" / "item_db_zh.sqlite"
    flat_db = eve / "db" / "item_db_zh.sqlite"
    if nested_db.is_file():
        meta_root = eve / "sde"
        db_path = nested_db
    elif flat_db.is_file():
        meta_root = eve
        db_path = flat_db
    else:
        names = sorted(p.name for p in eve.iterdir()) if eve.is_dir() else []
        raise SystemExit(
            "Missing item_db_zh.sqlite after unzip. "
            f"Tried {nested_db} and {flat_db}. Top-level in {eve!r}: {names!r}"
        )
    for name in ("latest.txt", "latest.log"):
        p = meta_root / name
        if p.is_file():
            return db_path, p
    raise SystemExit(f"No latest.txt or latest.log under {meta_root} after unzip.")


def _github_auth_headers() -> dict[str, str]:
    h = {
        "User-Agent": USER_AGENT,
        "Accept": "application/vnd.github+json",
    }
    token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    if token:
        h["Authorization"] = f"Bearer {token}"
    return h


def http_json(url: str) -> dict:
    req = urllib.request.Request(url, headers=_github_auth_headers())
    with urllib.request.urlopen(req, timeout=120) as resp:
        return json.loads(resp.read().decode("utf-8"))


def resolve_tag_from_releases_page() -> str:
    """Follow github.com/releases/latest redirect; no REST API (avoids API rate limits)."""
    final: str | None = None
    urllib_err: Exception | None = None
    try:
        req = urllib.request.Request(GITHUB_HTML_LATEST, headers=_github_auth_headers())
        with urllib.request.urlopen(req, timeout=120) as resp:
            final = resp.geturl()
            with open(os.devnull, "wb") as sink:
                shutil.copyfileobj(resp, sink)
    except (urllib.error.URLError, TimeoutError, OSError) as e:
        urllib_err = e
        curl = shutil.which("curl")
        if not curl:
            raise SystemExit(
                f"Could not open {GITHUB_HTML_LATEST!r} ({e!r}); "
                "install curl on PATH or set GITHUB_TOKEN / GH_TOKEN and retry."
            ) from e
        curl_args = [curl]
        if sys.platform == "win32":
            curl_args.append("--ssl-no-revoke")
            curl_args.append("--http1.1")
        with tempfile.NamedTemporaryFile(delete=False) as sink:
            sink_path = sink.name
        try:
            curl_args += [
                "-sL",
                "--retry",
                "5",
                "--retry-all-errors",
                "--retry-delay",
                "2",
                "-o",
                sink_path,
                "-w",
                "%{url_effective}",
                "-A",
                USER_AGENT,
                GITHUB_HTML_LATEST,
            ]
            r = subprocess.run(
                curl_args,
                capture_output=True,
                text=True,
                timeout=120,
                check=False,
            )
        finally:
            try:
                os.unlink(sink_path)
            except OSError:
                pass
        if r.returncode != 0:
            raise SystemExit(
                f"curl failed ({r.returncode}) resolving latest release: {r.stderr.strip() or r.stdout!r}"
            ) from e
        final = (r.stdout or "").strip().splitlines()[-1].strip()
        print(f"[i] Resolved release tag via curl (urllib error: {urllib_err!r})", file=sys.stderr)

    assert final is not None
    marker = "/releases/tag/"
    if marker not in final:
        raise SystemExit(f"Could not parse release tag from redirect URL: {final}")
    tag = final.split(marker, 1)[1].split("?", 1)[0].split("#", 1)[0]
    tag = urllib.parse.unquote(tag)
    if not tag:
        raise SystemExit(f"Empty release tag parsed from URL: {final}")
    return tag


def direct_asset_urls(tag: str) -> tuple[str, str, str]:
    """Build browser_download-style URLs when the GitHub API is unavailable."""
    enc = urllib.parse.quote(tag, safe="")
    base = f"https://github.com/EstamelGG/EveSDE_2.0/releases/download/{enc}"
    sde_url = f"{base}/{ASSET_SDE}"
    primary = f"{base}/{ASSET_ICONS_PRIMARY}"
    alt = f"{base}/{ASSET_ICONS_ALT}"
    metadata_url = f"{base}/{ASSET_METADATA}"
    curl = shutil.which("curl")
    if not curl:
        return sde_url, primary, metadata_url
    curl_args = [curl]
    if sys.platform == "win32":
        curl_args.append("--ssl-no-revoke")
        curl_args.append("--http1.1")
    with tempfile.NamedTemporaryFile(delete=False) as hdr:
        hdr_path = hdr.name
    try:
        curl_args += [
            "-sI",
            "-L",
            "--retry",
            "5",
            "--retry-all-errors",
            "--retry-delay",
            "2",
            "-o",
            hdr_path,
            "-w",
            "%{http_code}",
            primary,
        ]
        r = subprocess.run(
            curl_args,
            capture_output=True,
            text=True,
            timeout=60,
            check=False,
        )
    finally:
        try:
            os.unlink(hdr_path)
        except OSError:
            pass
    code = (r.stdout or "").strip().splitlines()[-1].strip()
    if r.returncode == 0 and code == "404":
        return sde_url, alt, metadata_url
    return sde_url, primary, metadata_url


def _downloaded_zip_ok(dest: Path) -> bool:
    """urllib can finish with a truncated body on flaky TLS; reject bad .zip before unzip."""
    if dest.suffix.lower() != ".zip":
        return True
    return dest.is_file() and zipfile.is_zipfile(dest)


def download_file(url: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    headers = _github_auth_headers()
    req = urllib.request.Request(url, headers=headers)
    urllib_ok = False
    try:
        with urllib.request.urlopen(req, timeout=600) as resp:
            with dest.open("wb") as out:
                shutil.copyfileobj(resp, out)
        urllib_ok = True
    except (urllib.error.URLError, TimeoutError, OSError) as e:
        curl = shutil.which("curl")
        if not curl:
            raise SystemExit(
                f"Download failed ({e!r}) and curl was not found on PATH: {url}"
            ) from e
        print(f"[i] urllib TLS error; downloading via curl -> {dest.name}", file=sys.stderr)

    if urllib_ok and _downloaded_zip_ok(dest):
        return
    if urllib_ok and dest.suffix.lower() == ".zip":
        n = dest.stat().st_size if dest.is_file() else 0
        print(
            f"[i] urllib saved a corrupt or truncated zip ({n} bytes); "
            f"retrying via curl -> {dest.name}",
            file=sys.stderr,
        )
        try:
            dest.unlink()
        except OSError:
            pass

    curl = shutil.which("curl")
    if not curl:
        raise SystemExit(f"curl not on PATH (needed for zip retry or TLS fallback): {url}")

    args: list[str] = [curl]
    if sys.platform == "win32":
        args.append("--ssl-no-revoke")
        args.append("--http1.1")
    args += [
        "--retry",
        "5",
        "--retry-all-errors",
        "--retry-delay",
        "2",
        "--fail",
        "-sL",
        "-o",
        str(dest),
    ]
    token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    if token:
        args += ["-H", f"Authorization: Bearer {token}"]
    args += ["-A", USER_AGENT, url]
    last_rc = -1
    last_r: subprocess.CompletedProcess[str] | None = None
    for attempt in range(6):
        r = subprocess.run(args, timeout=600, check=False, capture_output=True, text=True)
        last_r = r
        last_rc = r.returncode
        if r.returncode == 0 and _downloaded_zip_ok(dest):
            return
        if r.returncode == 0 and dest.suffix.lower() == ".zip":
            n = dest.stat().st_size if dest.is_file() else 0
            print(
                f"[i] curl wrote a corrupt or truncated zip ({n} bytes); "
                f"retrying ({attempt + 1}/6) ...",
                file=sys.stderr,
            )
            try:
                dest.unlink()
            except OSError:
                pass
            time.sleep(3)
            continue
        err = (r.stderr or "").strip()
        if attempt < 5:
            print(
                f"[i] curl download attempt {attempt + 1}/6 failed (exit {r.returncode}); "
                f"retrying in 3s ...{(' ' + err) if err else ''}",
                file=sys.stderr,
            )
            time.sleep(3)
    raise SystemExit(
        f"curl download failed after retries (exit {last_rc}): {url}\n"
        f"{((last_r.stderr or '').strip()) if last_r else ''}"
    )


def wipe_eve_sde_root(root: Path) -> None:
    if not root.exists():
        root.mkdir(parents=True)
        return
    for child in root.iterdir():
        if child.is_dir():
            shutil.rmtree(child)
        else:
            child.unlink()


def pick_asset_urls(release: dict) -> tuple[str, str, str | None]:
    assets = {a["name"]: a["browser_download_url"] for a in release.get("assets", []) if "browser_download_url" in a}
    if ASSET_SDE not in assets:
        raise SystemExit(f"Release missing {ASSET_SDE!r}. Available: {sorted(assets)}")
    icon_url = assets.get(ASSET_ICONS_PRIMARY) or assets.get(ASSET_ICONS_ALT)
    if not icon_url:
        raise SystemExit(
            f"Release missing {ASSET_ICONS_PRIMARY!r} or {ASSET_ICONS_ALT!r}. Available: {sorted(assets)}"
        )
    metadata_url = assets.get(ASSET_METADATA)
    return assets[ASSET_SDE], icon_url, metadata_url


def _extract_json_fence_from_body(body: str) -> dict | None:
    m = re.search(r"```json\s*([\s\S]*?)\s*```", body)
    if not m:
        return None
    try:
        return json.loads(m.group(1).strip())
    except json.JSONDecodeError:
        return None


def _tag_build_number(tag: str) -> str:
    if tag.startswith("sde-build-"):
        return tag.removeprefix("sde-build-").strip()
    return "0"


def flatten_release_meta(raw: dict, published_at: str = "") -> dict:
    """Normalize to version_key fields + optional icon/hash fields for bundled latest.txt."""
    bn_raw = raw.get("build_number")
    try:
        bn_int = int(bn_raw) if bn_raw is not None and str(bn_raw).strip() != "" else 0
    except (TypeError, ValueError):
        bn_int = 0
    bn_str = str(bn_int)
    rd = str(raw.get("release_date") or "").strip()
    ct = str(raw.get("completion_time") or "").strip()
    if not ct and rd:
        ct = rd
    if not rd and published_at:
        rd = published_at.strip()
    out: dict[str, object] = {
        "build_number": bn_str,
        "release_date": rd,
        "completion_time": ct,
    }
    for k in ("icon_version", "icon_sha256", "sde_sha256", "patch_number", "extra_db"):
        if k in raw and raw[k] is not None:
            out[k] = raw[k]
    return out


def metadata_from_release_body(release: dict) -> dict | None:
    """Parse 版本元数据 ```json ... ``` from GitHub release notes (same 构建信息 as the web UI)."""
    body = release.get("body") or ""
    if not isinstance(body, str) or not body.strip():
        return None
    j = _extract_json_fence_from_body(body)
    if not j:
        return None
    pub = str(release.get("published_at") or "")
    return flatten_release_meta(j, pub)


def try_download_json(url: str) -> dict | None:
    """Download a small JSON asset; return None on failure (never raises SystemExit)."""
    d = Path(tempfile.mkdtemp(prefix="sde-meta-"))
    p = d / "tmp.json"
    try:
        try:
            download_file(url, p)
        except SystemExit:
            return None
        try:
            return json.loads(p.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            return None
    finally:
        shutil.rmtree(d, ignore_errors=True)


def remote_meta_for_compare(metadata_url: str | None, release: dict | None, tag: str) -> dict:
    """Build / icon / release fields for comparison **before** downloading ``sde.zip``."""
    if metadata_url:
        raw = try_download_json(metadata_url)
        if raw is not None:
            pub = str((release or {}).get("published_at") or "")
            return flatten_release_meta(raw, pub)
    if release:
        m = metadata_from_release_body(release)
        if m is not None:
            return m
    bn = _tag_build_number(tag)
    try:
        bn_int = int(bn)
    except ValueError:
        bn_int = 0
    return flatten_release_meta(
        {"build_number": bn_int, "release_date": "", "completion_time": ""},
        str((release or {}).get("published_at") or ""),
    )


def merge_meta_for_bundle(primary: dict, from_zip: dict | None) -> dict:
    """Prefer non-empty values from extracted ``latest.log`` / ``latest.txt`` over pre-download meta."""
    out = dict(primary)
    if from_zip:
        for k, v in from_zip.items():
            if v is not None and str(v).strip() != "":
                out[k] = v
    return out


def load_latest(path: Path) -> dict | None:
    if not path.is_file():
        return None
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        raise SystemExit(f"Invalid JSON in {path}: {e}") from e


def version_key(d: dict) -> tuple[int, str, str]:
    try:
        bn = int(str(d.get("build_number") or "0").strip())
    except ValueError:
        bn = 0
    rd = str(d.get("release_date") or "")
    ct = str(d.get("completion_time") or "")
    return (bn, rd, ct)


def remote_is_newer(remote: dict, local: dict | None) -> bool:
    if local is None:
        return True
    return version_key(remote) > version_key(local)


def load_app_release_meta() -> dict | None:
    """Release JSON shipped with the app (assets/latest.txt, or legacy db/latest.txt)."""
    return load_latest(APP_LATEST) or load_latest(LEGACY_APP_LATEST)


def replace_asset(src: Path, dest: Path) -> None:
    """Remove any existing *dest* file, then copy *src* to *dest* (same filename)."""
    dest.parent.mkdir(parents=True, exist_ok=True)
    if dest.is_file():
        dest.unlink()
    shutil.copy2(src, dest)


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument(
        "--eve-sde-root",
        type=Path,
        default=DEFAULT_EVE_SDE_ROOT,
        help="Root folder to wipe, download zips into, and unzip sde.zip (default: E:\\Marshall\\eve_sde)",
    )
    p.add_argument(
        "--yes",
        action="store_true",
        help="Required to confirm wiping --eve-sde-root contents",
    )
    p.add_argument(
        "--force",
        action="store_true",
        help="Skip version compare; always normalize and copy into app assets",
    )
    p.add_argument(
        "--dry-run",
        action="store_true",
        help="Fetch release + compare metadata only (metadata.json / release body vs app); no wipe or large downloads",
    )
    args = p.parse_args()

    eve = args.eve_sde_root.resolve()

    if not args.dry_run and not args.yes:
        print("Refusing to modify disk without --yes (wipes eve_sde root per run).", file=sys.stderr)
        print("Re-run with:  python tools/sync_sde_release.py --yes", file=sys.stderr)
        return 2

    release: dict | None = None
    tag: str = "?"
    release_title = ""
    sde_url: str | None = None
    icons_url: str | None = None

    try:
        release = http_json(GITHUB_API_LATEST)
    except urllib.error.HTTPError as e:
        if e.code in (403, 429):
            print(
                f"[i] GitHub API HTTP {e.code} (rate limit). "
                f"Resolving tag via {GITHUB_HTML_LATEST!r} instead.",
                file=sys.stderr,
            )
        else:
            print(f"Failed to fetch GitHub latest release: HTTP {e.code}: {e}", file=sys.stderr)
            return 1
    except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as e:
        print(
            f"[i] GitHub API unreachable ({e!r}). "
            f"Resolving tag via {GITHUB_HTML_LATEST!r} instead.",
            file=sys.stderr,
        )

    if release is not None:
        tag = str(release.get("tag_name", "?"))
        release_title = str(release.get("name", "") or "")
        sde_url, icons_url, metadata_url = pick_asset_urls(release)
    else:
        try:
            tag = resolve_tag_from_releases_page()
            sde_url, icons_url, metadata_url = direct_asset_urls(tag)
        except SystemExit:
            raise
        except (urllib.error.URLError, TimeoutError, OSError) as e2:
            print(f"Failed to resolve latest release without API: {e2}", file=sys.stderr)
            print("Optional: set GITHUB_TOKEN or GH_TOKEN for higher GitHub API limits.", file=sys.stderr)
            return 1

    assert sde_url is not None and icons_url is not None

    print(f"[+] Latest release: {tag} ({release_title})")

    remote_cmp = remote_meta_for_compare(metadata_url, release, tag)
    app_meta = load_app_release_meta()
    print(
        "[+] Upstream (compare): "
        f"build_number={remote_cmp.get('build_number')!r} "
        f"icon_version={remote_cmp.get('icon_version', 'n/a')!r} "
        f"release_date={remote_cmp.get('release_date', '')!r}"
    )

    if args.dry_run:
        print("[dry-run] Would wipe (if update):", eve)
        print("[dry-run] Would download:", ASSET_SDE, "and icons zip (only if update)")
        print("[dry-run] remote (compare):", remote_cmp)
        print("[dry-run] app (bundled):  ", app_meta)
        print("[dry-run] would_update:   ", args.force or remote_is_newer(remote_cmp, app_meta))
        return 0

    if not args.force and not remote_is_newer(remote_cmp, app_meta):
        print("[=] App bundle is already up to date (vs GitHub metadata). No download, no asset changes.")
        print("    Remote:", version_key(remote_cmp), "App:", version_key(app_meta or {}))
        print("[i] Cloud upload skipped (not configured).")
        return 0

    print("[+] Remote newer than bundled app (or --force); downloading SDE and icons ...")
    print(f"[+] Wiping {eve} ...")
    wipe_eve_sde_root(eve)

    sde_zip = eve / ASSET_SDE
    icons_zip = eve / ASSET_ICONS_PRIMARY
    print(f"[+] Downloading {ASSET_SDE} ...")
    download_file(sde_url, sde_zip)
    print(f"[+] Downloading icons ...")
    download_file(icons_url, icons_zip)

    print(f"[+] Extracting {ASSET_SDE} ...")
    with zipfile.ZipFile(sde_zip) as zf:
        zf.extractall(eve)

    remote_db, remote_latest = find_remote_db_and_meta(eve)

    remote_meta = load_latest(remote_latest)
    if not remote_meta:
        raise SystemExit(f"Empty or invalid: {remote_latest}")

    print("[+] Newer SDE detected; normalizing BOOLEAN -> INTEGER for Room ...")
    APP_DEBUG_DB_DIR.mkdir(parents=True, exist_ok=True)
    normalized = remote_db.with_name("item_db_zh.normalized.sqlite")
    cmd = [
        sys.executable,
        str(NORMALIZE_SCRIPT),
        "--source",
        str(remote_db),
        "--output",
        str(normalized),
    ]
    print("Running:", " ".join(cmd))
    r = subprocess.run(cmd, cwd=str(REPO_ROOT))
    if r.returncode != 0:
        return r.returncode

    replace_asset(normalized, APP_DB)
    print(f"[+] Replaced {APP_DB}")

    APP_DEBUG_ASSETS_DIR.mkdir(parents=True, exist_ok=True)
    if LEGACY_APP_LATEST.is_file():
        LEGACY_APP_LATEST.unlink()
    merged = merge_meta_for_bundle(remote_cmp, remote_meta)
    APP_LATEST.write_text(json.dumps(merged, indent=2) + "\n", encoding="utf-8")
    print(f"[+] Wrote {APP_LATEST}")

    replace_asset(icons_zip, APP_ICONS_ZIP)
    print(f"[+] Replaced {APP_ICONS_ZIP}")

    print("[i] Cloud upload skipped (not configured).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
