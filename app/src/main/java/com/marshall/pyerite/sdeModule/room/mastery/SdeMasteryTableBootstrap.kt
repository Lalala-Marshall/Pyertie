package com.marshall.pyerite.sdeModule.room.mastery

import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Device-copy only: create empty mastery tables if the installed SDE file
 * does not have them yet, so Room can open. Never patch bundled assets or
 * GitHub SDE releases; when EveSDE later ships these tables, this is a no-op.
 */
internal object SdeMasteryTableBootstrap {
    fun ensurePresent(dbFile: File) {
        val db = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE,
        )
        try {
            db.execSQL(CREATE_CERTIFICATE_SKILLS)
            db.execSQL(CREATE_MASTERIES)
        } finally {
            db.close()
        }
    }

    const val CREATE_CERTIFICATE_SKILLS = """
            CREATE TABLE IF NOT EXISTS certificateSkills (
                certificateID INTEGER NOT NULL,
                skillID INTEGER NOT NULL,
                basic INTEGER NOT NULL DEFAULT 0,
                standard INTEGER NOT NULL DEFAULT 0,
                improved INTEGER NOT NULL DEFAULT 0,
                advanced INTEGER NOT NULL DEFAULT 0,
                elite INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (certificateID, skillID)
            )
            """

    const val CREATE_MASTERIES = """
            CREATE TABLE IF NOT EXISTS masteries (
                typeid INTEGER NOT NULL,
                masteryLevel INTEGER NOT NULL,
                certificateID INTEGER NOT NULL,
                PRIMARY KEY (typeid, masteryLevel, certificateID)
            )
            """
}
