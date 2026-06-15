package com.marshall.pyerite.databaseHierarchyModule.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R

internal data class DurationComponents(
    val years: Int,
    val months: Int,
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
)

internal fun splitDurationComponents(
    totalSecondsInput: Int,
    secondsPerMinute: Int,
    secondsPerHour: Int,
    secondsPerDay: Int,
    secondsPerMonth: Int,
    secondsPerYear: Int,
): DurationComponents {
    var remaining = totalSecondsInput.coerceAtLeast(0)

    val years = remaining / secondsPerYear
    remaining %= secondsPerYear
    val months = remaining / secondsPerMonth
    remaining %= secondsPerMonth
    val days = remaining / secondsPerDay
    remaining %= secondsPerDay
    val hours = remaining / secondsPerHour
    remaining %= secondsPerHour
    val minutes = remaining / secondsPerMinute
    val seconds = remaining % secondsPerMinute

    return DurationComponents(
        years = years,
        months = months,
        days = days,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
    )
}

@Composable
internal fun formatDurationFromSeconds(seconds: Int?): String {
    if (seconds == null || seconds <= 0) return ""
    return formatDurationFromTotalSeconds(seconds)
}

@Composable
internal fun formatDurationFromMilliseconds(rawValue: Double?): String {
    if (rawValue == null) return ""
    val totalSeconds = (rawValue / 1000.0).toInt().coerceAtLeast(0)
    return formatDurationFromTotalSeconds(totalSeconds)
}

@Composable
internal fun formatDurationFromTotalSeconds(totalSecondsInput: Int): String {
    val secondsPerMinute = integerResource(R.integer.duration_seconds_per_minute)
    val secondsPerHour = secondsPerMinute * integerResource(R.integer.duration_minutes_per_hour)
    val secondsPerDay = secondsPerHour * integerResource(R.integer.duration_hours_per_day)
    val secondsPerMonth = secondsPerDay * integerResource(R.integer.duration_days_per_month)
    val secondsPerYear = secondsPerDay * integerResource(R.integer.duration_days_per_year)

    val components = splitDurationComponents(
        totalSecondsInput = totalSecondsInput,
        secondsPerMinute = secondsPerMinute,
        secondsPerHour = secondsPerHour,
        secondsPerDay = secondsPerDay,
        secondsPerMonth = secondsPerMonth,
        secondsPerYear = secondsPerYear,
    )

    val parts = buildList {
        if (components.years > 0) {
            add(stringResource(R.string.duration_part_years, components.years))
        }
        if (components.months > 0) {
            add(stringResource(R.string.duration_part_months, components.months))
        }
        if (components.days > 0) {
            add(stringResource(R.string.duration_part_days, components.days))
        }
        if (components.hours > 0) {
            add(stringResource(R.string.duration_part_hours, components.hours))
        }
        if (components.minutes > 0) {
            add(stringResource(R.string.duration_part_minutes, components.minutes))
        }
        if (components.seconds > 0 || isEmpty()) {
            add(stringResource(R.string.duration_part_seconds, components.seconds))
        }
    }
    return parts.joinToString("")
}
