package com.marshall.pyerite.sdeModule.room.industry

import androidx.room.Entity

@Entity(
    tableName = "blueprint_manufacturing_output",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintManufacturingOutputEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val quantity: Int?,
)
