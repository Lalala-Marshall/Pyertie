package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity

@Dao
interface TypeDao {
    @Query("SELECT * FROM types WHERE groupID = :groupId ORDER BY type_id")
    suspend fun getTypesByGroup(groupId: Int): List<TypeEntity>

    @Query("SELECT * FROM types WHERE type_id = :typeId")
    suspend fun getTypeById(typeId: Int): TypeEntity?
}