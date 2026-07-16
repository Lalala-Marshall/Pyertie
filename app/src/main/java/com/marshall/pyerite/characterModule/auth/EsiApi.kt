package com.marshall.pyerite.characterModule.auth

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface EsiApi {
    @Headers("Accept: application/json")
    @GET("latest/characters/{characterId}/")
    suspend fun fetchCharacter(@Path("characterId") characterId: Long): EsiCharacterDto

    @Headers("Accept: application/json")
    @GET("latest/corporations/{corporationId}/")
    suspend fun fetchCorporation(@Path("corporationId") corporationId: Int): EsiOrganizationDto

    @Headers("Accept: application/json")
    @GET("latest/alliances/{allianceId}/")
    suspend fun fetchAlliance(@Path("allianceId") allianceId: Int): EsiOrganizationDto
}
