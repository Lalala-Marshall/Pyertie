package com.marshall.pyerite.util

import androidx.compose.runtime.Composable
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import org.koin.compose.koinInject

/**
 * Formats a duration for UI.
 *
 * - Max unit: [DurationDisplayFormatter.MaxUnit] (default year; day for home/skills queue)
 * - [DurationDisplayFormatter.Precision.MINUTE]: min unit minute (default remaining time)
 * - [DurationDisplayFormatter.Precision.HOUR]: min unit hour (skills-page queue)
 * - [DurationDisplayFormatter.Precision.SECOND]: min unit second (type-detail dogma, etc.)
 * Unit labels follow [ContentLanguage] (zh / en).
 */
fun formatDurationDisplay(
    totalSeconds: Long,
    includeSeconds: Boolean = false,
    language: ContentLanguage,
    maxUnit: DurationDisplayFormatter.MaxUnit = DurationDisplayFormatter.MaxUnit.YEAR,
): String = formatDurationDisplay(
    totalSeconds = totalSeconds,
    precision = if (includeSeconds) {
        DurationDisplayFormatter.Precision.SECOND
    } else {
        DurationDisplayFormatter.Precision.MINUTE
    },
    language = language,
    maxUnit = maxUnit,
)

fun formatDurationDisplay(
    totalSeconds: Long,
    precision: DurationDisplayFormatter.Precision,
    language: ContentLanguage,
    maxUnit: DurationDisplayFormatter.MaxUnit = DurationDisplayFormatter.MaxUnit.YEAR,
): String = DurationDisplayFormatter.format(
    totalSeconds = totalSeconds,
    precision = precision,
    language = language,
    maxUnit = maxUnit,
)

/** Compose helper: uses [LocaleController.contentLanguage]. */
@Composable
fun formatDurationDisplay(
    totalSeconds: Long,
    includeSeconds: Boolean = false,
    maxUnit: DurationDisplayFormatter.MaxUnit = DurationDisplayFormatter.MaxUnit.YEAR,
): String {
    val localeController: LocaleController = koinInject()
    return formatDurationDisplay(
        totalSeconds = totalSeconds,
        includeSeconds = includeSeconds,
        language = localeController.contentLanguage,
        maxUnit = maxUnit,
    )
}

/** Compose helper: uses [LocaleController.contentLanguage]. */
@Composable
fun formatDurationDisplay(
    totalSeconds: Long,
    precision: DurationDisplayFormatter.Precision,
    maxUnit: DurationDisplayFormatter.MaxUnit = DurationDisplayFormatter.MaxUnit.YEAR,
): String {
    val localeController: LocaleController = koinInject()
    return formatDurationDisplay(
        totalSeconds = totalSeconds,
        precision = precision,
        language = localeController.contentLanguage,
        maxUnit = maxUnit,
    )
}
