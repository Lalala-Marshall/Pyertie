package com.marshall.pyerite.corporationModule.structures.data

import com.marshall.pyerite.corporationModule.structures.model.CorporationStructure
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureService
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureServiceStatus
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureState
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructuresAccessException
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructuresConfig
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructuresSnapshot
import com.marshall.pyerite.esiModule.api.EsiCorporationApi
import com.marshall.pyerite.esiModule.data.EsiPublicDataSource
import com.marshall.pyerite.esiModule.model.EsiCorporationStructureDto
import com.marshall.pyerite.esiModule.model.EsiHttpStatus
import com.marshall.pyerite.esiModule.model.EsiPagedQuery
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.model.EveSsoScope
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.sdeModule.room.RoomProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

internal class CorporationStructuresLoader(
    private val tokenManager: EveTokenManager,
    private val corporationApi: EsiCorporationApi,
    private val publicEsi: EsiPublicDataSource,
    private val roomProvider: RoomProvider,
) {
    suspend fun loadStructures(characterId: Long): CorporationStructuresSnapshot =
        withContext(Dispatchers.IO) {
            requireStructuresScope(characterId)
            val corporationId = resolveCorporationId(characterId)
            val structures = fetchStructures(characterId, corporationId)
            val icons = loadIcons(structures.map { it.typeId })
            val systems = loadSystems(structures.map { it.systemId })
            CorporationStructuresSnapshot(
                structures = structures.map { dto ->
                    val system = systems[dto.systemId]
                    CorporationStructure(
                        structureId = dto.structureId,
                        name = dto.name.orEmpty(),
                        iconFilename = icons[dto.typeId],
                        state = CorporationStructureState.fromApi(dto.state),
                        fuelExpiresAtMillis = parseEsiDateMillis(dto.fuelExpires),
                        services = dto.services.orEmpty()
                            .filter { it.name.isNotBlank() }
                            .map { service ->
                                CorporationStructureService(
                                    name = service.name,
                                    status = CorporationStructureServiceStatus.fromApi(service.state),
                                )
                            },
                        systemId = dto.systemId,
                        systemZhName = system?.systemZhName,
                        systemEnName = system?.systemEnName,
                        systemName = system?.systemName,
                        regionZhName = system?.regionZhName,
                        regionEnName = system?.regionEnName,
                        regionName = system?.regionName,
                        systemSecurityStatus = system?.securityStatus,
                    )
                },
            )
        }

    private fun requireStructuresScope(characterId: Long) {
        val granted = tokenManager.grantedScopes(characterId)
        if (EveSsoScope.CORPORATIONS_READ_STRUCTURES !in granted) {
            throw CorporationStructuresAccessException()
        }
    }

    private suspend fun resolveCorporationId(characterId: Long): Long {
        val corporationId = publicEsi.fetchCharacter(characterId).corporationId
        if (corporationId == null || corporationId <= 0L) {
            throw CorporationStructuresAccessException()
        }
        return corporationId
    }

    private suspend fun fetchStructures(
        characterId: Long,
        corporationId: Long,
    ): List<EsiCorporationStructureDto> {
        val byId = LinkedHashMap<Long, EsiCorporationStructureDto>()
        var page = CorporationStructuresConfig.FIRST_PAGE
        var totalPages = CorporationStructuresConfig.FIRST_PAGE
        while (page <= totalPages && page <= CorporationStructuresConfig.STRUCTURES_MAX_PAGES) {
            val response = fetchStructuresPage(characterId, corporationId, page)
            if (!response.isSuccessful) {
                if (page > CorporationStructuresConfig.FIRST_PAGE &&
                    response.code() == EsiHttpStatus.NOT_FOUND
                ) {
                    break
                }
                throw mapPagedError(response)
            }
            val chunk = response.body().orEmpty()
            chunk.forEach { entry -> byId[entry.structureId] = entry }
            totalPages = resolveTotalPages(
                headerPages = response.headers()[EsiPagedQuery.PAGES_HEADER]?.toIntOrNull(),
                chunkSize = chunk.size,
                page = page,
            )
            page++
        }
        return byId.values.toList()
    }

    private suspend fun fetchStructuresPage(
        characterId: Long,
        corporationId: Long,
        page: Int,
    ): Response<List<EsiCorporationStructureDto>> {
        return tokenManager.executeWithAuthRetry(characterId) { auth ->
            val response = corporationApi.fetchStructures(
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
        chunkSize < CorporationStructuresConfig.STRUCTURES_PAGE_SIZE -> page
        else -> page + 1
    }

    private fun <T> mapPagedError(response: Response<T>): Throwable {
        if (response.code() == EsiHttpStatus.FORBIDDEN) {
            return CorporationStructuresAccessException()
        }
        return HttpException(response)
    }

    private suspend fun loadIcons(typeIds: List<Int>): Map<Int, String?> {
        val distinct = typeIds.filter { it > 0 }.distinct()
        if (distinct.isEmpty()) return emptyMap()
        val dao = roomProvider.getDatabase().sdeTypeDao()
        return distinct.chunked(CorporationStructuresConfig.QUERY_CHUNK).flatMap { chunk ->
            runCatching { dao.getTypesForDisplay(chunk) }.getOrDefault(emptyList())
        }.associate { it.id to it.iconFilename }
    }

    private suspend fun loadSystems(systemIds: List<Long>): Map<Long, ResolvedSystem> {
        val ids = systemIds.filter { it > 0L }.distinct()
        if (ids.isEmpty()) return emptyMap()
        val dao = roomProvider.getDatabase().mapDao()
        val fromSde = ids.chunked(CorporationStructuresConfig.QUERY_CHUNK).flatMap { chunk ->
            runCatching { dao.getSolarSystemRegionLocations(chunk) }.getOrDefault(emptyList())
        }.mapNotNull { row ->
            val resolved = ResolvedSystem(
                systemZhName = row.systemZhName,
                systemEnName = row.systemEnName,
                systemName = row.systemName,
                regionZhName = row.regionZhName,
                regionEnName = row.regionEnName,
                regionName = row.regionName,
                securityStatus = row.securityStatus,
            )
            if (!resolved.hasContent()) return@mapNotNull null
            row.solarSystemId to resolved
        }.toMap()
        val missing = ids.filter { it !in fromSde }
        if (missing.isEmpty()) return fromSde
        val fromEsi = missing.mapNotNull { systemId ->
            val name = publicEsi.fetchSolarSystemName(systemId)?.takeIf { it.isNotBlank() }
            val security = publicEsi.fetchSolarSystemSecurity(systemId)
            val resolved = ResolvedSystem(
                systemZhName = null,
                systemEnName = null,
                systemName = name,
                regionZhName = null,
                regionEnName = null,
                regionName = null,
                securityStatus = security,
            )
            if (!resolved.hasContent()) return@mapNotNull null
            systemId to resolved
        }.toMap()
        return fromSde + fromEsi
    }

    private data class ResolvedSystem(
        val systemZhName: String?,
        val systemEnName: String?,
        val systemName: String?,
        val regionZhName: String?,
        val regionEnName: String?,
        val regionName: String?,
        val securityStatus: Double?,
    ) {
        fun hasContent(): Boolean =
            !systemZhName.isNullOrBlank() ||
                !systemEnName.isNullOrBlank() ||
                !systemName.isNullOrBlank() ||
                securityStatus != null
    }
}
