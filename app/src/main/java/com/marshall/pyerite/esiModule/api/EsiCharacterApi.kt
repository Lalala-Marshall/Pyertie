package com.marshall.pyerite.esiModule.api

import com.marshall.pyerite.esiModule.model.EsiCharacterAttributesDto
import com.marshall.pyerite.esiModule.model.EsiCharacterClonesDto
import com.marshall.pyerite.esiModule.model.EsiCharacterDto
import com.marshall.pyerite.esiModule.model.EsiCharacterFatigueDto
import com.marshall.pyerite.esiModule.model.EsiCharacterLocationDto
import com.marshall.pyerite.esiModule.model.EsiCharacterMedalDto
import com.marshall.pyerite.esiModule.model.EsiCharacterOnlineDto
import com.marshall.pyerite.esiModule.model.EsiCharacterShipDto
import com.marshall.pyerite.esiModule.model.EsiCharacterSkillsDto
import com.marshall.pyerite.esiModule.model.EsiMailHeaderDto
import com.marshall.pyerite.esiModule.model.EsiMailingListDto
import com.marshall.pyerite.esiModule.model.EsiSkillQueueEntryDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * ESI `/characters/...` routes (OpenAPI 3.1, no `/latest`, no trailing slash).
 */
internal interface EsiCharacterApi {
    @Headers("Accept: application/json")
    @GET("characters/{character_id}")
    suspend fun fetchCharacter(@Path("character_id") characterId: Long): EsiCharacterDto

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
    @GET("characters/{character_id}/attributes")
    suspend fun fetchAttributes(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): EsiCharacterAttributesDto

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
    @GET("characters/{character_id}/clones")
    suspend fun fetchClones(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): EsiCharacterClonesDto

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/implants")
    suspend fun fetchImplants(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): List<Int>

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
    @GET("characters/{character_id}/mail")
    suspend fun fetchMailHeaders(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): List<EsiMailHeaderDto>

    @Headers("Accept: application/json")
    @GET("characters/{character_id}/mail/lists")
    suspend fun fetchMailingLists(
        @Path("character_id") characterId: Long,
        @Header("Authorization") authorization: String,
    ): List<EsiMailingListDto>
}
