package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity

@Dao
interface TypeDao {
    @RawQuery
    suspend fun getTypesByGroup(
        query: SupportSQLiteQuery
    ): List<TypeEntity>
}