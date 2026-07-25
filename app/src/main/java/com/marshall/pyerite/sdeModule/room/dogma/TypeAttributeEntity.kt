package com.marshall.pyerite.sdeModule.room.dogma

import androidx.room.*

@Entity(
    tableName = "typeAttributes",
    primaryKeys = ["type_id", "attribute_id"]
)
data class TypeAttributeEntity(
    @ColumnInfo(name = "type_id")
    val typeId: Int,
    @ColumnInfo(name = "attribute_id")
    val attributeId: Int,
    val value: Double?,
    @ColumnInfo(name = "unitID")
    val unitId: Int?
)
