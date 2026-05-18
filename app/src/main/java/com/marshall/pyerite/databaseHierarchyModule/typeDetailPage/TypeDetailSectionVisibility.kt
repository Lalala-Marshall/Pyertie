package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary

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
): Boolean {
    val showRefiningOutput = (refiningOutputSummary?.outputMaterialCount ?: 0) > 0
    return blueprints.isNotEmpty() || showRefiningOutput
}

internal const val FITTING_EXCLUDED_ATTRIBUTE_NAME = "upgradeSlotsLeft"
internal const val NAVIGATION_EXCLUDED_ATTRIBUTE_NAME = "warpSpeedMultiplier"
