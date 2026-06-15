package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.Entity

@Entity(
    tableName = "blueprint_invention_products",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintInventionProductEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val quantity: Int?,
    val probability: Double?,
)

@Entity(
    tableName = "blueprint_invention_skills",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintInventionSkillEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val level: Int?,
)
