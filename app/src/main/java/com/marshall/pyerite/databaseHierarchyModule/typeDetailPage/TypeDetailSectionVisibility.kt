package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeApplicableBlueprintCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceCount

internal fun hasDisplayableDogmaRows(attributes: List<TypeAttributeDetail>): Boolean =
    attributes.any { it.displayName != null && it.value != null }

internal fun hasFittingSectionContent(attributes: List<TypeAttributeDetail>): Boolean =
    attributes
        .asSequence()
        .filter { it.displayName != null && it.value != null }
        .any { it.name != FITTING_EXCLUDED_ATTRIBUTE_NAME }

internal fun hasNavigationSectionContent(attributes: List<TypeAttributeDetail>): Boolean =
    attributes
        .asSequence()
        .filter { it.displayName != null && it.value != null }
        .any { it.name != NAVIGATION_EXCLUDED_ATTRIBUTE_NAME }

internal fun hasIndustrySectionContent(
    blueprints: List<TypeBlueprintDetail>,
    refiningOutputSummary: TypeRefiningOutputSummary?,
    applicableBlueprintCount: TypeApplicableBlueprintCount?,
    refiningSourceCount: TypeRefiningSourceCount?,
): Boolean {
    val showRefiningOutput = (refiningOutputSummary?.outputMaterialCount ?: 0) > 0
    val showApplicable = (applicableBlueprintCount?.count ?: 0) > 0
    val showRefiningSource = (refiningSourceCount?.count ?: 0) > 0
    return blueprints.isNotEmpty() || showRefiningOutput || showApplicable || showRefiningSource
}

internal fun shouldShowIndustryCount(count: Int?): Boolean = (count ?: 0) > 0

internal const val FITTING_EXCLUDED_ATTRIBUTE_NAME = "upgradeSlotsLeft"
internal const val NAVIGATION_EXCLUDED_ATTRIBUTE_NAME = "warpSpeedMultiplier"
