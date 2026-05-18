package com.marshall.pyerite.databaseHierarchyModule.room.entity

data class TypeBlueprintDetail(
    val typeId: Int,
    val name: String?,
    val iconFilename: String?,
)

data class TypeRefiningOutputSummary(
    val processSize: Int?,
    val outputMaterialCount: Int,
)
