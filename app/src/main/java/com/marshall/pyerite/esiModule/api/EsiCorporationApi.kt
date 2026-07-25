package com.marshall.pyerite.esiModule.api

import com.marshall.pyerite.esiModule.model.EsiOrganizationDto
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * ESI `/corporations/...` routes (OpenAPI 3.1, no `/latest`, no trailing slash).
 */
internal interface EsiCorporationApi {
    @Headers("Accept: application/json")
    @GET("corporations/{corporation_id}")
    suspend fun fetchCorporation(@Path("corporation_id") corporationId: Long): EsiOrganizationDto
}
