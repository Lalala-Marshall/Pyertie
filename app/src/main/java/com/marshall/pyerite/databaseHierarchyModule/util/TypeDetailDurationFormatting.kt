package com.marshall.pyerite.databaseHierarchyModule.util

import androidx.compose.runtime.Composable
import com.marshall.pyerite.util.formatDurationDisplay

@Composable
internal fun formatDurationFromSeconds(seconds: Int?): String {
    if (seconds == null || seconds <= 0) return ""
    return formatDurationFromTotalSeconds(seconds.toLong())
}

@Composable
internal fun formatDurationFromMilliseconds(rawValue: Double?): String {
    if (rawValue == null) return ""
    val totalSeconds = (rawValue / 1000.0).toLong().coerceAtLeast(0L)
    return formatDurationFromTotalSeconds(totalSeconds)
}

@Composable
private fun formatDurationFromTotalSeconds(totalSecondsInput: Long): String =
    formatDurationDisplay(
        totalSeconds = totalSecondsInput,
        includeSeconds = true,
    )
