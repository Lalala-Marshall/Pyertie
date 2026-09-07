package com.marshall.pyerite.esiModule.api

import com.marshall.pyerite.esiModule.model.EsiMarketPriceDto
import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * ESI `/markets/...` routes (OpenAPI 3.1, no `/latest`, no trailing slash).
 */
internal interface EsiMarketApi {
    @Headers("Accept: application/json")
    @GET("markets/prices")
    suspend fun fetchPrices(): List<EsiMarketPriceDto>
}
