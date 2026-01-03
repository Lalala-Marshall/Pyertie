package com.marshall.pyerite.data.db

import android.content.Context
import java.io.FileOutputStream

object DatabaseInitializer {

    private const val DB_NAME = "item_db_zh.sqlite"

    fun init(context: Context) {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (dbFile.exists()) return

        dbFile.parentFile?.mkdirs()

        context.assets.open("db/$DB_NAME").use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
