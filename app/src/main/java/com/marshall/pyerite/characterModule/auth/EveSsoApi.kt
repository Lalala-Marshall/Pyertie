package com.marshall.pyerite.characterModule.auth

import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface EveSsoApi {
    @GET
    suspend fun fetchMetadata(@Url url: String): EveSsoMetadataDto

    @FormUrlEncoded
    @POST
    suspend fun postToken(
        @Url url: String,
        @FieldMap fields: Map<String, String>,
    ): EveSsoTokenDto
}
