package com.marshall.pyerite.util

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/**
 * Project-wide numeric display for ISK, SP, and similar magnitudes.
 *
 * - `Style.COMPACT`: abbreviate with `M` / `B` (no `k`); two fractional digits, truncated.
 * - `Style.FULL`: every three digits grouped with commas; doubles also truncate to two decimals.
 */
object NumberDisplayFormatter {

    enum class Style {
        /** `M` then `B`; below one million uses `Style.FULL`. */
        COMPACT,

        /** A Thousand separators; no abbreviation. */
        FULL,
    }

    private const val MILLION = 1_000_000.0
    private const val BILLION = 1_000_000_000.0
    private const val FRACTION_SCALE = 100.0
    private const val FRACTION_DIGITS = 2

    private const val SUFFIX_MILLION = "M"
    private const val SUFFIX_BILLION = "B"

    fun format(value: Double, style: Style): String = when (style) {
        Style.FULL -> formatFull(value)
        Style.COMPACT -> formatCompact(value)
    }

    fun format(value: Long, style: Style): String = when (style) {
        Style.FULL -> formatFullInteger(value)
        Style.COMPACT -> formatCompact(value.toDouble())
    }

    private fun formatCompact(value: Double): String {
        val sign = if (value < 0) "-" else ""
        val magnitude = abs(value)
        return when {
            magnitude >= BILLION -> {
                sign + truncateToFixed(magnitude / BILLION) + SUFFIX_BILLION
            }
            magnitude >= MILLION -> {
                sign + truncateToFixed(magnitude / MILLION) + SUFFIX_MILLION
            }
            else -> formatFull(value)
        }
    }

    private fun formatFull(value: Double): String {
        val sign = if (value < 0) "-" else ""
        val magnitude = abs(value)
        val integerPart = floor(magnitude).toLong()
        val fractionPart = floor((magnitude - integerPart) * FRACTION_SCALE).toInt()
        return sign + formatFullInteger(integerPart) +
            "." + fractionPart.toString().padStart(FRACTION_DIGITS, '0')
    }

    private fun formatFullInteger(value: Long): String =
        String.format(Locale.US, "%,d", value)

    /** Truncate (not round) to [FRACTION_DIGITS] places; always emits that many digits. */
    private fun truncateToFixed(value: Double): String {
        val truncated = floor(value * FRACTION_SCALE) / FRACTION_SCALE
        val integerPart = floor(truncated).toLong()
        val fractionPart = floor((truncated - integerPart) * FRACTION_SCALE).toInt()
        return integerPart.toString() +
            "." + fractionPart.toString().padStart(FRACTION_DIGITS, '0')
    }
}
