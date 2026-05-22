package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.Entity

@Entity(
    tableName = "blueprint_manufacturing_skills",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintManufacturingSkillEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val level: Int?,
)
