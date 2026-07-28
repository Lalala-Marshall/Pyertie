package com.marshall.pyerite.characterSkillsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared by main-page skills hint, skills page, and attributes page.
 * - Detail routes: [NAV_ARG_CHARACTER_ID] from [SavedStateHandle]
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
                indicateLoading = !_uiState.value.detailsReady || !_uiState.value.attributesReady,
                includeAttributes = true,
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
            cachedStatus = repository.cachedStatus(characterId),
            cachedAttributes = repository.cachedAttributes(characterId),
        )
        load(
            characterId = characterId,
            indicateLoading = !_uiState.value.detailsReady,
            includeAttributes = false,
        )
    }

    fun refresh() {
        val characterId = trackedCharacterId ?: return
        if (_uiState.value.isLoading) return
        load(
            characterId = characterId,
            indicateLoading = true,
            includeAttributes = routeCharacterId != null,
        )
    }

    private fun initialUiState(): CharacterSkillsUiState {
        val characterId = routeCharacterId ?: return CharacterSkillsUiState.empty()
        return CharacterSkillsUiState.fromCache(
            characterId = characterId,
            cachedStatus = repository.cachedStatus(characterId),
            cachedAttributes = repository.cachedAttributes(characterId),
        )
    }

    private fun load(
        characterId: Long,
        indicateLoading: Boolean,
        includeAttributes: Boolean,
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hadCachedQueue = _uiState.value.detailsReady
            val hadCachedAttributes = _uiState.value.attributesReady
            _uiState.update {
                it.copy(
                    isLoading = indicateLoading,
                    loadFailed = false,
                )
            }
            val result = runCatching {
                if (includeAttributes) {
                    coroutineScope {
                        val statusDeferred = async { repository.loadStatus(characterId) }
                        val attributesDeferred = async { repository.loadAttributes(characterId) }
                        statusDeferred.await() to attributesDeferred.await()
                    }
                } else {
                    repository.loadStatus(characterId) to null
                }
            }
            if (trackedCharacterId != characterId) return@launch
            _uiState.update { current ->
                result.fold(
                    onSuccess = { (status, attributes) ->
                        current.copy(
                            status = status,
                            attributes = attributes ?: current.attributes,
                            isLoading = false,
                            loadFailed = false,
                            detailsReady = true,
                            attributesReady = attributes != null || current.attributesReady,
                        )
                    },
                    onFailure = {
                        current.copy(
                            isLoading = false,
                            loadFailed = if (includeAttributes) {
                                !hadCachedQueue && !hadCachedAttributes
                            } else {
                                !hadCachedQueue
                            },
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
    val attributes: CharacterAttributes,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    /** True after at least one successful skill-queue ESI load (or cache hit). */
    val detailsReady: Boolean,
    /** True after at least one successful attributes ESI load (or cache hit). */
    val attributesReady: Boolean,
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
        ): CharacterSkillsUiState = CharacterSkillsUiState(
            status = cachedStatus ?: CharacterSkillQueueStatus.empty(characterId),
            attributes = cachedAttributes ?: CharacterAttributes.empty(characterId),
            isLoading = cachedStatus == null && cachedAttributes == null,
            loadFailed = false,
            detailsReady = cachedStatus != null,
            attributesReady = cachedAttributes != null,
        )
    }
}
