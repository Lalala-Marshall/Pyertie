package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups WHERE categoryID = :categoryId AND published != 0 ORDER BY group_id")
    suspend fun getGroupsByCategory(categoryId: Int): List<GroupEntity>
}