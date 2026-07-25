package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import com.marshall.pyerite.sdeModule.room.industry.BlueprintResearchLevelTime
import com.marshall.pyerite.util.NumberDisplayFormatter
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

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
    NumberDisplayFormatter.format(count.toLong(), NumberDisplayFormatter.Style.FULL)

/** Fixed [Locale.US] so display stays stable if the device locale changes at runtime. */
private val inventionProbabilityFormatter = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 1
}

internal fun formatInventionProbability(probability: Double?): String {
    if (probability == null) return ""
    val percent = probability * 100.0
    return if (percent == percent.roundToInt().toDouble()) {
        "${percent.roundToInt()}%"
    } else {
        "${inventionProbabilityFormatter.format(percent)}%"
    }
}

internal fun buildBlueprintResearchLevelTimes(
    baseTimeSeconds: Int,
    levelTimeModifiers: IntArray,
    timeDivisor: Int,
): List<BlueprintResearchLevelTime> {
    if (baseTimeSeconds <= 0 || timeDivisor <= 0 || levelTimeModifiers.size <= 1) {
        return emptyList()
    }
    val maxLevel = levelTimeModifiers.lastIndex
    return (1..maxLevel).map { level ->
        val cumulativeSeconds = baseTimeSeconds * levelTimeModifiers[level] / timeDivisor
        BlueprintResearchLevelTime(
            level = level,
            cumulativeTimeSeconds = cumulativeSeconds,
        )
    }
}
