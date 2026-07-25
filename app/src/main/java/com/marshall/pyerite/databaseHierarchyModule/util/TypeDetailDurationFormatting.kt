package com.marshall.pyerite.databaseHierarchyModule.util

import com.marshall.pyerite.util.formatDurationDisplay

internal fun formatDurationFromSeconds(seconds: Int?): String {
    if (seconds == null || seconds <= 0) return ""
    return formatDurationFromTotalSeconds(seconds.toLong())
}

internal fun formatDurationFromMilliseconds(rawValue: Double?): String {
    if (rawValue == null) return ""
    val totalSeconds = (rawValue / 1000.0).toLong().coerceAtLeast(0L)
    return formatDurationFromTotalSeconds(totalSeconds)
}

private fun formatDurationFromTotalSeconds(totalSecondsInput: Long): String =
    formatDurationDisplay(
        totalSeconds = totalSecondsInput,
        includeSeconds = true,
    )
