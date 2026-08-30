package com.marshall.pyerite.personalPropertyModule.model

/** Pagination, SDE filters, and cache TTL for personal-property ESI loads. */
internal object PersonalPropertyConfig {
    const val FIRST_PAGE = 1
    const val ASSETS_PAGE_SIZE = 1000
    const val ASSETS_MAX_PAGES = 50
    const val CONTRACTS_PAGE_SIZE = 1000
    const val CONTRACTS_MAX_PAGES = 10
    const val CONTRACT_ITEMS_CONCURRENCY = 4
    const val TYPE_ID_QUERY_CHUNK = 500
    const val BLUEPRINT_CATEGORY_ID = 9

    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_MINUTE = 60
    private const val MINUTES_PER_HOUR = 60
    const val MARKET_PRICE_CACHE_TTL_MS =
        MILLIS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR
}
