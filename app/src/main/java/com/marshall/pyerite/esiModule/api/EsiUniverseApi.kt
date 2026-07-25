package com.marshall.pyerite.esiModule.api

import com.marshall.pyerite.esiModule.model.EsiUniverseStarDto
import com.marshall.pyerite.esiModule.model.EsiUniverseStationDto
import com.marshall.pyerite.esiModule.model.EsiUniverseStructureDto
import com.marshall.pyerite.esiModule.model.EsiUniverseSystemDto
import com.marshall.pyerite.esiModule.model.EsiUniverseTypeDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * ESI `/universe/...` routes (OpenAPI 3.1, no `/latest`, no trailing slash).
 */
internal interface EsiUniverseApi {
    @Headers("Accept: application/json")
    @GET("universe/systems/{system_id}")
    suspend fun fetchSolarSystem(@Path("system_id") systemId: Long): EsiUniverseSystemDto

    @Headers("Accept: application/json")
    @GET("universe/stars/{star_id}")
    suspend fun fetchStar(@Path("star_id") starId: Long): EsiUniverseStarDto

    @Headers("Accept: application/json")
    @GET("universe/types/{type_id}")
    suspend fun fetchUniverseType(@Path("type_id") typeId: Int): EsiUniverseTypeDto

    @Headers("Accept: application/json")
    @GET("universe/structures/{structure_id}")
    suspend fun fetchStructure(
        @Path("structure_id") structureId: Long,
        @Header("Authorization") authorization: String,
    ): EsiUniverseStructureDto

    @Headers("Accept: application/json")
    @GET("universe/stations/{station_id}")
    suspend fun fetchStation(@Path("station_id") stationId: Long): EsiUniverseStationDto
}
