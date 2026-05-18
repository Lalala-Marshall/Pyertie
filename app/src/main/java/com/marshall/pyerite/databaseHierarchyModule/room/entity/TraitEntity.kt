package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "traits",
    primaryKeys = ["typeid", "content", "skill"]
)
data class TraitEntity(
    @ColumnInfo(name = "typeid") val typeId: Int,
    val content: String,
    @ColumnInfo(name = "skill", defaultValue = "-1") val skill: Int,
    val importance: Int?,
    @ColumnInfo(name = "bonus_type") val bonusType: String?,
)
