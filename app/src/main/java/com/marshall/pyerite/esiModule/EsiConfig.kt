package com.marshall.pyerite.esiModule

import com.marshall.pyerite.data.network.EsiNetworkConfig

/** Public ESI / image-server endpoints (not secrets). */
object EsiConfig {
    const val BASE_URL = EsiNetworkConfig.BASE_URL
    const val IMAGE_BASE_URL = "https://images.evetech.net/"

    object Image {
        const val PORTRAIT_SIZE = 128
        const val LOGO_SIZE = 64
    }
}
