package com.marshall.pyerite.peoplePlacesModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesBuildingFilters
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCategory
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCharacterFilters
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesConfig
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCorporationFilters
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesResult
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesSearchRequest
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesSearchStatus
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesViewer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PeoplePlacesViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PeoplePlacesRepository,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[PeoplePlacesNavArgs.CHARACTER_ID]) {
        "Missing ${PeoplePlacesNavArgs.CHARACTER_ID}"
    }

    private val _uiState = MutableStateFlow(PeoplePlacesUiState())
    val uiState: StateFlow<PeoplePlacesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val viewer = runCatching { repository.loadViewer(characterId) }.getOrNull()
            _uiState.update { it.copy(viewer = viewer) }
            if (_uiState.value.hasCommittedSearch) {
                scheduleSearch(immediate = true)
            }
        }
    }

    fun onQueryChange(query: String) {
        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.update {
                it.copy(
                    query = query,
                    searchStatus = PeoplePlacesSearchStatus.IDLE,
                    results = emptyList(),
                    displayedCount = 0,
                    totalCount = 0,
                )
            }
            return
        }
        _uiState.update { current ->
            current.copy(
                query = query,
                searchStatus = if (current.searchStatus == PeoplePlacesSearchStatus.QUERY_TOO_SHORT) {
                    PeoplePlacesSearchStatus.IDLE
                } else {
                    current.searchStatus
                },
            )
        }
        scheduleSearch()
    }

    fun onSearchAction() {
        searchJob?.cancel()
        val query = _uiState.value.query.trim()
        if (query.requiresMinLength()) {
            _uiState.update {
                it.copy(
                    searchStatus = PeoplePlacesSearchStatus.QUERY_TOO_SHORT,
                    results = emptyList(),
                    displayedCount = 0,
                    totalCount = 0,
                )
            }
            return
        }
        scheduleSearch(immediate = true)
    }

    fun onCategoryChange(category: PeoplePlacesCategory) {
        if (_uiState.value.category == category) return
        _uiState.update { it.copy(category = category) }
        if (_uiState.value.hasCommittedSearch || _uiState.value.query.isNotBlank()) {
            scheduleSearch(immediate = true)
        }
    }

    fun onCharacterFiltersChange(
        filters: PeoplePlacesCharacterFilters,
        immediate: Boolean = false,
    ) {
        _uiState.update { it.copy(characterFilters = filters) }
        rescheduleIfCommitted(immediate)
    }

    fun onCorporationFiltersChange(
        filters: PeoplePlacesCorporationFilters,
        immediate: Boolean = false,
    ) {
        _uiState.update { it.copy(corporationFilters = filters) }
        rescheduleIfCommitted(immediate)
    }

    fun onAllianceExactMatchChange(exactMatch: Boolean) {
        _uiState.update { it.copy(allianceExactMatch = exactMatch) }
        rescheduleIfCommitted(immediate = true)
    }

    fun onBuildingFiltersChange(
        filters: PeoplePlacesBuildingFilters,
        immediate: Boolean = false,
    ) {
        _uiState.update { it.copy(buildingFilters = filters) }
        rescheduleIfCommitted(immediate)
    }

    fun onClearFilters() {
        _uiState.update { state ->
            when (state.category) {
                PeoplePlacesCategory.CHARACTER -> state.copy(
                    characterFilters = PeoplePlacesCharacterFilters(),
                )
                PeoplePlacesCategory.CORPORATION -> state.copy(
                    corporationFilters = PeoplePlacesCorporationFilters(),
                )
                PeoplePlacesCategory.ALLIANCE -> state
                PeoplePlacesCategory.STRUCTURE -> state.copy(
                    buildingFilters = PeoplePlacesBuildingFilters(),
                )
            }
        }
        rescheduleIfCommitted(immediate = true)
    }

    private fun rescheduleIfCommitted(immediate: Boolean = false) {
        if (!_uiState.value.hasCommittedSearch && _uiState.value.query.isBlank()) return
        if (_uiState.value.query.isBlank()) return
        scheduleSearch(immediate)
    }

    private fun scheduleSearch(immediate: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (!immediate) {
                delay(PeoplePlacesConfig.SEARCH_DEBOUNCE)
            }
            performSearch()
        }
    }

    private suspend fun performSearch() {
        val snapshot = _uiState.value
        val query = snapshot.query.trim()
        if (query.isEmpty()) {
            _uiState.update {
                it.copy(
                    searchStatus = PeoplePlacesSearchStatus.IDLE,
                    results = emptyList(),
                    displayedCount = 0,
                    totalCount = 0,
                )
            }
            return
        }
        if (query.requiresMinLength()) {
            _uiState.update {
                it.copy(
                    searchStatus = PeoplePlacesSearchStatus.IDLE,
                    results = emptyList(),
                    displayedCount = 0,
                    totalCount = 0,
                )
            }
            return
        }
        _uiState.update { it.copy(searchStatus = PeoplePlacesSearchStatus.SEARCHING) }
        val result = runCatching {
            repository.search(
                PeoplePlacesSearchRequest(
                    characterId = characterId,
                    query = query,
                    category = snapshot.category,
                    characterFilters = snapshot.characterFilters,
                    corporationFilters = snapshot.corporationFilters,
                    allianceExactMatch = snapshot.allianceExactMatch,
                    buildingFilters = snapshot.buildingFilters,
                    viewer = snapshot.viewer,
                ),
            )
        }
        _uiState.update { current ->
            result.fold(
                onSuccess = { outcome ->
                    current.copy(
                        searchStatus = PeoplePlacesSearchStatus.RESULTS,
                        results = outcome.results,
                        displayedCount = outcome.displayedCount,
                        totalCount = outcome.totalCount,
                    )
                },
                onFailure = {
                    current.copy(
                        searchStatus = PeoplePlacesSearchStatus.FAILED,
                        results = emptyList(),
                        displayedCount = 0,
                        totalCount = 0,
                    )
                },
            )
        }
    }
}

internal data class PeoplePlacesUiState(
    val category: PeoplePlacesCategory = PeoplePlacesCategory.CHARACTER,
    val query: String = "",
    val characterFilters: PeoplePlacesCharacterFilters = PeoplePlacesCharacterFilters(),
    val corporationFilters: PeoplePlacesCorporationFilters = PeoplePlacesCorporationFilters(),
    val allianceExactMatch: Boolean = false,
    val buildingFilters: PeoplePlacesBuildingFilters = PeoplePlacesBuildingFilters(),
    val viewer: PeoplePlacesViewer? = null,
    val searchStatus: PeoplePlacesSearchStatus = PeoplePlacesSearchStatus.IDLE,
    val results: List<PeoplePlacesResult> = emptyList(),
    val displayedCount: Int = 0,
    val totalCount: Int = 0,
) {
    val hasCommittedSearch: Boolean
        get() = searchStatus != PeoplePlacesSearchStatus.IDLE

    val showMyCorporationFilter: Boolean
        get() = viewer != null

    val showMyAllianceFilter: Boolean
        get() = viewer?.allianceId != null
}

private fun String.requiresMinLength(): Boolean =
    toLongOrNull() == null && length < PeoplePlacesConfig.SEARCH_MIN_LENGTH
