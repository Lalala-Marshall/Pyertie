package com.marshall.pyerite.loyaltyPointsModule.model

/** Cache TTL and solar-system security display for loyalty-point screens. */
internal object LoyaltyPointsConfig {
    const val SYSTEM_SECURITY_FORMAT = "%.1f"
    const val SECURITY_LOW_THRESHOLD = 0.5
    const val LOCATION_SEGMENT_GAP = " "
    const val UNKNOWN_REGION_ID = -1

    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_MINUTE = 60
    private const val MINUTES_PER_HOUR = 60
    const val MARKET_PRICE_CACHE_TTL_MS =
        MILLIS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR

    fun isMilitiaCorporation(militiaFactionId: Int?): Boolean {
        val id = militiaFactionId ?: return false
        return id != NO_MILITIA_FACTION_ID
    }

    private const val NO_MILITIA_FACTION_ID = 0
}
