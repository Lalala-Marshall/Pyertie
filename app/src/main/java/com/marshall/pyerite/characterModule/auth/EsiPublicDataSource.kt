package com.marshall.pyerite.characterModule.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.ConcurrentHashMap

internal class EsiPublicDataSource(
    private val api: EsiApi,
) {
    private val characterCache = ConcurrentHashMap<Long, EsiCharacterPublic>()
    private val corporationCache = ConcurrentHashMap<Long, EsiOrganization>()
    private val allianceCache = ConcurrentHashMap<Long, EsiOrganization>()

    suspend fun fetchCharacter(characterId: Long): EsiCharacterPublic = withContext(Dispatchers.IO) {
        characterCache[characterId]?.let { return@withContext it }
        val dto = getDto { api.fetchCharacter(characterId) }
        EsiCharacterPublic(
            characterId = characterId,
            name = dto.name,
            corporationId = dto.corporationId,
            allianceId = dto.allianceId,
            securityStatus = dto.securityStatus,
        ).also { characterCache[characterId] = it }
    }

    /** Public corp name/ticker — character detail only returns corporation_id. */
    suspend fun fetchCorporation(corporationId: Long): EsiOrganization = withContext(Dispatchers.IO) {
        corporationCache[corporationId]?.let { return@withContext it }
        val dto = getDto { api.fetchCorporation(corporationId) }
        EsiOrganization(
            id = corporationId,
            name = dto.name,
            ticker = dto.ticker?.takeIf { it.isNotBlank() },
        ).also { corporationCache[corporationId] = it }
    }

    /** Public alliance name/ticker — character detail only returns alliance_id. */
    suspend fun fetchAlliance(allianceId: Long): EsiOrganization = withContext(Dispatchers.IO) {
        allianceCache[allianceId]?.let { return@withContext it }
        val dto = getDto { api.fetchAlliance(allianceId) }
        EsiOrganization(
            id = allianceId,
            name = dto.name,
            ticker = dto.ticker?.takeIf { it.isNotBlank() },
        ).also { allianceCache[allianceId] = it }
    }

    suspend fun fetchSolarSystemName(systemId: Long): String? = withContext(Dispatchers.IO) {
        runCatching { getDto { api.fetchSolarSystem(systemId) }.name }.getOrNull()
    }

    suspend fun fetchSolarSystemSecurity(systemId: Long): Double? = withContext(Dispatchers.IO) {
        runCatching { getDto { api.fetchSolarSystem(systemId) }.securityStatus }.getOrNull()
    }

    suspend fun fetchTypeName(typeId: Int): String? = withContext(Dispatchers.IO) {
        runCatching { getDto { api.fetchUniverseType(typeId) }.name }.getOrNull()
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

internal fun portraitUrl(characterId: Long, size: Int = EveSsoConfig.Image.PORTRAIT_SIZE): String =
    "${EveSsoConfig.IMAGE_BASE_URL}characters/$characterId/portrait?size=$size"

internal fun corporationLogoUrl(corporationId: Long, size: Int = EveSsoConfig.Image.LOGO_SIZE): String =
    "${EveSsoConfig.IMAGE_BASE_URL}corporations/$corporationId/logo?size=$size"

internal fun allianceLogoUrl(allianceId: Long, size: Int = EveSsoConfig.Image.LOGO_SIZE): String =
    "${EveSsoConfig.IMAGE_BASE_URL}alliances/$allianceId/logo?size=$size"
