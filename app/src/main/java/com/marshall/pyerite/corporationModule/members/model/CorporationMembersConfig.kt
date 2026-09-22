package com.marshall.pyerite.corporationModule.members.model

/** Pagination, location display, and list separators for corporation members. */
internal object CorporationMembersConfig {
    const val FIRST_PAGE = 1
    /** ESI member-tracking page size. */
    const val MEMBER_TRACKING_PAGE_SIZE = 1_000
    const val MEMBER_TRACKING_MAX_PAGES = 20
    const val QUERY_CHUNK = 500
    /** Player-owned Upwell structures use location IDs at or above this value. */
    const val PLAYER_STRUCTURE_ID_MIN = 1_000_000_000_000L

    const val SYSTEM_SECURITY_FORMAT = "%.1f"
    const val HINT_SEPARATOR = " · "
    const val LOCATION_SEGMENT_GAP = " "
    const val SECURITY_NEGATIVE_MAX = 0.0
    const val SECURITY_LOW_MAX = 0.5
}
