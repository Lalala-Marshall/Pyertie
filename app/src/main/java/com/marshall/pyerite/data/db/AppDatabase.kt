package com.marshall.pyerite.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marshall.pyerite.databaseHierarchyModule.room.dao.CategoryDao
import com.marshall.pyerite.databaseHierarchyModule.room.dao.GroupDao
import com.marshall.pyerite.databaseHierarchyModule.room.dao.TypeDao

@Database(
    entities = [RoomAnchorEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun groupDao(): GroupDao
    abstract fun typeDao(): TypeDao
}