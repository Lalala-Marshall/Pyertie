package com.marshall.pyerite.util

/**
 * Formats a duration for UI.
 *
 * - Max unit: year
 * - [DurationDisplayFormatter.Precision.MINUTE]: min unit minute (default remaining time)
 * - [DurationDisplayFormatter.Precision.HOUR]: min unit hour (skills-page queue)
 * - [DurationDisplayFormatter.Precision.SECOND]: min unit second (type-detail dogma, etc.)
 * Unit letters are fixed Latin lowercase (`y`/`mo`/`d`/`h`/`m`/`s`), not localized.
 */
fun formatDurationDisplay(
    totalSeconds: Long,
    includeSeconds: Boolean = false,
): String = formatDurationDisplay(
    totalSeconds = totalSeconds,
    precision = if (includeSeconds) {
        DurationDisplayFormatter.Precision.SECOND
    } else {
        DurationDisplayFormatter.Precision.MINUTE
    },
)

fun formatDurationDisplay(
    totalSeconds: Long,
    precision: DurationDisplayFormatter.Precision,
): String = DurationDisplayFormatter.format(
    totalSeconds = totalSeconds,
    precision = precision,
)
