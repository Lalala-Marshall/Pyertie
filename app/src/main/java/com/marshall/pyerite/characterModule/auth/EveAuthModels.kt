package com.marshall.pyerite.characterModule.auth

import com.marshall.pyerite.data.network.JsonScopeListSerializer
import com.marshall.pyerite.data.network.JsonStringOrArraySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EveTokenSet(
    val characterId: Long,
    val characterName: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresAtEpochMs: Long,
    val scopes: List<String> = emptyList(),
)

@Serializable
data class EveSsoMetadataDto(
    @SerialName("authorization_endpoint") val authorizationEndpoint: String,
    @SerialName("token_endpoint") val tokenEndpoint: String,
)

@Serializable
data class EveSsoTokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("expires_in") val expiresInSeconds: Long = 1_200L,
    val scope: String = "",
) {
    fun toDomain(): EveSsoTokenResponse = EveSsoTokenResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        expiresInSeconds = expiresInSeconds,
        scopes = scope.split(' ').filter { it.isNotBlank() },
    )
}

data class EveSsoTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
    val scopes: List<String>,
)

@Serializable
data class EveJwtPayloadDto(
    val sub: String = "",
    val name: String = "",
    val exp: Long = 0L,
    @Serializable(with = JsonScopeListSerializer::class)
    val scp: List<String> = emptyList(),
    @Serializable(with = JsonStringOrArraySerializer::class)
    val aud: List<String> = emptyList(),
    val iss: String = "",
)

data class EveJwtClaims(
    val characterId: Long,
    val characterName: String,
    val expiresAtEpochMs: Long,
    val scopes: List<String>,
    val audience: List<String>,
    val issuer: String,
)

@Serializable
data class EsiCharacterDto(
    val name: String,
    @SerialName("corporation_id") val corporationId: Int? = null,
    @SerialName("alliance_id") val allianceId: Int? = null,
    @SerialName("security_status") val securityStatus: Double? = null,
)

@Serializable
data class EsiOrganizationDto(
    val name: String,
    val ticker: String? = null,
)

data class EsiCharacterPublic(
    val characterId: Long,
    val name: String,
    val corporationId: Int?,
    val allianceId: Int?,
    val securityStatus: Double?,
)

data class EsiOrganization(
    val id: Int,
    val name: String,
    val ticker: String?,
)
