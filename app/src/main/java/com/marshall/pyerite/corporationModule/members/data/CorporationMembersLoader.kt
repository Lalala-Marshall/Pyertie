package com.marshall.pyerite.corporationModule.members.data

import com.marshall.pyerite.corporationModule.members.model.CorporationMember
import com.marshall.pyerite.corporationModule.members.model.CorporationMembersAccessException
import com.marshall.pyerite.corporationModule.members.model.CorporationMembersConfig
import com.marshall.pyerite.corporationModule.members.model.CorporationMembersSnapshot
import com.marshall.pyerite.esiModule.api.EsiCorporationApi
import com.marshall.pyerite.esiModule.api.EsiUniverseApi
import com.marshall.pyerite.esiModule.data.EsiPublicDataSource
import com.marshall.pyerite.esiModule.data.portraitUrl
import com.marshall.pyerite.esiModule.model.EsiCorporationMemberTrackingDto
import com.marshall.pyerite.esiModule.model.EsiHttpStatus
import com.marshall.pyerite.esiModule.model.EsiPagedQuery
import com.marshall.pyerite.eveAuthModule.model.EveSsoScope
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.sdeModule.room.RoomProvider
import com.marshall.pyerite.sdeModule.room.map.MapDao
import com.marshall.pyerite.sdeModule.room.map.SolarSystemLocationRow
import com.marshall.pyerite.sdeModule.room.map.SolarSystemLookupRow
import com.marshall.pyerite.sdeModule.room.type.TypeDisplayIconRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

internal class CorporationMembersLoader(
    private val tokenManager: EveTokenManager,
    private val corporationApi: EsiCorporationApi,
    private val universeApi: EsiUniverseApi,
    private val publicEsi: EsiPublicDataSource,
    private val roomProvider: RoomProvider,
) {
    suspend fun loadMembers(characterId: Long): CorporationMembersSnapshot =
        withContext(Dispatchers.IO) {
            requireTrackMembersScope(characterId)
            val corporationId = resolveCorporationId(characterId)
            val tracking = fetchMemberTracking(characterId, corporationId)
            val names = loadNames(tracking.map { it.characterId })
            val ships = loadShips(tracking.mapNotNull { it.shipTypeId })
            val locations = resolveLocations(
                characterId = characterId,
                locationIds = tracking.mapNotNull { it.locationId },
            )
            val members = tracking.map { dto ->
                val ship = dto.shipTypeId?.let { ships[it] }
                val location = dto.locationId?.let { locations[it] }
                CorporationMember(
                    characterId = dto.characterId,
                    name = names[dto.characterId].orEmpty(),
                    portraitUrl = portraitUrl(dto.characterId),
                    shipZhName = ship?.zhName,
                    shipEnName = ship?.enName,
                    shipName = ship?.name,
                    shipIconFilename = ship?.iconFilename,
                    systemZhName = location?.systemZhName,
                    systemEnName = location?.systemEnName,
                    systemName = location?.systemName,
                    systemSecurityStatus = location?.securityStatus,
                )
            }
            CorporationMembersSnapshot(members = members)
        }

    private fun requireTrackMembersScope(characterId: Long) {
        val granted = tokenManager.grantedScopes(characterId)
        if (EveSsoScope.CORPORATIONS_TRACK_MEMBERS !in granted) {
            throw CorporationMembersAccessException()
        }
    }

    private suspend fun resolveCorporationId(characterId: Long): Long {
        val corporationId = publicEsi.fetchCharacter(characterId).corporationId
        if (corporationId == null || corporationId <= 0L) {
            throw CorporationMembersAccessException()
        }
        return corporationId
    }

    private suspend fun fetchMemberTracking(
        characterId: Long,
        corporationId: Long,
    ): List<EsiCorporationMemberTrackingDto> {
        val byId = LinkedHashMap<Long, EsiCorporationMemberTrackingDto>()
        var page = CorporationMembersConfig.FIRST_PAGE
        var totalPages = CorporationMembersConfig.FIRST_PAGE
        while (page <= totalPages && page <= CorporationMembersConfig.MEMBER_TRACKING_MAX_PAGES) {
            val response = fetchMemberTrackingPage(characterId, corporationId, page)
            if (!response.isSuccessful) {
                if (page > CorporationMembersConfig.FIRST_PAGE &&
                    response.code() == EsiHttpStatus.NOT_FOUND
                ) {
                    break
                }
                throw mapPagedError(response)
            }
            val chunk = response.body().orEmpty()
            chunk.forEach { entry -> byId[entry.characterId] = entry }
            totalPages = resolveTotalPages(
                headerPages = response.headers()[EsiPagedQuery.PAGES_HEADER]?.toIntOrNull(),
                chunkSize = chunk.size,
                page = page,
            )
            page++
        }
        return byId.values.toList()
    }

    private suspend fun fetchMemberTrackingPage(
        characterId: Long,
        corporationId: Long,
        page: Int,
    ): Response<List<EsiCorporationMemberTrackingDto>> {
        return tokenManager.executeWithAuthRetry(characterId) { auth ->
            val response = corporationApi.fetchMemberTracking(
                corporationId = corporationId,
                authorization = auth,
                page = page,
            )
            if (response.code() == EsiHttpStatus.UNAUTHORIZED) {
                throw HttpException(response)
            }
            response
        }
    }

    private fun resolveTotalPages(
        headerPages: Int?,
        chunkSize: Int,
        page: Int,
    ): Int = when {
        headerPages != null -> headerPages
        chunkSize < CorporationMembersConfig.MEMBER_TRACKING_PAGE_SIZE -> page
        else -> page + 1
    }

    private fun <T> mapPagedError(response: Response<T>): Throwable {
        if (response.code() == EsiHttpStatus.FORBIDDEN) {
            return CorporationMembersAccessException()
        }
        return HttpException(response)
    }

    private suspend fun loadNames(characterIds: List<Long>): Map<Long, String> {
        if (characterIds.isEmpty()) return emptyMap()
        return publicEsi.fetchUniverseNames(characterIds).associate { it.id to it.name }
    }

    private suspend fun loadShips(typeIds: List<Int>): Map<Int, TypeDisplayIconRow> {
        val distinct = typeIds.filter { it > 0 }.distinct()
        if (distinct.isEmpty()) return emptyMap()
        val dao = roomProvider.getDatabase().sdeTypeDao()
        val fromSde = distinct.chunked(CorporationMembersConfig.QUERY_CHUNK).flatMap { chunk ->
            runCatching { dao.getTypesForDisplay(chunk) }.getOrDefault(emptyList())
        }.associateBy { it.id }
        val missing = distinct.filter { it !in fromSde }
        if (missing.isEmpty()) return fromSde
        val fromEsi = missing.mapNotNull { typeId ->
            val name = publicEsi.fetchTypeName(typeId)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            typeId to TypeDisplayIconRow(
                id = typeId,
                name = name,
                zhName = null,
                enName = null,
                iconFilename = null,
            )
        }
        return fromSde + fromEsi
    }

    private suspend fun resolveLocations(
        characterId: Long,
        locationIds: List<Long>,
    ): Map<Long, ResolvedSystem> {
        val ids = locationIds.filter { it > 0L }.distinct()
        if (ids.isEmpty()) return emptyMap()
        val dao = roomProvider.getDatabase().mapDao()
        val systems = mutableMapOf<Long, ResolvedSystem>()
        rememberSystems(dao, ids, systems)

        val resolved = mutableMapOf<Long, ResolvedSystem>()
        val remaining = mutableListOf<Long>()
        for (id in ids) {
            val direct = systems[id]
            if (direct != null) {
                resolved[id] = direct
            } else {
                remaining += id
            }
        }
        if (remaining.isEmpty()) return resolved

        val stations = remaining.chunked(CorporationMembersConfig.QUERY_CHUNK).flatMap { chunk ->
            runCatching { dao.getStations(chunk) }.getOrDefault(emptyList())
        }.associateBy { it.stationId }
        val stationSystemIds = stations.values.mapNotNull { it.solarSystemId?.toLong() }
        rememberSystems(dao, stationSystemIds, systems)

        val unresolved = mutableListOf<Long>()
        for (id in remaining) {
            val station = stations[id]
            if (station == null) {
                unresolved += id
                continue
            }
            val system = station.solarSystemId?.toLong()?.let { systemFor(dao, it, systems) }
            if (system != null) {
                resolved[id] = system
            }
        }

        for (id in unresolved) {
            val systemId = solarSystemIdForUnresolved(characterId, id) ?: continue
            val system = systemFor(dao, systemId, systems) ?: continue
            resolved[id] = system
        }
        return resolved
    }

    private suspend fun rememberSystems(
        dao: MapDao,
        solarSystemIds: List<Long>,
        systems: MutableMap<Long, ResolvedSystem>,
    ) {
        val missing = solarSystemIds.filter { it > 0L && it !in systems }.distinct()
        if (missing.isEmpty()) return
        missing.chunked(CorporationMembersConfig.QUERY_CHUNK).forEach { chunk ->
            val rows = runCatching { dao.getSolarSystemLocations(chunk) }.getOrDefault(emptyList())
            rows.forEach { row ->
                row.toResolved()?.let { systems[row.solarSystemId] = it }
            }
        }
    }

    private suspend fun systemFor(
        dao: MapDao,
        solarSystemId: Long,
        systems: MutableMap<Long, ResolvedSystem>,
    ): ResolvedSystem? {
        systems[solarSystemId]?.let { return it }
        val fromSde = runCatching { dao.getSolarSystemLocation(solarSystemId) }.getOrNull()
            ?.toResolved()
        val resolved = fromSde ?: systemFromEsi(solarSystemId)
        if (resolved != null) {
            systems[solarSystemId] = resolved
        }
        return resolved
    }

    private fun SolarSystemLookupRow.toResolved(): ResolvedSystem? = toResolvedSystem(
        systemZhName = systemZhName,
        systemEnName = systemEnName,
        systemName = systemName,
        securityStatus = securityStatus,
    )

    private fun SolarSystemLocationRow.toResolved(): ResolvedSystem? =
        toResolvedSystem(
            systemZhName = systemZhName,
            systemEnName = systemEnName,
            systemName = systemName,
            securityStatus = securityStatus,
        )

    private suspend fun solarSystemIdForUnresolved(
        characterId: Long,
        locationId: Long,
    ): Long? {
        if (locationId >= CorporationMembersConfig.PLAYER_STRUCTURE_ID_MIN) {
            return runCatching {
                tokenManager.executeWithAuthRetry(characterId) { auth ->
                    universeApi.fetchStructure(locationId, auth)
                }
            }.getOrNull()?.solarSystemId
        }
        return publicEsi.fetchStation(locationId)?.systemId ?: locationId
    }

    private suspend fun systemFromEsi(solarSystemId: Long): ResolvedSystem? {
        val name = publicEsi.fetchSolarSystemName(solarSystemId)?.takeIf { it.isNotBlank() }
        val security = publicEsi.fetchSolarSystemSecurity(solarSystemId)
        return toResolvedSystem(
            systemZhName = null,
            systemEnName = null,
            systemName = name,
            securityStatus = security,
        )
    }

    private fun toResolvedSystem(
        systemZhName: String?,
        systemEnName: String?,
        systemName: String?,
        securityStatus: Double?,
    ): ResolvedSystem? {
        val hasName = !systemZhName.isNullOrBlank() ||
            !systemEnName.isNullOrBlank() ||
            !systemName.isNullOrBlank()
        if (!hasName && securityStatus == null) return null
        return ResolvedSystem(
            systemZhName = systemZhName,
            systemEnName = systemEnName,
            systemName = systemName,
            securityStatus = securityStatus,
        )
    }

    private data class ResolvedSystem(
        val systemZhName: String?,
        val systemEnName: String?,
        val systemName: String?,
        val securityStatus: Double?,
    )
}
