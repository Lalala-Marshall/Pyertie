package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity

@Dao
interface GroupDao {
    @RawQuery
    suspend fun getGroupsByCategory(query: SupportSQLiteQuery): List<GroupEntity>
}