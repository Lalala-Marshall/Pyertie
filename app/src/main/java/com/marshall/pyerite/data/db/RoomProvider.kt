package com.marshall.pyerite.data.db

import android.content.Context
import androidx.room.Room
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.SdeDatabase

class RoomProvider(
    private val context: Context,
    private val localeController: LocaleController,
) {

    @Volatile
    private var cachedDbName: String? = null

    @Volatile
    private var cachedDatabase: AppDatabase? = null

    @Synchronized
    fun getDatabase(): AppDatabase {
        val dbName = resolveDatabaseFileName()
        val existing = cachedDatabase
        if (existing != null && cachedDbName == dbName) return existing

        val dbFile = context.getDatabasePath(dbName)
        val database = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, dbName)
            .createFromFile(dbFile)
            .build()
        cachedDbName = dbName
        cachedDatabase = database
        return database
    }

    private fun resolveDatabaseFileName(): String {
        val preferred = localeController.resolveDatabaseFileName()
        if (context.getDatabasePath(preferred).exists()) return preferred
        return SdeDatabase.ZH_FILE_NAME
    }
}
