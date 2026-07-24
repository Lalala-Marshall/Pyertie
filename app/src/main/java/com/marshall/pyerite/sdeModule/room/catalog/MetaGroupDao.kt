package com.marshall.pyerite.sdeModule.room.catalog

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MetaGroupDao {
    @Query("SELECT * FROM metaGroups")
    suspend fun getAllMetaGroups(): List<MetaGroupEntity>
}
