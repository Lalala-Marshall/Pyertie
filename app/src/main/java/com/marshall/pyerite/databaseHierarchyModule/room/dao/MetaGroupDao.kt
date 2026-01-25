package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.databaseHierarchyModule.room.entity.MetaGroupEntity

@Dao
interface MetaGroupDao {
    @Query("SELECT * FROM metaGroups")
    suspend fun getAllMetaGroups(): List<MetaGroupEntity>
}
