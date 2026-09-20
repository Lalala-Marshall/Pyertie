package com.marshall.pyerite.corporationModule.wallet.model

import com.marshall.pyerite.localization.ContentLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object CorporationWalletDateFormatter {

    fun dayKey(epochMs: Long): String = formatLocal(epochMs, CorporationWalletConfig.DAY_KEY_PATTERN)

    fun displayDate(epochMs: Long, language: ContentLanguage): String =
        formatLocal(epochMs, datePattern(language), localeFor(language))

    fun displayDate(dayKey: String, language: ContentLanguage): String {
        val parsed = parseDayKey(dayKey) ?: return dayKey
        return displayDate(parsed, language)
    }

    fun displayDateTimeMinute(epochMs: Long, language: ContentLanguage): String {
        val date = displayDate(epochMs, language)
        val time = formatLocal(epochMs, CorporationWalletConfig.DISPLAY_TIME_MINUTE_PATTERN)
        return "$date $time"
    }

    fun displayTimeSecond(epochMs: Long): String =
        formatLocal(epochMs, CorporationWalletConfig.DISPLAY_TIME_SECOND_PATTERN)

    fun dayStartEpochMs(dayKey: String): Long = parseDayKey(dayKey) ?: 0L

    fun isWithinRollingDays(epochMs: Long, days: Int, nowMs: Long): Boolean {
        val windowMs = days * CorporationWalletConfig.MILLIS_PER_DAY
        val age = nowMs - epochMs
        return age in 0 until windowMs
    }

    private fun parseDayKey(dayKey: String): Long? = runCatching {
        SimpleDateFormat(CorporationWalletConfig.DAY_KEY_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getDefault()
            isLenient = false
        }.parse(dayKey)?.time
    }.getOrNull()

    private fun datePattern(language: ContentLanguage): String = when (language) {
        ContentLanguage.CHINESE -> CorporationWalletConfig.DISPLAY_DATE_PATTERN_ZH
        ContentLanguage.ENGLISH -> CorporationWalletConfig.DISPLAY_DATE_PATTERN_EN
    }

    private fun localeFor(language: ContentLanguage): Locale = when (language) {
        ContentLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
        ContentLanguage.ENGLISH -> Locale.US
    }

    private fun formatLocal(
        epochMs: Long,
        pattern: String,
        locale: Locale = Locale.US,
    ): String = SimpleDateFormat(pattern, locale).apply {
        timeZone = TimeZone.getDefault()
    }.format(Date(epochMs))
}
