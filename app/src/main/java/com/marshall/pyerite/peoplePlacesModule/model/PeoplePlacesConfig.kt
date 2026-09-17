package com.marshall.pyerite.peoplePlacesModule.model

import kotlin.time.Duration.Companion.milliseconds

internal object PeoplePlacesConfig {
    const val DISPLAY_LIMIT = 50
    const val SEARCH_MIN_LENGTH = 3
    val SEARCH_DEBOUNCE = 600.milliseconds
    const val HYDRATE_CONCURRENCY = 8
    const val SYSTEM_SECURITY_FORMAT = "%.1f"
    const val SECURITY_LOW_THRESHOLD = 0.5
    const val LOCATION_SEGMENT_GAP = " "
    const val LOCATION_NAME_SEPARATOR = "/"
    const val STANDING_PLUS_10 = 10.0
    const val STANDING_PLUS_5 = 5.0
    const val STANDING_MINUS_5 = -5.0
    const val STANDING_MINUS_10 = -10.0
    const val MATCH_RANK_EXACT = 0
    const val MATCH_RANK_PREFIX = 1
    const val MATCH_RANK_CONTAINS = 2
    const val MATCH_RANK_OTHER = 3
}
