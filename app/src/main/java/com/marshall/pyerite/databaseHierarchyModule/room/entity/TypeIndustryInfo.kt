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

/** Distinct blueprint types that require the queried type as a material. */
data class TypeApplicableBlueprintCount(
    val count: Int,
)

/** Distinct ore/item types that can refine into the queried type. */
data class TypeRefiningSourceCount(
    val count: Int,
)
