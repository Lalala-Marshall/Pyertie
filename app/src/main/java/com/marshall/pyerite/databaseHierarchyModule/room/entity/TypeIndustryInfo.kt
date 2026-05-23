package com.marshall.pyerite.databaseHierarchyModule.room.entity

data class TypeBlueprintDetail(
    val typeId: Int,
    val name: String?,
    val iconFilename: String?,
)

/** Product manufactured by a blueprint (one row per blueprint in SDE). */
data class BlueprintManufacturingProduct(
    val typeId: Int,
    val name: String?,
    val iconFilename: String?,
    val quantity: Int?,
)

/** Material required to manufacture from a blueprint. */
data class BlueprintManufacturingMaterial(
    val typeId: Int,
    val name: String?,
    val iconFilename: String?,
    val quantity: Int?,
)

/** Skill required to manufacture from a blueprint. */
data class BlueprintManufacturingSkill(
    val typeId: Int,
    val name: String?,
    val iconFilename: String?,
    val level: Int?,
)

/** One ME research level and cumulative seconds from level 0. */
data class BlueprintMaterialResearchLevelTime(
    val level: Int,
    val cumulativeTimeSeconds: Int,
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

/** One material produced when refining the queried ore type. */
data class TypeRefiningOutputItem(
    val typeId: Int,
    val name: String?,
    val iconFilename: String?,
    val quantity: Int?,
)

/** One ore type that refines into the queried material. */
data class TypeRefiningSourceItem(
    val typeId: Int,
    val name: String?,
    val iconFilename: String?,
    val metaGroupId: Int?,
    val quantityPerUnit: Int?,
    val processSize: Int?,
)

/** chargeGroup* / launcherGroup* dogma row before group lookup. */
data class TypeCompatibleGroupRef(
    val attributeId: Int,
    val attributeName: String?,
    val attributeDisplayName: String?,
    val attributeIconFilename: String?,
    val groupId: Int,
)

/** A launcher/charge group this type is compatible with (from chargeGroup* / launcherGroup* dogma). */
data class TypeCompatibleGroupDetail(
    val attributeId: Int,
    val attributeName: String?,
    val attributeDisplayName: String?,
    val attributeIconFilename: String?,
    val groupId: Int,
    val groupZhName: String?,
    val groupEnName: String?,
    val groupName: String?,
    val groupIconFilename: String?,
)
