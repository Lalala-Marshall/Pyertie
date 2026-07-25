package com.marshall.pyerite.esiModule.http

/** Public ESI / image-server endpoints and HTTP pin (not secrets). */
object EsiConfig {
    const val HOST = "esi.evetech.net"
    const val BASE_URL = "https://$HOST/"
    const val IMAGE_BASE_URL = "https://images.evetech.net/"

    /**
     * Pins ESI behavior to a reviewed OpenAPI compatibility date
     * (`X-Compatibility-Date`). Bump intentionally when adopting API changes.
     */
    const val COMPATIBILITY_DATE = "2026-07-17"

    object Image {
        const val PORTRAIT_SIZE = 128
        const val LOGO_SIZE = 64
    }
}
