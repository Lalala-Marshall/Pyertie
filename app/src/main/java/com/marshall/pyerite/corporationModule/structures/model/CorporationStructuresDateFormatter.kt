package com.marshall.pyerite.corporationModule.structures.model

import com.marshall.pyerite.localization.ContentLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object CorporationStructuresDateFormatter {

    fun displayDateTime(epochMs: Long, language: ContentLanguage): String {
        val pattern = when (language) {
            ContentLanguage.CHINESE -> CorporationStructuresConfig.DISPLAY_DATE_TIME_PATTERN_ZH
            ContentLanguage.ENGLISH -> CorporationStructuresConfig.DISPLAY_DATE_TIME_PATTERN_EN
        }
        return SimpleDateFormat(pattern, localeFor(language)).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date(epochMs))
    }

    fun remainingSeconds(expiresAtMillis: Long, nowMs: Long): Long {
        val remainingMs = expiresAtMillis - nowMs
        if (remainingMs <= 0L) return 0L
        return remainingMs / CorporationStructuresConfig.MILLIS_PER_SECOND
    }

    fun isExpired(expiresAtMillis: Long, nowMs: Long): Boolean = expiresAtMillis <= nowMs

    private fun localeFor(language: ContentLanguage): Locale = when (language) {
        ContentLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
        ContentLanguage.ENGLISH -> Locale.US
    }
}
