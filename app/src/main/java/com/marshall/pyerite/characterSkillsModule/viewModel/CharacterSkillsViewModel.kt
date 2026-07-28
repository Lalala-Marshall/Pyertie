package com.marshall.pyerite.characterSkillsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared by main-page skills hint and the skills detail page.
 * - Detail route: [NAV_ARG_CHARACTER_ID] from [SavedStateHandle]
 * - Main page: call [setCharacterId] when selection changes
 *
 * Shows disk/memory cache immediately, then refreshes from ESI.
 */
class CharacterSkillsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterSkillsRepository,
) : ViewModel() {

    private val routeCharacterId: Long? = savedStateHandle[NAV_ARG_CHARACTER_ID]

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CharacterSkillsUiState> = _uiState.asStateFlow()

    private var trackedCharacterId: Long? = routeCharacterId
    private var loadJob: Job? = null

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
            _uiState.value = CharacterSkillsUiState.empty()
            return
        }
        if (trackedCharacterId == characterId) return
        trackedCharacterId = characterId
        _uiState.value = CharacterSkillsUiState.fromCache(
            characterId = characterId,
            cached = repository.cachedStatus(characterId),
        )
        load(
            characterId = characterId,
            indicateLoading = !_uiState.value.detailsReady,
        )
    }

    fun refresh() {
        val characterId = trackedCharacterId ?: return
        if (_uiState.value.isLoading) return
        load(characterId, indicateLoading = true)
    }

    private fun initialUiState(): CharacterSkillsUiState {
        val characterId = routeCharacterId ?: return CharacterSkillsUiState.empty()
        return CharacterSkillsUiState.fromCache(
            characterId = characterId,
            cached = repository.cachedStatus(characterId),
        )
    }

    private fun load(
        characterId: Long,
        indicateLoading: Boolean,
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hadCachedDetails = _uiState.value.detailsReady
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
                            loadFailed = !hadCachedDetails,
                        )
                    },
                )
            }
        }
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = "characterId"
    }
}

data class CharacterSkillsUiState(
    val status: CharacterSkillQueueStatus,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    /** True after at least one successful skills ESI load (or disk/memory cache hit). */
    val detailsReady: Boolean,
) {
    companion object {
        fun empty(): CharacterSkillsUiState = CharacterSkillsUiState(
            status = CharacterSkillQueueStatus.empty(characterId = 0L),
            isLoading = false,
            loadFailed = false,
            detailsReady = false,
        )

        fun fromCache(
            characterId: Long,
            cached: CharacterSkillQueueStatus?,
        ): CharacterSkillsUiState = if (cached != null) {
            CharacterSkillsUiState(
                status = cached,
                isLoading = false,
                loadFailed = false,
                detailsReady = true,
            )
        } else {
            CharacterSkillsUiState(
                status = CharacterSkillQueueStatus.empty(characterId),
                isLoading = true,
                loadFailed = false,
                detailsReady = false,
            )
        }
    }
}
