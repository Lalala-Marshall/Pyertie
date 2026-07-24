package com.marshall.pyerite.sdeModule.room.catalog

import androidx.room.Dao
import androidx.room.Query

@Dao
interface GroupDao {
    @Query("SELECT * FROM `groups` WHERE categoryID = :categoryId ORDER BY group_id")
    suspend fun getGroupsByCategory(categoryId: Int): List<GroupEntity>

    @Query("SELECT * FROM `groups` WHERE group_id IN (:groupIds) ORDER BY group_id")
    suspend fun getGroupsByIds(groupIds: List<Int>): List<GroupEntity>
}