package com.marshall.pyerite.eveAuthModule.sso

import com.marshall.pyerite.eveAuthModule.model.EveJWTsDto
import com.marshall.pyerite.eveAuthModule.model.EveSsoMetadataDto
import com.marshall.pyerite.eveAuthModule.model.EveSsoTokenDto
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

internal interface EveSsoApi {
    @GET
    suspend fun fetchMetadata(@Url url: String): EveSsoMetadataDto

    @GET
    suspend fun fetchJWTs(@Url url: String): EveJWTsDto

    @FormUrlEncoded
    @POST
    suspend fun postToken(
        @Url url: String,
        @FieldMap fields: Map<String, String>,
    ): EveSsoTokenDto

    @FormUrlEncoded
    @POST
    suspend fun revokeToken(
        @Url url: String,
        @FieldMap fields: Map<String, String>,
    )
}
