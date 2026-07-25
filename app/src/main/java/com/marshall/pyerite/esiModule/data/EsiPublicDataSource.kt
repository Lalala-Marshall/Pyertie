package com.marshall.pyerite.esiModule.data

import com.marshall.pyerite.esiModule.api.EsiAllianceApi
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.api.EsiCorporationApi
import com.marshall.pyerite.esiModule.api.EsiUniverseApi
import com.marshall.pyerite.esiModule.http.EsiConfig
import com.marshall.pyerite.esiModule.model.EsiCharacterPublic
import com.marshall.pyerite.esiModule.model.EsiOrganization
import com.marshall.pyerite.esiModule.model.EsiUniverseStationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.ConcurrentHashMap

internal class EsiPublicDataSource(
    private val characterApi: EsiCharacterApi,
    private val corporationApi: EsiCorporationApi,
    private val allianceApi: EsiAllianceApi,
    private val universeApi: EsiUniverseApi,
) {
    private val characterCache = ConcurrentHashMap<Long, EsiCharacterPublic>()
    private val corporationCache = ConcurrentHashMap<Long, EsiOrganization>()
    private val allianceCache = ConcurrentHashMap<Long, EsiOrganization>()

    suspend fun fetchCharacter(characterId: Long): EsiCharacterPublic = withContext(Dispatchers.IO) {
        characterCache[characterId]?.let { return@withContext it }
        val dto = getDto { characterApi.fetchCharacter(characterId) }
        EsiCharacterPublic(
            characterId = characterId,
            name = dto.name,
            corporationId = dto.corporationId,
            allianceId = dto.allianceId,
            birthday = dto.birthday,
            securityStatus = dto.securityStatus,
        ).also { characterCache[characterId] = it }
    }

    /** Public corp name/ticker — character detail only returns corporation_id. */
    suspend fun fetchCorporation(corporationId: Long): EsiOrganization = withContext(Dispatchers.IO) {
        corporationCache[corporationId]?.let { return@withContext it }
        val dto = getDto { corporationApi.fetchCorporation(corporationId) }
        EsiOrganization(
            id = corporationId,
            name = dto.name,
            ticker = dto.ticker?.takeIf { it.isNotBlank() },
        ).also { corporationCache[corporationId] = it }
    }

    /** Public alliance name/ticker — character detail only returns alliance_id. */
    suspend fun fetchAlliance(allianceId: Long): EsiOrganization = withContext(Dispatchers.IO) {
        allianceCache[allianceId]?.let { return@withContext it }
        val dto = getDto { allianceApi.fetchAlliance(allianceId) }
        EsiOrganization(
            id = allianceId,
            name = dto.name,
            ticker = dto.ticker?.takeIf { it.isNotBlank() },
        ).also { allianceCache[allianceId] = it }
    }

    suspend fun fetchSolarSystemName(systemId: Long): String? = withContext(Dispatchers.IO) {
        runCatching { getDto { universeApi.fetchSolarSystem(systemId) }.name }.getOrNull()
    }

    suspend fun fetchSolarSystemSecurity(systemId: Long): Double? = withContext(Dispatchers.IO) {
        runCatching { getDto { universeApi.fetchSolarSystem(systemId) }.securityStatus }.getOrNull()
    }

    /** Sun type for a system (ESI system → star → type_id). */
    suspend fun fetchSolarSystemStarTypeId(systemId: Long): Int? = withContext(Dispatchers.IO) {
        runCatching {
            val starId = getDto { universeApi.fetchSolarSystem(systemId) }.starId
                ?: return@runCatching null
            getDto { universeApi.fetchStar(starId) }.typeId
        }.getOrNull()
    }

    suspend fun fetchStation(stationId: Long): EsiUniverseStationDto? = withContext(Dispatchers.IO) {
        runCatching { getDto { universeApi.fetchStation(stationId) } }.getOrNull()
    }

    suspend fun fetchTypeName(typeId: Int): String? = withContext(Dispatchers.IO) {
        runCatching { getDto { universeApi.fetchUniverseType(typeId) }.name }.getOrNull()
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

internal fun portraitUrl(characterId: Long, size: Int = EsiConfig.Image.PORTRAIT_SIZE): String =
    "${EsiConfig.IMAGE_BASE_URL}characters/$characterId/portrait?size=$size"

internal fun corporationLogoUrl(corporationId: Long, size: Int = EsiConfig.Image.LOGO_SIZE): String =
    "${EsiConfig.IMAGE_BASE_URL}corporations/$corporationId/logo?size=$size"

internal fun allianceLogoUrl(allianceId: Long, size: Int = EsiConfig.Image.LOGO_SIZE): String =
    "${EsiConfig.IMAGE_BASE_URL}alliances/$allianceId/logo?size=$size"
