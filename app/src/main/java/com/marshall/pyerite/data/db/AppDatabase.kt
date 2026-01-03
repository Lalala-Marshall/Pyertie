package com.marshall.pyerite.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marshall.pyerite.databaseHierarchyModule.room.dao.CategoryDao

@Database(
    entities = [RoomAnchorEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
}