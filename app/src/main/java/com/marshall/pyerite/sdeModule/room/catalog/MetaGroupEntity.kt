package com.marshall.pyerite.sdeModule.room.catalog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metaGroups")
data class MetaGroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "metagroup_id")
    val id: Int,
    val name: String? = null
)
