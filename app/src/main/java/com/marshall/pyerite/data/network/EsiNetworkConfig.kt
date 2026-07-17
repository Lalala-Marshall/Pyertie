package com.marshall.pyerite.data.network

/** ESI HTTP defaults shared by OkHttp and feature API clients. */
object EsiNetworkConfig {
    const val HOST = "esi.evetech.net"
    const val BASE_URL = "https://$HOST/"

    /**
     * Pins ESI behavior to a reviewed OpenAPI compatibility date
     * (`X-Compatibility-Date`). Bump intentionally when adopting API changes.
     */
    const val COMPATIBILITY_DATE = "2026-07-17"
}
