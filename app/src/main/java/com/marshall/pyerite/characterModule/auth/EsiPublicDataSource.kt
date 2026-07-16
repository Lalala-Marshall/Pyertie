package com.marshall.pyerite.characterModule.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class EsiPublicDataSource(
    private val api: EsiApi,
) {
    suspend fun fetchCharacter(characterId: Long): EsiCharacterPublic = withContext(Dispatchers.IO) {
        val dto = getDto { api.fetchCharacter(characterId) }
        EsiCharacterPublic(
            characterId = characterId,
            name = dto.name,
            corporationId = dto.corporationId,
            allianceId = dto.allianceId,
            securityStatus = dto.securityStatus,
        )
    }

    suspend fun fetchCorporation(corporationId: Int): EsiOrganization = withContext(Dispatchers.IO) {
        val dto = getDto { api.fetchCorporation(corporationId) }
        EsiOrganization(
            id = corporationId,
            name = dto.name,
            ticker = dto.ticker?.takeIf { it.isNotBlank() },
        )
    }

    suspend fun fetchAlliance(allianceId: Int): EsiOrganization = withContext(Dispatchers.IO) {
        val dto = getDto { api.fetchAlliance(allianceId) }
        EsiOrganization(
            id = allianceId,
            name = dto.name,
            ticker = dto.ticker?.takeIf { it.isNotBlank() },
        )
    }

    private suspend fun <T> getDto(call: suspend () -> T): T {
        return try {
            call()
        } catch (httpError: HttpException) {
            val raw = httpError.response()?.errorBody()?.string().orEmpty()
            error("ESI HTTP ${httpError.code()}: $raw")
        }
    }
}

fun portraitUrl(characterId: Long, size: Int = 128): String =
    "${EveSsoConfig.IMAGE_BASE_URL}characters/$characterId/portrait?size=$size"

fun corporationLogoUrl(corporationId: Int, size: Int = 64): String =
    "${EveSsoConfig.IMAGE_BASE_URL}corporations/$corporationId/logo?size=$size"

fun allianceLogoUrl(allianceId: Int, size: Int = 64): String =
    "${EveSsoConfig.IMAGE_BASE_URL}alliances/$allianceId/logo?size=$size"
