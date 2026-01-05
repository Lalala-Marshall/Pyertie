package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity

@Dao
interface CategoryDao {
    @RawQuery
    suspend fun getCategoriesRaw(query: SupportSQLiteQuery): List<CategoryEntity>
}