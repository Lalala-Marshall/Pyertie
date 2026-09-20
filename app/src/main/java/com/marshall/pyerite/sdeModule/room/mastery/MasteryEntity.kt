package com.marshall.pyerite.sdeModule.room.mastery

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "masteries",
    primaryKeys = ["typeid", "masteryLevel", "certificateID"],
)
data class MasteryEntity(
    @ColumnInfo(name = "typeid") val typeId: Int,
    val masteryLevel: Int,
    @ColumnInfo(name = "certificateID") val certificateId: Int,
)
