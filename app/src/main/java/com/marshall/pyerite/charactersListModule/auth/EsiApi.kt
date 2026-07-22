package com.marshall.pyerite.charactersListModule.auth

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * ESI routes follow OpenAPI 3.1 paths (no `/latest`, no trailing slash).
 * Compatibility is pinned via [com.marshall.pyerite.data.network.EsiNetworkConfig.COMPATIBILITY_DATE]
 * on the shared OkHttp client.
 */
internal interface EsiApi {
    @Headers("Accept: application/json")
    @GET("characters/{character_id}")
    suspend fun fetchCharacter(@Path("character_id") characterId: Long): EsiCharacterDto

    @Headers("Accept: application/json")
    @GET("corporations/{corporation_id}")
    suspend fun fetchCorporation(@Path("corporation_id") corporationId: Long): EsiOrganizationDto

    @Headers("Accept: application/json")
    @GET("alliances/{alliance_id}")
    suspend fun fetchAlliance(@Path("alliance_id") allianceId: Long): EsiOrganizationDto

    @Headers("Accept: application/json")
    @GET("universe/systems/{system_id}")
    suspend fun fetchSolarSystem(@Path("system_id") systemId: Long): EsiUniverseSystemDto

    @Headers("Accept: application/json")
    @GET("universe/types/{type_id}")
    suspend fun fetchUniverseType(@Path("type_id") typeId: Int): EsiUniverseTypeDto

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/wallet")
    suspend fun fetchWallet(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): ResponseBody

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/skills")
    suspend fun fetchSkills(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): EsiCharacterSkillsDto

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/skillqueue")
    suspend fun fetchSkillQueue(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): List<EsiSkillQueueEntryDto>

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/location")
    suspend fun fetchLocation(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): EsiCharacterLocationDto

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/ship")
    suspend fun fetchShip(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): EsiCharacterShipDto

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/fatigue")
    suspend fun fetchFatigue(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): EsiCharacterFatigueDto

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/medals")
    suspend fun fetchMedals(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): List<EsiCharacterMedalDto>

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/online")
    suspend fun fetchOnline(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): EsiCharacterOnlineDto

    @Headers("Accept: application/json")
    @GET("universe/structures/{structure_id}")
    suspend fun fetchStructure(
        @Path("structure_id") structureId: Long,
        @Header("Authorization") authorization: String,
    ): EsiUniverseStructureDto
}
