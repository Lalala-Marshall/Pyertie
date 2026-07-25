package com.marshall.pyerite.util

/**
 * Project-wide duration breakdown for user-visible remaining / training times.
 *
 * Calendar approximations: 30-day month, 365-day year.
 * Unit labels are fixed Latin letters (not localized): y / mo / d / h / m / s.
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

    private const val UNIT_YEAR = "y"
    private const val UNIT_MONTH = "mo"
    private const val UNIT_DAY = "d"
    private const val UNIT_HOUR = "h"
    private const val UNIT_MINUTE = "m"
    private const val UNIT_SECOND = "s"
    private const val PART_SEPARATOR = " "

    /**
     * @param includeSeconds when false, leftover seconds are dropped (minute is the
     *   smallest unit — used for character skill-queue remaining time).
     */
    fun split(
        totalSeconds: Long,
        includeSeconds: Boolean = true,
    ): Components {
        var remaining = totalSeconds.coerceAtLeast(0L)

        val years = (remaining / SECONDS_PER_YEAR).toInt()
        remaining %= SECONDS_PER_YEAR
        val months = (remaining / SECONDS_PER_MONTH).toInt()
        remaining %= SECONDS_PER_MONTH
        val days = (remaining / SECONDS_PER_DAY).toInt()
        remaining %= SECONDS_PER_DAY
        val hours = (remaining / SECONDS_PER_HOUR).toInt()
        remaining %= SECONDS_PER_HOUR
        val minutes = (remaining / SECONDS_PER_MINUTE).toInt()
        val seconds = if (includeSeconds) {
            (remaining % SECONDS_PER_MINUTE).toInt()
        } else {
            0
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

    /**
     * Formats [totalSeconds] as e.g. `1y 2mo 3d 4h 5m` (lowercase Latin units).
     */
    fun format(
        totalSeconds: Long,
        includeSeconds: Boolean = false,
    ): String {
        val components = split(
            totalSeconds = totalSeconds,
            includeSeconds = includeSeconds,
        )
        val parts = buildList {
            if (components.years > 0) {
                add("${components.years}$UNIT_YEAR")
            }
            if (components.months > 0) {
                add("${components.months}$UNIT_MONTH")
            }
            if (components.days > 0) {
                add("${components.days}$UNIT_DAY")
            }
            if (components.hours > 0) {
                add("${components.hours}$UNIT_HOUR")
            }
            if (components.minutes > 0) {
                add("${components.minutes}$UNIT_MINUTE")
            }
            if (includeSeconds) {
                if (components.seconds > 0 || isEmpty()) {
                    add("${components.seconds}$UNIT_SECOND")
                }
            } else if (isEmpty()) {
                add("0$UNIT_MINUTE")
            }
        }
        return parts.joinToString(PART_SEPARATOR)
    }

    data class Components(
        val years: Int,
        val months: Int,
        val days: Int,
        val hours: Int,
        val minutes: Int,
        val seconds: Int,
    )
}
