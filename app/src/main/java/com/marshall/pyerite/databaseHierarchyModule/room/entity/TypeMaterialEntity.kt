package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.Entity

@Entity(
    tableName = "typeMaterials",
    primaryKeys = ["typeid", "output_material"],
)
data class TypeMaterialEntity(
    val typeid: Int,
    val categoryid: Int?,
    val process_size: Int?,
    val output_material: Int,
    val output_material_categoryid: Int?,
    val output_material_groupid: Int?,
    val output_quantity: Int?,
    val output_material_name: String?,
    val output_material_icon: String?,
)
