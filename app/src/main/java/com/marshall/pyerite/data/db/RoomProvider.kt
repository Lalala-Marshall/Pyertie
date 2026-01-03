package com.marshall.pyerite.data.db

import android.content.Context
import androidx.room.Room

class RoomProvider(private val context: Context) {

    companion object {
        private const val DB_NAME = "item_db_zh.sqlite"
    }

    private val _database: AppDatabase by lazy {
        val dbFile = context.getDatabasePath(DB_NAME)
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
            .createFromFile(dbFile)
            .build()
    }

    fun getDatabase(): AppDatabase = _database

}
