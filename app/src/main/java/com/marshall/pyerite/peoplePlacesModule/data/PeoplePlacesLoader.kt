package com.marshall.pyerite.peoplePlacesModule.data

import com.marshall.pyerite.esiModule.api.EsiAllianceApi
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.api.EsiCorporationApi
import com.marshall.pyerite.esiModule.api.EsiUniverseApi
import com.marshall.pyerite.esiModule.data.EsiPublicDataSource
import com.marshall.pyerite.esiModule.data.allianceLogoUrl
import com.marshall.pyerite.esiModule.data.corporationLogoUrl
import com.marshall.pyerite.esiModule.data.portraitUrl
import com.marshall.pyerite.esiModule.http.EsiConfig
import com.marshall.pyerite.esiModule.model.EsiContactDto
import com.marshall.pyerite.esiModule.model.EsiSearchQuery
import com.marshall.pyerite.esiModule.model.EsiSearchResultDto
import com.marshall.pyerite.esiModule.model.EsiUniverseNameCategory
import com.marshall.pyerite.eveAuthModule.model.EveSsoScope
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.localizedName
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesBuildingType
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCategory
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesConfig
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesResult
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesSearchOutcome
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesSearchRequest
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesStanding
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesViewer
import com.marshall.pyerite.sdeModule.room.RoomProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

internal class PeoplePlacesLoader(
    private val publicEsi: EsiPublicDataSource,
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
    private val corporationApi: EsiCorporationApi,
    private val allianceApi: EsiAllianceApi,
    private val universeApi: EsiUniverseApi,
    private val roomProvider: RoomProvider,
    private val localeController: LocaleController,
) {
    private val contactsByCharacterId = ConcurrentHashMap<Long, ViewerContacts>()

    suspend fun loadViewer(characterId: Long): PeoplePlacesViewer = withContext(Dispatchers.IO) {
        val public = publicEsi.fetchCharacter(characterId)
        val corporationId = public.corporationId
        val corporation = corporationId?.let { id ->
            runCatching { publicEsi.fetchCorporation(id) }.getOrNull()
        }
        val allianceId = public.allianceId ?: corporation?.allianceId
        val contacts = loadViewerContacts(characterId, corporationId, allianceId)
        contactsByCharacterId[characterId] = contacts
        PeoplePlacesViewer(
            characterId = characterId,
            corporationId = corporationId,
            allianceId = allianceId,
        )
    }

    suspend fun search(request: PeoplePlacesSearchRequest): PeoplePlacesSearchOutcome =
        withContext(Dispatchers.IO) {
            when (request.category) {
                PeoplePlacesCategory.CHARACTER -> searchCharacters(request)
                PeoplePlacesCategory.CORPORATION -> searchCorporations(request)
                PeoplePlacesCategory.ALLIANCE -> searchAlliances(request)
                PeoplePlacesCategory.STRUCTURE -> searchBuildings(request)
            }
        }

    private suspend fun searchCharacters(
        request: PeoplePlacesSearchRequest,
    ): PeoplePlacesSearchOutcome {
        val filters = request.characterFilters
        val ids = resolveSearchIds(
            request = request,
            exactMatch = filters.exactMatch,
            categories = listOf(EsiUniverseNameCategory.CHARACTER),
        )
        val named = namedIds(ids, EsiUniverseNameCategory.CHARACTER, request.query)
        val needsOrg = filters.corporationQuery.isNotBlank() ||
            filters.allianceQuery.isNotBlank() ||
            filters.myCorporationOnly ||
            filters.myAllianceOnly
        val candidates = if (needsOrg) named else named.take(PeoplePlacesConfig.DISPLAY_LIMIT)
        val hydrated = hydrateAll(candidates) { hydrateCharacter(it) }
        val resolvedCorpId = resolveOrgFilterId(filters.corporationQuery, OrgFilterKind.CORPORATION)
        val resolvedAllianceId = resolveOrgFilterId(filters.allianceQuery, OrgFilterKind.ALLIANCE)
        val filtered = hydrated.filter { result ->
            matchesCharacterFilters(result, filters, request.viewer, resolvedCorpId, resolvedAllianceId)
        }
        val displayed = filtered.take(PeoplePlacesConfig.DISPLAY_LIMIT).map { result ->
            result.copy(
                standing = resolveStanding(
                    viewer = request.viewer,
                    kind = PeoplePlacesCategory.CHARACTER,
                    resultId = result.id,
                    resultCorporationId = result.corporationId,
                    resultAllianceId = result.allianceId,
                ),
            )
        }
        val total = if (needsOrg) filtered.size else named.size
        return PeoplePlacesSearchOutcome(displayed, displayed.size, total)
    }

    private suspend fun searchCorporations(
        request: PeoplePlacesSearchRequest,
    ): PeoplePlacesSearchOutcome {
        val filters = request.corporationFilters
        val ids = resolveSearchIds(
            request = request,
            exactMatch = filters.exactMatch,
            categories = listOf(EsiUniverseNameCategory.CORPORATION),
        )
        val named = namedIds(ids, EsiUniverseNameCategory.CORPORATION, request.query)
        val needsOrg = filters.allianceQuery.isNotBlank() || filters.myAllianceOnly
        val candidates = if (needsOrg) named else named.take(PeoplePlacesConfig.DISPLAY_LIMIT)
        val hydrated = hydrateAll(candidates) { hydrateCorporation(it) }
        val resolvedAllianceId = resolveOrgFilterId(filters.allianceQuery, OrgFilterKind.ALLIANCE)
        val filtered = hydrated.filter { result ->
            matchesCorporationFilters(result, filters, request.viewer, resolvedAllianceId)
        }
        val displayed = filtered.take(PeoplePlacesConfig.DISPLAY_LIMIT).map { result ->
            result.copy(
                standing = resolveStanding(
                    viewer = request.viewer,
                    kind = PeoplePlacesCategory.CORPORATION,
                    resultId = result.id,
                    resultCorporationId = result.id,
                    resultAllianceId = result.allianceId,
                ),
            )
        }
        val total = if (needsOrg) filtered.size else named.size
        return PeoplePlacesSearchOutcome(displayed, displayed.size, total)
    }

    private suspend fun searchAlliances(
        request: PeoplePlacesSearchRequest,
    ): PeoplePlacesSearchOutcome {
        val ids = resolveSearchIds(
            request = request,
            exactMatch = request.allianceExactMatch,
            categories = listOf(EsiUniverseNameCategory.ALLIANCE),
        )
        val named = namedIds(ids, EsiUniverseNameCategory.ALLIANCE, request.query)
        val displayed = named.take(PeoplePlacesConfig.DISPLAY_LIMIT).map { namedId ->
            PeoplePlacesResult.Alliance(
                id = namedId.id,
                name = namedId.name,
                logoUrl = allianceLogoUrl(namedId.id),
                standing = resolveStanding(
                    viewer = request.viewer,
                    kind = PeoplePlacesCategory.ALLIANCE,
                    resultId = namedId.id,
                    resultCorporationId = null,
                    resultAllianceId = namedId.id,
                ),
            )
        }
        return PeoplePlacesSearchOutcome(displayed, displayed.size, named.size)
    }

    private suspend fun searchBuildings(
        request: PeoplePlacesSearchRequest,
    ): PeoplePlacesSearchOutcome {
        val filters = request.buildingFilters
        val canSearchStructures = EveSsoScope.SEARCH_STRUCTURES in
            tokenManager.grantedScopes(request.characterId)
        val categories = buildingCategories(filters.buildingType, canSearchStructures)
        if (categories.isEmpty()) {
            return PeoplePlacesSearchOutcome(emptyList(), 0, 0)
        }
        val typedIds = if (filters.exactMatch && request.query.trim().toLongOrNull() != null) {
            exactBuildingIds(request.query, filters.buildingType, canSearchStructures)
        } else {
            if (request.query.trim().length < PeoplePlacesConfig.SEARCH_MIN_LENGTH) {
                return emptyOutcome()
            }
            val dto = esiSearch(
                characterId = request.characterId,
                query = request.query,
                categories = categories,
                strict = filters.exactMatch,
            ) ?: return emptyOutcome()
            buildingIdsFromSearch(dto, filters.buildingType, canSearchStructures)
        }
        val named = namedBuildingIds(typedIds, request.query)
        val candidates = named.take(PeoplePlacesConfig.DISPLAY_LIMIT)
        val hydrated = hydrateAll(candidates) { hydrateBuilding(request.characterId, it) }
            .filterNotNull()
        return PeoplePlacesSearchOutcome(hydrated, hydrated.size, named.size)
    }

    private suspend fun resolveSearchIds(
        request: PeoplePlacesSearchRequest,
        exactMatch: Boolean,
        categories: List<String>,
    ): List<Long> {
        val query = request.query.trim()
        if (exactMatch) {
            query.toLongOrNull()?.let { return listOf(it) }
        }
        if (query.length < PeoplePlacesConfig.SEARCH_MIN_LENGTH) return emptyList()
        val dto = esiSearch(
            characterId = request.characterId,
            query = query,
            categories = categories,
            strict = exactMatch,
        ) ?: return emptyList()
        return idsForCategory(dto, categories.first())
    }

    private suspend fun esiSearch(
        characterId: Long,
        query: String,
        categories: List<String>,
        strict: Boolean,
    ): EsiSearchResultDto? {
        return runCatching {
            tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.search(
                    characterId = characterId,
                    authorization = auth,
                    categories = categories.joinToString(EsiSearchQuery.CATEGORY_SEPARATOR),
                    search = query,
                    strict = strict,
                )
            }
        }.getOrNull()
    }

    private fun idsForCategory(dto: EsiSearchResultDto, category: String): List<Long> = when (category) {
        EsiUniverseNameCategory.CHARACTER -> dto.character
        EsiUniverseNameCategory.CORPORATION -> dto.corporation
        EsiUniverseNameCategory.ALLIANCE -> dto.alliance
        EsiUniverseNameCategory.STATION -> dto.station
        EsiUniverseNameCategory.STRUCTURE -> dto.structure
        else -> emptyList()
    }

    private suspend fun namedIds(
        ids: List<Long>,
        expectedCategory: String,
        query: String,
    ): List<NamedId> {
        if (ids.isEmpty()) return emptyList()
        val names = publicEsi.fetchUniverseNames(ids).associateBy { it.id }
        return ids.mapIndexedNotNull { index, id ->
            val dto = names[id]
            if (dto != null && dto.category != expectedCategory) return@mapIndexedNotNull null
            NamedId(
                id = id,
                name = dto?.name.orEmpty(),
                category = dto?.category ?: expectedCategory,
                esiIndex = index,
            )
        }.sortedWith(namedIdComparator(query))
    }

    private suspend fun namedBuildingIds(
        typedIds: List<TypedBuildingId>,
        query: String,
    ): List<TypedBuildingId> {
        if (typedIds.isEmpty()) return emptyList()
        val names = publicEsi.fetchUniverseNames(typedIds.map { it.id }).associateBy { it.id }
        return typedIds.mapIndexed { index, item ->
            val dto = names[item.id]
            item.copy(
                name = dto?.name?.takeIf { it.isNotBlank() } ?: item.name,
                esiIndex = index,
            )
        }.sortedWith(
            compareBy<TypedBuildingId> { matchRank(it.name, query) }.thenBy { it.esiIndex },
        )
    }

    private fun namedIdComparator(query: String): Comparator<NamedId> =
        compareBy<NamedId> { matchRank(it.name, query) }.thenBy { it.esiIndex }

    private fun matchRank(name: String, query: String): Int {
        val n = name.lowercase(Locale.ROOT)
        val q = query.trim().lowercase(Locale.ROOT)
        return when {
            n == q -> PeoplePlacesConfig.MATCH_RANK_EXACT
            q.isNotEmpty() && n.startsWith(q) -> PeoplePlacesConfig.MATCH_RANK_PREFIX
            q.isNotEmpty() && n.contains(q) -> PeoplePlacesConfig.MATCH_RANK_CONTAINS
            else -> PeoplePlacesConfig.MATCH_RANK_OTHER
        }
    }

    private suspend fun <I, T> hydrateAll(
        items: List<I>,
        hydrate: suspend (I) -> T,
    ): List<T> = coroutineScope {
        val semaphore = Semaphore(PeoplePlacesConfig.HYDRATE_CONCURRENCY)
        items.map { item ->
            async {
                semaphore.withPermit { hydrate(item) }
            }
        }.awaitAll()
    }

    private suspend fun hydrateCharacter(named: NamedId): PeoplePlacesResult.Character {
        val public = runCatching { publicEsi.fetchCharacter(named.id) }.getOrNull()
        val corporationId = public?.corporationId
        val corporation = corporationId?.let { id ->
            runCatching { publicEsi.fetchCorporation(id) }.getOrNull()
        }
        val allianceId = public?.allianceId ?: corporation?.allianceId
        val alliance = allianceId?.let { id ->
            runCatching { publicEsi.fetchAlliance(id) }.getOrNull()
        }
        return PeoplePlacesResult.Character(
            id = named.id,
            name = public?.name?.takeIf { it.isNotBlank() } ?: named.name,
            portraitUrl = portraitUrl(named.id),
            corporationId = corporationId,
            corporationName = corporation?.name,
            corporationIconUrl = corporationId?.let { corporationLogoUrl(it) },
            allianceId = allianceId,
            allianceName = alliance?.name,
            allianceIconUrl = allianceId?.let { allianceLogoUrl(it) },
            standing = null,
        )
    }

    private suspend fun hydrateCorporation(named: NamedId): PeoplePlacesResult.Corporation {
        val corporation = runCatching { publicEsi.fetchCorporation(named.id) }.getOrNull()
        val allianceId = corporation?.allianceId
        val alliance = allianceId?.let { id ->
            runCatching { publicEsi.fetchAlliance(id) }.getOrNull()
        }
        return PeoplePlacesResult.Corporation(
            id = named.id,
            name = corporation?.name?.takeIf { it.isNotBlank() } ?: named.name,
            logoUrl = corporationLogoUrl(named.id),
            allianceId = allianceId,
            allianceName = alliance?.name,
            allianceIconUrl = allianceId?.let { allianceLogoUrl(it) },
            standing = null,
        )
    }

    private suspend fun hydrateBuilding(
        characterId: Long,
        item: TypedBuildingId,
    ): PeoplePlacesResult.Building? {
        return when (item.kind) {
            BuildingKind.STATION -> resolveStation(item.id, item.name)
            BuildingKind.STRUCTURE -> resolveStructure(characterId, item.id, item.name)
            BuildingKind.UNKNOWN -> resolveStation(item.id, item.name)
                ?: resolveStructure(characterId, item.id, item.name)
        }
    }

    private suspend fun resolveStation(
        stationId: Long,
        fallbackName: String,
    ): PeoplePlacesResult.Building? {
        val sdeStation = runCatching {
            roomProvider.getDatabase().mapDao().getStation(stationId)
        }.getOrNull()
        if (sdeStation != null) {
            val location = resolveSystemLocation(sdeStation.solarSystemId?.toLong())
            return PeoplePlacesResult.Building(
                id = stationId,
                name = sdeStation.name?.takeIf { it.isNotBlank() } ?: fallbackName,
                iconFileName = sdeStation.typeId?.let { resolveTypeIconFilename(it) },
                securityStatus = location?.securityStatus,
                systemName = location?.systemName,
                regionName = location?.regionName,
            )
        }
        val esiStation = publicEsi.fetchStation(stationId) ?: return null
        val location = resolveSystemLocation(esiStation.systemId)
        return PeoplePlacesResult.Building(
            id = stationId,
            name = esiStation.name.takeIf { it.isNotBlank() } ?: fallbackName,
            iconFileName = resolveTypeIconFilename(esiStation.typeId),
            securityStatus = location?.securityStatus,
            systemName = location?.systemName,
            regionName = location?.regionName,
        )
    }

    private suspend fun resolveStructure(
        characterId: Long,
        structureId: Long,
        fallbackName: String,
    ): PeoplePlacesResult.Building? {
        val structure = runCatching {
            tokenManager.executeWithAuthRetry(characterId) { auth ->
                universeApi.fetchStructure(structureId, auth)
            }
        }.getOrNull() ?: return null
        val location = resolveSystemLocation(structure.solarSystemId)
        return PeoplePlacesResult.Building(
            id = structureId,
            name = structure.name.takeIf { it.isNotBlank() } ?: fallbackName,
            iconFileName = structure.typeId?.let { resolveTypeIconFilename(it) },
            securityStatus = location?.securityStatus,
            systemName = location?.systemName,
            regionName = location?.regionName,
        )
    }

    private suspend fun resolveSystemLocation(solarSystemId: Long?): SystemLocation? {
        solarSystemId ?: return null
        val row = runCatching {
            roomProvider.getDatabase().mapDao().getSolarSystemLocation(solarSystemId)
        }.getOrNull()
        val language = localeController.contentLanguage
        val systemName = localizedName(
            zh = row?.systemZhName,
            en = row?.systemEnName,
            fallback = row?.systemName,
            language = language,
        ).takeIf { it.isNotBlank() } ?: publicEsi.fetchSolarSystemName(solarSystemId)
        val regionName = localizedName(
            zh = row?.regionZhName,
            en = row?.regionEnName,
            fallback = row?.regionName,
            language = language,
        ).takeIf { it.isNotBlank() }
        val security = row?.securityStatus ?: publicEsi.fetchSolarSystemSecurity(solarSystemId)
        return SystemLocation(
            securityStatus = security,
            systemName = systemName,
            regionName = regionName,
        )
    }

    private suspend fun resolveTypeIconFilename(typeId: Int): String? {
        val dao = runCatching { roomProvider.getDatabase().sdeTypeDao() }.getOrNull() ?: return null
        return runCatching { dao.getTypeIconFilename(typeId) }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: runCatching { dao.getTypeById(typeId)?.iconFilename }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun matchesCharacterFilters(
        result: PeoplePlacesResult.Character,
        filters: com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCharacterFilters,
        viewer: PeoplePlacesViewer?,
        resolvedCorpId: Long?,
        resolvedAllianceId: Long?,
    ): Boolean {
        if (filters.myCorporationOnly) {
            val viewerCorp = viewer?.corporationId ?: return false
            if (result.corporationId != viewerCorp) return false
        }
        if (filters.myAllianceOnly) {
            val viewerAlliance = viewer?.allianceId ?: return false
            if (result.allianceId != viewerAlliance) return false
        }
        if (!matchesOrgFilter(filters.corporationQuery, result.corporationId, result.corporationName, resolvedCorpId)) {
            return false
        }
        if (!matchesOrgFilter(filters.allianceQuery, result.allianceId, result.allianceName, resolvedAllianceId)) {
            return false
        }
        return true
    }

    private fun matchesCorporationFilters(
        result: PeoplePlacesResult.Corporation,
        filters: com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCorporationFilters,
        viewer: PeoplePlacesViewer?,
        resolvedAllianceId: Long?,
    ): Boolean {
        if (filters.myAllianceOnly) {
            val viewerAlliance = viewer?.allianceId ?: return false
            if (result.allianceId != viewerAlliance) return false
        }
        return matchesOrgFilter(
            filters.allianceQuery,
            result.allianceId,
            result.allianceName,
            resolvedAllianceId,
        )
    }

    private fun matchesOrgFilter(
        query: String,
        orgId: Long?,
        orgName: String?,
        resolvedId: Long?,
    ): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return true
        trimmed.toLongOrNull()?.let { return orgId == it }
        if (resolvedId != null && orgId == resolvedId) return true
        return orgName.orEmpty().contains(trimmed, ignoreCase = true)
    }

    private suspend fun resolveOrgFilterId(query: String, kind: OrgFilterKind): Long? {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || trimmed.toLongOrNull() != null) return null
        val dto = runCatching { universeApi.fetchUniverseIds(listOf(trimmed)) }.getOrNull() ?: return null
        return when (kind) {
            OrgFilterKind.CORPORATION -> dto.corporations.firstOrNull {
                it.name.equals(trimmed, ignoreCase = true)
            }?.id
            OrgFilterKind.ALLIANCE -> dto.alliances.firstOrNull {
                it.name.equals(trimmed, ignoreCase = true)
            }?.id
        }
    }

    private fun resolveStanding(
        viewer: PeoplePlacesViewer?,
        kind: PeoplePlacesCategory,
        resultId: Long,
        resultCorporationId: Long?,
        resultAllianceId: Long?,
    ): PeoplePlacesStanding? {
        viewer ?: return null
        val contacts = contactsByCharacterId[viewer.characterId] ?: ViewerContacts()
        when (kind) {
            PeoplePlacesCategory.CHARACTER -> {
                if (viewer.corporationId != null && resultCorporationId == viewer.corporationId) {
                    return PeoplePlacesStanding.SAME_CORPORATION
                }
                if (viewer.allianceId != null && resultAllianceId == viewer.allianceId) {
                    return PeoplePlacesStanding.SAME_ALLIANCE
                }
            }
            PeoplePlacesCategory.CORPORATION -> {
                if (viewer.corporationId != null && resultId == viewer.corporationId) {
                    return PeoplePlacesStanding.SAME_CORPORATION
                }
                if (viewer.allianceId != null && resultAllianceId == viewer.allianceId) {
                    return PeoplePlacesStanding.SAME_ALLIANCE
                }
            }
            PeoplePlacesCategory.ALLIANCE -> {
                if (viewer.allianceId != null && resultId == viewer.allianceId) {
                    return PeoplePlacesStanding.SAME_ALLIANCE
                }
            }
            PeoplePlacesCategory.STRUCTURE -> return null
        }
        val targets = when (kind) {
            PeoplePlacesCategory.CHARACTER -> listOfNotNull(resultCorporationId, resultAllianceId, resultId)
            PeoplePlacesCategory.CORPORATION -> listOfNotNull(resultId, resultAllianceId)
            PeoplePlacesCategory.ALLIANCE -> listOf(resultId)
        }
        val sources = listOf(
            contacts.corporationContacts,
            contacts.allianceContacts,
            contacts.personalContacts,
        )
        for (source in sources) {
            for (targetId in targets) {
                val standing = source.firstOrNull { it.contactId == targetId }?.standing
                if (standing != null) return standing.toStandingIcon()
            }
        }
        return null
    }

    private fun Double.toStandingIcon(): PeoplePlacesStanding = when {
        this >= PeoplePlacesConfig.STANDING_PLUS_10 -> PeoplePlacesStanding.PLUS_10
        this >= PeoplePlacesConfig.STANDING_PLUS_5 -> PeoplePlacesStanding.PLUS_5
        this > PeoplePlacesConfig.STANDING_MINUS_5 -> PeoplePlacesStanding.NEUTRAL
        this > PeoplePlacesConfig.STANDING_MINUS_10 -> PeoplePlacesStanding.MINUS_5
        else -> PeoplePlacesStanding.MINUS_10
    }

    private suspend fun loadViewerContacts(
        characterId: Long,
        corporationId: Long?,
        allianceId: Long?,
    ): ViewerContacts {
        val personal = fetchContactsOrEmpty(characterId) { auth ->
            characterApi.fetchContacts(characterId, auth)
        }
        val corporation = corporationId?.let { corpId ->
            fetchContactsOrEmpty(characterId) { auth ->
                corporationApi.fetchContacts(corpId, auth)
            }
        }.orEmpty()
        val alliance = allianceId?.let { id ->
            fetchContactsOrEmpty(characterId) { auth ->
                allianceApi.fetchContacts(id, auth)
            }
        }.orEmpty()
        return ViewerContacts(
            personalContacts = personal,
            corporationContacts = corporation,
            allianceContacts = alliance,
        )
    }

    private suspend fun fetchContactsOrEmpty(
        viewerCharacterId: Long,
        block: suspend (authorization: String) -> List<EsiContactDto>,
    ): List<EsiContactDto> {
        return runCatching {
            tokenManager.executeWithAuthRetry(viewerCharacterId, block)
        }.getOrElse { error ->
            if (error is HttpException && error.code() == EsiConfig.HttpStatus.FORBIDDEN) {
                emptyList()
            } else {
                emptyList()
            }
        }
    }

    private fun buildingCategories(
        type: PeoplePlacesBuildingType,
        canSearchStructures: Boolean,
    ): List<String> = when (type) {
        PeoplePlacesBuildingType.STATION -> listOf(EsiUniverseNameCategory.STATION)
        PeoplePlacesBuildingType.STRUCTURE ->
            if (canSearchStructures) listOf(EsiUniverseNameCategory.STRUCTURE) else emptyList()
        PeoplePlacesBuildingType.ALL -> buildList {
            add(EsiUniverseNameCategory.STATION)
            if (canSearchStructures) add(EsiUniverseNameCategory.STRUCTURE)
        }
    }

    private fun buildingIdsFromSearch(
        dto: EsiSearchResultDto,
        type: PeoplePlacesBuildingType,
        canSearchStructures: Boolean,
    ): List<TypedBuildingId> = buildList {
        if (type != PeoplePlacesBuildingType.STRUCTURE) {
            dto.station.forEach { id ->
                add(TypedBuildingId(id = id, kind = BuildingKind.STATION))
            }
        }
        if (type != PeoplePlacesBuildingType.STATION && canSearchStructures) {
            dto.structure.forEach { id ->
                add(TypedBuildingId(id = id, kind = BuildingKind.STRUCTURE))
            }
        }
    }

    private fun exactBuildingIds(
        query: String,
        type: PeoplePlacesBuildingType,
        canSearchStructures: Boolean,
    ): List<TypedBuildingId> {
        val id = query.trim().toLongOrNull() ?: return emptyList()
        val kind = when (type) {
            PeoplePlacesBuildingType.STATION -> BuildingKind.STATION
            PeoplePlacesBuildingType.STRUCTURE ->
                if (canSearchStructures) BuildingKind.STRUCTURE else return emptyList()
            PeoplePlacesBuildingType.ALL -> BuildingKind.UNKNOWN
        }
        return listOf(TypedBuildingId(id = id, kind = kind, name = query.trim()))
    }

    private fun emptyOutcome() = PeoplePlacesSearchOutcome(emptyList(), 0, 0)

    private data class NamedId(
        val id: Long,
        val name: String,
        val category: String,
        val esiIndex: Int,
    )

    private data class TypedBuildingId(
        val id: Long,
        val kind: BuildingKind,
        val name: String = "",
        val esiIndex: Int = 0,
    )

    private enum class BuildingKind {
        STATION,
        STRUCTURE,
        UNKNOWN,
    }

    private enum class OrgFilterKind {
        CORPORATION,
        ALLIANCE,
    }

    private data class ViewerContacts(
        val personalContacts: List<EsiContactDto> = emptyList(),
        val corporationContacts: List<EsiContactDto> = emptyList(),
        val allianceContacts: List<EsiContactDto> = emptyList(),
    )

    private data class SystemLocation(
        val securityStatus: Double?,
        val systemName: String?,
        val regionName: String?,
    )
}
