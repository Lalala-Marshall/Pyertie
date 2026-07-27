package com.marshall.pyerite.characterClonesModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared by main-page clone hint and the clones detail page.
 * - Detail route: [NAV_ARG_CHARACTER_ID] from [SavedStateHandle]
 * - Main page: call [setCharacterId] when selection changes
 *
 * Shows disk/memory cache immediately (like character-sheet SP), then refreshes from ESI.
 */
class CharacterClonesViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterClonesRepository,
) : ViewModel() {

    private val routeCharacterId: Long? = savedStateHandle[NAV_ARG_CHARACTER_ID]

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CharacterClonesUiState> = _uiState.asStateFlow()

    private var trackedCharacterId: Long? = routeCharacterId
    private var loadJob: Job? = null

    init {
        val characterId = routeCharacterId
        if (characterId != null) {
            // Cache already applied in [initialUiState]; always revalidate from ESI.
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
            _uiState.value = CharacterClonesUiState.empty()
            return
        }
        if (trackedCharacterId == characterId) return
        trackedCharacterId = characterId
        _uiState.value = CharacterClonesUiState.fromCache(
            characterId = characterId,
            cached = repository.cachedStatus(characterId),
        )
        // Revalidate after painting cache; keep spinner off when cache already shown.
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

    private fun initialUiState(): CharacterClonesUiState {
        val characterId = routeCharacterId ?: return CharacterClonesUiState.empty()
        return CharacterClonesUiState.fromCache(
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

data class CharacterClonesUiState(
    val status: CharacterCloneStatus,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    /** True after at least one successful clones ESI load (or disk/memory cache hit). */
    val detailsReady: Boolean,
) {
    companion object {
        fun empty(): CharacterClonesUiState = CharacterClonesUiState(
            status = CharacterCloneStatus.empty(characterId = 0L),
            isLoading = false,
            loadFailed = false,
            detailsReady = false,
        )

        fun fromCache(
            characterId: Long,
            cached: CharacterCloneStatus?,
        ): CharacterClonesUiState = if (cached != null) {
            CharacterClonesUiState(
                status = cached,
                isLoading = false,
                loadFailed = false,
                detailsReady = true,
            )
        } else {
            CharacterClonesUiState(
                status = CharacterCloneStatus.empty(characterId),
                isLoading = true,
                loadFailed = false,
                detailsReady = false,
            )
        }
    }
}
