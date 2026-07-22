package com.marshall.pyerite.charactersListModule.auth

import com.marshall.pyerite.data.network.JsonScopeListSerializer
import com.marshall.pyerite.data.network.JsonStringOrArraySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Persisted SSO token bundle — auth package only; never expose outside [EveTokenManager]. */
@Serializable
internal data class EveTokenSet(
    val characterId: Long,
    val characterName: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = EveSsoConfig.TOKEN_TYPE_BEARER,
    val expiresAtEpochMs: Long,
    val scopes: List<String> = emptyList(),
) {
    /**
     * Parsed once per in-memory instance (not persisted — body property with no backing field
     * for serialization). [copy] / reload from store creates a new instance and re-parses once.
     */
    val grantedScopes: Set<EveSsoScope> by lazy { EveSsoScope.parseGranted(scopes) }
}

/** Non-secret session identity for restore / UI wiring. */
internal data class EveStoredSession(
    val characterId: Long,
    val characterName: String,
)

internal fun normalizeTokenType(raw: String?): String =
    raw?.takeIf { it.isNotBlank() } ?: EveSsoConfig.TOKEN_TYPE_BEARER

internal fun EveTokenSet.authorizationHeader(): String =
    "${normalizeTokenType(tokenType)} $accessToken"

internal fun EveTokenSet.toStoredSession(): EveStoredSession =
    EveStoredSession(characterId = characterId, characterName = characterName)

@Serializable
internal data class EveSsoMetadataDto(
    @SerialName("authorization_endpoint") val authorizationEndpoint: String,
    @SerialName("token_endpoint") val tokenEndpoint: String,
    @SerialName("jwks_uri") val jwksUri: String? = null,
    @SerialName("revocation_endpoint") val revocationEndpoint: String? = null,
)

@Serializable
internal data class EveJWTsDto(
    val keys: List<EveJwkDto> = emptyList(),
)

@Serializable
internal data class EveJwkDto(
    val kty: String = "",
    val kid: String = "",
    val use: String = "",
    val alg: String = "",
    val n: String = "",
    val e: String = "",
)

@Serializable
internal data class EveSsoTokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = EveSsoConfig.TOKEN_TYPE_BEARER,
    @SerialName("expires_in") val expiresInSeconds: Long =
        EveSsoConfig.DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS,
    val scope: String = "",
) {
    fun toDomain(): EveSsoTokenResponse = EveSsoTokenResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = normalizeTokenType(tokenType),
        expiresInSeconds = expiresInSeconds,
        scopes = scope.split(' ').filter { it.isNotBlank() },
    )
}

internal data class EveSsoTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
    val scopes: List<String>,
)

@Serializable
internal data class EveJwtPayloadDto(
    val sub: String = "",
    val name: String = "",
    val exp: Long = 0L,
    @Serializable(with = JsonScopeListSerializer::class)
    val scp: List<String> = emptyList(),
    @Serializable(with = JsonStringOrArraySerializer::class)
    val aud: List<String> = emptyList(),
    val iss: String = "",
)

internal data class EveJwtClaims(
    val characterId: Long,
    val characterName: String,
    val expiresAtEpochMs: Long,
    val scopes: List<String>,
    val audience: List<String>,
    val issuer: String,
)

@Serializable
internal data class EsiCharacterDto(
    val name: String,
    @SerialName("corporation_id") val corporationId: Long? = null,
    @SerialName("alliance_id") val allianceId: Long? = null,
    val birthday: String? = null,
    @SerialName("security_status") val securityStatus: Double? = null,
)

@Serializable
internal data class EveOAuthErrorDto(
    val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
)

@Serializable
internal data class EsiOrganizationDto(
    val name: String,
    val ticker: String? = null,
)

@Serializable
internal data class EsiCharacterSkillsDto(
    @SerialName("total_sp") val totalSp: Long = 0L,
    @SerialName("unallocated_sp") val unallocatedSp: Long = 0L,
)

@Serializable
internal data class EsiSkillQueueEntryDto(
    @SerialName("skill_id") val skillId: Int,
    @SerialName("finished_level") val finishedLevel: Int,
    @SerialName("queue_position") val queuePosition: Int = 0,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("finish_date") val finishDate: String? = null,
    @SerialName("training_start_sp") val trainingStartSp: Long? = null,
    @SerialName("level_end_sp") val levelEndSp: Long? = null,
    @SerialName("level_start_sp") val levelStartSp: Long? = null,
)

@Serializable
internal data class EsiCharacterLocationDto(
    @SerialName("solar_system_id") val solarSystemId: Long,
    @SerialName("station_id") val stationId: Long? = null,
    @SerialName("structure_id") val structureId: Long? = null,
)

@Serializable
internal data class EsiUniverseSystemDto(
    val name: String,
    @SerialName("security_status") val securityStatus: Double? = null,
)

@Serializable
internal data class EsiUniverseTypeDto(
    val name: String,
)

@Serializable
internal data class EsiCharacterShipDto(
    @SerialName("ship_item_id") val shipItemId: Long,
    @SerialName("ship_type_id") val shipTypeId: Int,
    @SerialName("ship_name") val shipName: String? = null,
)

@Serializable
internal data class EsiCharacterFatigueDto(
    @SerialName("jump_fatigue_expire_date") val jumpFatigueExpireDate: String? = null,
    @SerialName("last_jump_date") val lastJumpDate: String? = null,
    @SerialName("last_update_date") val lastUpdateDate: String? = null,
)

@Serializable
internal data class EsiCharacterMedalDto(
    @SerialName("medal_id") val medalId: Int,
    val title: String = "",
    val description: String = "",
    val reason: String = "",
    val date: String? = null,
    @SerialName("corporation_id") val corporationId: Long? = null,
    @SerialName("issuer_id") val issuerId: Long? = null,
)

@Serializable
internal data class EsiCharacterOnlineDto(
    val online: Boolean = false,
)

@Serializable
internal data class EsiUniverseStructureDto(
    val name: String,
    @SerialName("solar_system_id") val solarSystemId: Long? = null,
    @SerialName("type_id") val typeId: Int? = null,
)

internal data class EsiCharacterPublic(
    val characterId: Long,
    val name: String,
    val corporationId: Long?,
    val allianceId: Long?,
    val birthday: String? = null,
    val securityStatus: Double? = null,
)

internal data class EsiOrganization(
    val id: Long,
    val name: String,
    val ticker: String?,
)
