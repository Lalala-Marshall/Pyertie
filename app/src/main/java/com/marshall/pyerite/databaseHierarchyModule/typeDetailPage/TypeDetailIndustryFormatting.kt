package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

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

/** EVE blueprint process time column is stored in seconds. */
internal fun formatDurationFromSeconds(seconds: Int?): String {
    if (seconds == null || seconds <= 0) return ""
    return formatDurationFromMilliseconds(seconds.toDouble() * 1000.0)
}

internal fun formatDurationFromMilliseconds(rawValue: Double): String {
    var totalSeconds = (rawValue / 1000.0).toInt().coerceAtLeast(0)
    val hours = totalSeconds / 3600
    totalSeconds %= 3600
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        append("${secs}s")
    }.trim()
}
