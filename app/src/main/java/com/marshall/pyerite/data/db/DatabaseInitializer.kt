package com.marshall.pyerite.data.db

import android.content.Context
import com.marshall.pyerite.localization.SdeDatabase
import java.io.FileOutputStream

object DatabaseInitializer {

    fun init(context: Context) {
        ensureDatabase(context, SdeDatabase.ZH_FILE_NAME)
        ensureDatabase(context, SdeDatabase.EN_FILE_NAME)
    }

    private fun ensureDatabase(context: Context, dbName: String) {
        val dbFile = context.getDatabasePath(dbName)
        if (dbFile.exists()) return

        dbFile.parentFile?.mkdirs()

        context.assets.open("db/$dbName").use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
