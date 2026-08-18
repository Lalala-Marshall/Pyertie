package com.marshall.pyerite.esiModule.api

import com.marshall.pyerite.esiModule.model.EsiContactDto
import com.marshall.pyerite.esiModule.model.EsiOrganizationDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * ESI `/alliances/...` routes (OpenAPI 3.1, no `/latest`, no trailing slash).
 */
internal interface EsiAllianceApi {
    @Headers("Accept: application/json")
    @GET("alliances/{alliance_id}")
    suspend fun fetchAlliance(@Path("alliance_id") allianceId: Long): EsiOrganizationDto

    @Headers("Accept: application/json")
    @GET("alliances/{alliance_id}/contacts")
    suspend fun fetchContacts(
        @Path("alliance_id") allianceId: Long,
        @Header("Authorization") authorization: String,
    ): List<EsiContactDto>
}
