package com.marshall.pyerite.util

/**
 * Formats a duration for UI.
 *
 * - Max unit: year
 * - Min unit: minute when [includeSeconds] is false (character remaining time)
 * - Min unit: second when [includeSeconds] is true (type-detail dogma, etc.)
 * Unit letters are fixed Latin lowercase (`y`/`mo`/`d`/`h`/`m`/`s`), not localized.
 */
fun formatDurationDisplay(
    totalSeconds: Long,
    includeSeconds: Boolean = false,
): String = DurationDisplayFormatter.format(
    totalSeconds = totalSeconds,
    includeSeconds = includeSeconds,
)
