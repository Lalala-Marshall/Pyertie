package com.marshall.pyerite.esiModule.api

import com.marshall.pyerite.esiModule.model.EsiOrganizationDto
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * ESI `/alliances/...` routes (OpenAPI 3.1, no `/latest`, no trailing slash).
 */
internal interface EsiAllianceApi {
    @Headers("Accept: application/json")
    @GET("alliances/{alliance_id}")
    suspend fun fetchAlliance(@Path("alliance_id") allianceId: Long): EsiOrganizationDto
}
