package com.marshall.pyerite.esiModule.model

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Parses ESI ISO-8601 timestamps (UTC) to epoch millis. */
internal fun parseEsiDateMillis(raw: String?): Long? {
    if (raw.isNullOrBlank()) return null
    val normalized = raw.trim().removeSuffix("Z") + "Z"
    for (pattern in EsiDateTimeConfig.PARSE_PATTERNS) {
        val parsed = runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone(EsiDateTimeConfig.TIME_ZONE_UTC)
                isLenient = false
            }.parse(normalized)?.time
        }.getOrNull()
        if (parsed != null) return parsed
    }
    return null
}

internal object EsiDateTimeConfig {
    const val TIME_ZONE_UTC = "UTC"
    val PARSE_PATTERNS: Array<String> = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    )
    const val DISPLAY_DATE_PATTERN = "yyyy-MM-dd"
    const val DISPLAY_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm"
}
