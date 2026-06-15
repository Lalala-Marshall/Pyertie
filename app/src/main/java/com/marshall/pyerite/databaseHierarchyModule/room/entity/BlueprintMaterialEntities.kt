package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.Entity

@Entity(
    tableName = "blueprint_manufacturing_materials",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintManufacturingMaterialEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val quantity: Int?,
)

@Entity(
    tableName = "blueprint_invention_materials",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintInventionMaterialEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val quantity: Int?,
)

@Entity(
    tableName = "blueprint_copying_materials",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintCopyingMaterialEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val quantity: Int?,
)

@Entity(
    tableName = "blueprint_research_material_materials",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintResearchMaterialMaterialEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val quantity: Int?,
)

@Entity(
    tableName = "blueprint_research_time_materials",
    primaryKeys = ["blueprintTypeID", "typeID"],
)
data class BlueprintResearchTimeMaterialEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val typeID: Int,
    val typeName: String?,
    val typeIcon: String?,
    val quantity: Int?,
)
