package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintMaterialResearchLevelTime
import java.text.NumberFormat
import java.util.Locale

internal fun formatRefiningSourceLabel(
    name: String?,
    metaGroupId: Int?,
    metaGroupNameById: Map<Int, String?>,
): String {
    val baseName = name.orEmpty()
    if (metaGroupId == null) return baseName
    val metaLabel = metaGroupNameById[metaGroupId]?.takeIf { it.isNotBlank() }
        ?: "$metaGroupId"
    return "$baseName ($metaLabel)"
}

internal fun formatIndustryCount(count: Int): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).format(count)

internal fun buildMaterialResearchLevelTimes(
    baseTimeSeconds: Int,
    levelTimeModifiers: IntArray,
    timeDivisor: Int,
): List<BlueprintMaterialResearchLevelTime> {
    if (baseTimeSeconds <= 0 || timeDivisor <= 0 || levelTimeModifiers.size <= 1) {
        return emptyList()
    }
    val maxLevel = levelTimeModifiers.lastIndex
    return (1..maxLevel).map { level ->
        val cumulativeSeconds = baseTimeSeconds * levelTimeModifiers[level] / timeDivisor
        BlueprintMaterialResearchLevelTime(
            level = level,
            cumulativeTimeSeconds = cumulativeSeconds,
        )
    }
}
