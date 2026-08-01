package com.marshall.pyerite.util

import com.marshall.pyerite.localization.ContentLanguage
import kotlin.math.ceil

/**
 * Project-wide duration breakdown for user-visible remaining / training times.
 *
 * Calendar approximations: 30-day month, 365-day year.
 * Unit labels follow [ContentLanguage] (zh: 年/月/天/时/分/秒, en: y/mo/d/h/m/s).
 */
object DurationDisplayFormatter {

    const val SECONDS_PER_MINUTE = 60
    const val MINUTES_PER_HOUR = 60
    const val HOURS_PER_DAY = 24
    const val DAYS_PER_MONTH = 30
    const val DAYS_PER_YEAR = 365

    const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
    const val SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY
    const val SECONDS_PER_MONTH = SECONDS_PER_DAY * DAYS_PER_MONTH
    const val SECONDS_PER_YEAR = SECONDS_PER_DAY * DAYS_PER_YEAR

    /** Smallest unit included in [format] / [split]. */
    enum class Precision {
        /** … → s (type-detail dogma, manufacturing, etc.). */
        SECOND,

        /** … → m; leftover seconds dropped (most remaining-time UI). */
        MINUTE,

        /**
         * … → h; rounded **up** to whole hours (skills-page queue),
         * matching in-game hour-precision remaining display.
         */
        HOUR,
    }

    /** Largest unit included in [format] / [split]. */
    enum class MaxUnit {
        /** y → mo → d → … */
        YEAR,

        /** d → … (years/months folded into total days). */
        DAY,
    }

    private data class UnitStyle(
        val year: String,
        val month: String,
        val day: String,
        val hour: String,
        val minute: String,
        val second: String,
        val separator: String,
    )

    private fun unitStyle(language: ContentLanguage): UnitStyle = when (language) {
        ContentLanguage.CHINESE -> UnitStyle(
            year = UNIT_YEAR_ZH,
            month = UNIT_MONTH_ZH,
            day = UNIT_DAY_ZH,
            hour = UNIT_HOUR_ZH,
            minute = UNIT_MINUTE_ZH,
            second = UNIT_SECOND_ZH,
            separator = PART_SEPARATOR_ZH,
        )
        ContentLanguage.ENGLISH -> UnitStyle(
            year = UNIT_YEAR_EN,
            month = UNIT_MONTH_EN,
            day = UNIT_DAY_EN,
            hour = UNIT_HOUR_EN,
            minute = UNIT_MINUTE_EN,
            second = UNIT_SECOND_EN,
            separator = PART_SEPARATOR_EN,
        )
    }

    /**
     * @param includeSeconds when false, leftover seconds are dropped (minute is the
     *   smallest unit — used for character skill-queue remaining time).
     */
    fun split(
        totalSeconds: Long,
        includeSeconds: Boolean = true,
    ): Components = split(
        totalSeconds = totalSeconds,
        precision = if (includeSeconds) Precision.SECOND else Precision.MINUTE,
    )

    fun split(
        totalSeconds: Long,
        precision: Precision,
        maxUnit: MaxUnit = MaxUnit.YEAR,
    ): Components {
        var remaining = when (precision) {
            Precision.HOUR -> roundUpToWholeHours(totalSeconds)
            Precision.MINUTE, Precision.SECOND -> totalSeconds.coerceAtLeast(0L)
        }

        val years: Int
        val months: Int
        val days: Int
        when (maxUnit) {
            MaxUnit.YEAR -> {
                years = (remaining / SECONDS_PER_YEAR).toInt()
                remaining %= SECONDS_PER_YEAR
                months = (remaining / SECONDS_PER_MONTH).toInt()
                remaining %= SECONDS_PER_MONTH
                days = (remaining / SECONDS_PER_DAY).toInt()
                remaining %= SECONDS_PER_DAY
            }
            MaxUnit.DAY -> {
                years = 0
                months = 0
                days = (remaining / SECONDS_PER_DAY).toInt()
                remaining %= SECONDS_PER_DAY
            }
        }
        val hours = (remaining / SECONDS_PER_HOUR).toInt()
        remaining %= SECONDS_PER_HOUR
        val minutes = when (precision) {
            Precision.HOUR -> 0
            Precision.MINUTE, Precision.SECOND -> (remaining / SECONDS_PER_MINUTE).toInt()
        }
        val seconds = when (precision) {
            Precision.SECOND -> (remaining % SECONDS_PER_MINUTE).toInt()
            Precision.MINUTE, Precision.HOUR -> 0
        }

        return Components(
            years = years,
            months = months,
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
        )
    }

    /** Ceil to whole hours so e.g. 2h 1m displays as 3h, not 2h. */
    private fun roundUpToWholeHours(totalSeconds: Long): Long {
        if (totalSeconds <= 0L) return 0L
        return ceil(totalSeconds.toDouble() / SECONDS_PER_HOUR).toLong() * SECONDS_PER_HOUR
    }

    /**
     * Formats [totalSeconds] with language-appropriate units
     * (e.g. `1y 2mo` / `1年2月`).
     */
    fun format(
        totalSeconds: Long,
        includeSeconds: Boolean = false,
        language: ContentLanguage,
        maxUnit: MaxUnit = MaxUnit.YEAR,
    ): String = format(
        totalSeconds = totalSeconds,
        precision = if (includeSeconds) Precision.SECOND else Precision.MINUTE,
        language = language,
        maxUnit = maxUnit,
    )

    fun format(
        totalSeconds: Long,
        precision: Precision,
        language: ContentLanguage,
        maxUnit: MaxUnit = MaxUnit.YEAR,
    ): String {
        val components = split(
            totalSeconds = totalSeconds,
            precision = precision,
            maxUnit = maxUnit,
        )
        val units = unitStyle(language)
        val parts = buildList {
            if (components.years > 0) {
                add("${components.years}${units.year}")
            }
            if (components.months > 0) {
                add("${components.months}${units.month}")
            }
            if (components.days > 0) {
                add("${components.days}${units.day}")
            }
            if (components.hours > 0) {
                add("${components.hours}${units.hour}")
            }
            if (precision != Precision.HOUR && components.minutes > 0) {
                add("${components.minutes}${units.minute}")
            }
            when (precision) {
                Precision.SECOND -> {
                    if (components.seconds > 0 || isEmpty()) {
                        add("${components.seconds}${units.second}")
                    }
                }
                Precision.MINUTE -> {
                    if (isEmpty()) {
                        add("0${units.minute}")
                    }
                }
                Precision.HOUR -> {
                    if (isEmpty()) {
                        add("0${units.hour}")
                    }
                }
            }
        }
        return parts.joinToString(units.separator)
    }

    data class Components(
        val years: Int,
        val months: Int,
        val days: Int,
        val hours: Int,
        val minutes: Int,
        val seconds: Int,
    )

    private const val UNIT_YEAR_EN = "y"
    private const val UNIT_MONTH_EN = "mo"
    private const val UNIT_DAY_EN = "d"
    private const val UNIT_HOUR_EN = "h"
    private const val UNIT_MINUTE_EN = "m"
    private const val UNIT_SECOND_EN = "s"
    private const val PART_SEPARATOR_EN = " "

    private const val UNIT_YEAR_ZH = "年"
    private const val UNIT_MONTH_ZH = "月"
    private const val UNIT_DAY_ZH = "天"
    private const val UNIT_HOUR_ZH = "时"
    private const val UNIT_MINUTE_ZH = "分"
    private const val UNIT_SECOND_ZH = "秒"
    private const val PART_SEPARATOR_ZH = ""
}
