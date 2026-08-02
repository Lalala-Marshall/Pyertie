package com.marshall.pyerite.characterSkillsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillPoints
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogFilter
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared by main-page skills hint, skills page, attributes, catalog details, and catalog group.
 * - Detail routes: [NAV_ARG_CHARACTER_ID] from [SavedStateHandle]
 * - Catalog group: also [NAV_ARG_GROUP_ID] + [NAV_ARG_CATALOG_FILTER]
 * - Main page: call [setCharacterId] when selection changes
 */
class CharacterSkillsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterSkillsRepository,
) : ViewModel() {

    private val routeCharacterId: Long? = savedStateHandle[NAV_ARG_CHARACTER_ID]

    /** Set on the catalog-group route; null on other skill routes. */
    val routeCatalogGroupId: Int? = savedStateHandle[NAV_ARG_GROUP_ID]

    /** Filter snapshot from the catalog-group route nav args. */
    val routeCatalogFilter: SkillCatalogFilter =
        savedStateHandle.get<String>(NAV_ARG_CATALOG_FILTER)
            ?.let { runCatching { SkillCatalogFilter.valueOf(it) }.getOrNull() }
            ?: SkillCatalogFilter.ALL

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CharacterSkillsUiState> = _uiState.asStateFlow()

    private var trackedCharacterId: Long? = routeCharacterId
    private var loadJob: Job? = null
    private var catalogJob: Job? = null

    init {
        val characterId = routeCharacterId
        if (characterId != null) {
            load(
                characterId = characterId,
                indicateLoading = !_uiState.value.detailsReady,
            )
        }
    }

    /** Bind main-page selection (no-op on the detail route). */
    fun setCharacterId(characterId: Long?) {
        if (routeCharacterId != null) return
        if (characterId == null) {
            trackedCharacterId = null
            loadJob?.cancel()
            catalogJob?.cancel()
            _uiState.value = CharacterSkillsUiState.empty()
            return
        }
        if (trackedCharacterId == characterId) return
        trackedCharacterId = characterId
        _uiState.value = CharacterSkillsUiState.fromCache(
            characterId = characterId,
            cachedStatus = repository.cachedStatus(characterId),
            cachedAttributes = repository.cachedAttributes(characterId),
            cachedCatalog = repository.cachedCatalog(characterId),
            cachedSkillPoints = repository.cachedSkillPoints(characterId),
        )
        load(
            characterId = characterId,
            indicateLoading = !_uiState.value.detailsReady,
        )
    }

    fun refresh() {
        val characterId = trackedCharacterId ?: return
        if (_uiState.value.isLoading) return
        load(
            characterId = characterId,
            indicateLoading = true,
        )
    }

    /** Load attributes for the attributes page (cache-first). */
    fun ensureAttributesLoaded() {
        val characterId = trackedCharacterId ?: return
        if (_uiState.value.attributesReady) return
        loadAttributes(characterId)
    }

    fun refreshAttributes() {
        val characterId = trackedCharacterId ?: return
        if (_uiState.value.isLoading) return
        loadAttributes(characterId)
    }

    /** Load / refresh skill-catalog groups (catalog details page). */
    fun ensureCatalogLoaded() {
        val characterId = trackedCharacterId ?: return
        if (_uiState.value.catalogReady) {
            if (!_uiState.value.attributesReady) {
                ensureAttributesLoaded()
            }
            return
        }
        loadCatalog(characterId)
    }

    fun refreshCatalog() {
        val characterId = trackedCharacterId ?: return
        if (_uiState.value.isLoading) return
        loadCatalog(characterId)
    }

    fun setCatalogFilter(filter: SkillCatalogFilter) {
        _uiState.update { it.copy(catalogFilter = filter) }
    }

    fun setCatalogSearchActive(active: Boolean) {
        _uiState.update { it.copy(catalogSearchActive = active) }
    }

    fun setCatalogSearchQuery(query: String) {
        _uiState.update { it.copy(catalogSearchQuery = query) }
    }

    fun cancelCatalogSearch() {
        _uiState.update {
            it.copy(
                catalogSearchActive = false,
                catalogSearchQuery = "",
            )
        }
    }

    private fun initialUiState(): CharacterSkillsUiState {
        val characterId = routeCharacterId ?: return CharacterSkillsUiState.empty()
        return CharacterSkillsUiState.fromCache(
            characterId = characterId,
            cachedStatus = repository.cachedStatus(characterId),
            cachedAttributes = repository.cachedAttributes(characterId),
            cachedCatalog = repository.cachedCatalog(characterId),
            cachedSkillPoints = repository.cachedSkillPoints(characterId),
        )
    }

    private fun load(
        characterId: Long,
        indicateLoading: Boolean,
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hadCachedQueue = _uiState.value.detailsReady
            _uiState.update {
                it.copy(
                    isLoading = indicateLoading,
                    loadFailed = false,
                )
            }
            val result = runCatching {
                repository.loadStatus(characterId)
            }
            if (trackedCharacterId != characterId) return@launch
            _uiState.update { current ->
                result.fold(
                    onSuccess = { status ->
                        current.copy(
                            status = status,
                            isLoading = false,
                            loadFailed = false,
                            detailsReady = true,
                        )
                    },
                    onFailure = {
                        current.copy(
                            isLoading = false,
                            loadFailed = !hadCachedQueue,
                        )
                    },
                )
            }
        }
    }

    private fun loadAttributes(characterId: Long) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hadCached = _uiState.value.attributesReady
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                )
            }
            val result = runCatching {
                repository.loadAttributes(characterId)
            }
            if (trackedCharacterId != characterId) return@launch
            _uiState.update { current ->
                result.fold(
                    onSuccess = { attributes ->
                        current.copy(
                            attributes = attributes,
                            isLoading = false,
                            loadFailed = false,
                            attributesReady = true,
                        )
                    },
                    onFailure = {
                        current.copy(
                            isLoading = false,
                            loadFailed = !hadCached,
                        )
                    },
                )
            }
        }
    }

    private fun loadCatalog(characterId: Long) {
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            val hadCached = _uiState.value.catalogReady
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                )
            }
            val result = runCatching {
                coroutineScope {
                    val catalogDeferred = async { repository.loadCatalog(characterId) }
                    val statusDeferred = async { repository.loadStatus(characterId) }
                    val attributesDeferred = async {
                        runCatching { repository.loadAttributes(characterId) }.getOrNull()
                    }
                    Triple(
                        catalogDeferred.await(),
                        statusDeferred.await(),
                        attributesDeferred.await(),
                    )
                }
            }
            if (trackedCharacterId != characterId) return@launch
            _uiState.update { current ->
                result.fold(
                    onSuccess = { (catalogResult, status, attributes) ->
                        current.copy(
                            catalogGroups = catalogResult.groups,
                            skillPoints = catalogResult.skillPoints,
                            status = status,
                            attributes = attributes ?: current.attributes,
                            isLoading = false,
                            loadFailed = false,
                            catalogReady = true,
                            detailsReady = true,
                            attributesReady = attributes != null || current.attributesReady,
                            skillPointsReady = true,
                        )
                    },
                    onFailure = {
                        current.copy(
                            isLoading = false,
                            loadFailed = !hadCached,
                        )
                    },
                )
            }
        }
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = "characterId"
        const val NAV_ARG_GROUP_ID = "groupId"
        const val NAV_ARG_CATALOG_FILTER = "filter"
    }
}

data class CharacterSkillsUiState(
    val status: CharacterSkillQueueStatus,
    val attributes: CharacterAttributes,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    /** True after at least one successful skill-queue ESI load (or cache hit). */
    val detailsReady: Boolean,
    /** True after at least one successful attributes ESI load (or cache hit). */
    val attributesReady: Boolean,
    val catalogGroups: List<SkillCatalogGroup> = emptyList(),
    val catalogReady: Boolean = false,
    val skillPoints: CharacterSkillPoints = CharacterSkillPoints.empty(characterId = 0L),
    val skillPointsReady: Boolean = false,
    val catalogFilter: SkillCatalogFilter = SkillCatalogFilter.ALL,
    val catalogSearchQuery: String = "",
    val catalogSearchActive: Boolean = false,
) {
    companion object {
        fun empty(): CharacterSkillsUiState = CharacterSkillsUiState(
            status = CharacterSkillQueueStatus.empty(characterId = 0L),
            attributes = CharacterAttributes.empty(characterId = 0L),
            isLoading = false,
            loadFailed = false,
            detailsReady = false,
            attributesReady = false,
        )

        fun fromCache(
            characterId: Long,
            cachedStatus: CharacterSkillQueueStatus?,
            cachedAttributes: CharacterAttributes?,
            cachedCatalog: List<SkillCatalogGroup>?,
            cachedSkillPoints: CharacterSkillPoints?,
        ): CharacterSkillsUiState = CharacterSkillsUiState(
            status = cachedStatus ?: CharacterSkillQueueStatus.empty(characterId),
            attributes = cachedAttributes ?: CharacterAttributes.empty(characterId),
            isLoading = cachedStatus == null && cachedAttributes == null,
            loadFailed = false,
            detailsReady = cachedStatus != null,
            attributesReady = cachedAttributes != null,
            catalogGroups = cachedCatalog.orEmpty(),
            catalogReady = cachedCatalog != null,
            skillPoints = cachedSkillPoints ?: CharacterSkillPoints.empty(characterId),
            skillPointsReady = cachedSkillPoints != null,
        )
    }
}
