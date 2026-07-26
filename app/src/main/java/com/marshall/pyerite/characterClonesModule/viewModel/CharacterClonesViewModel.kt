package com.marshall.pyerite.characterClonesModule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterClonesViewModel(
    private val repository: CharacterClonesRepository,
) : ViewModel() {

    private val _nextCloneJumpEpochMs = MutableStateFlow<Long?>(null)
    val nextCloneJumpEpochMs: StateFlow<Long?> = _nextCloneJumpEpochMs.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var trackedCharacterId: Long? = null
    private var loadJob: Job? = null

    /** Bind main-page selection; loads cache first then refreshes from ESI when needed. */
    fun setCharacterId(characterId: Long?) {
        if (characterId == null) {
            trackedCharacterId = null
            loadJob?.cancel()
            _nextCloneJumpEpochMs.value = null
            _isRefreshing.value = false
            return
        }
        if (trackedCharacterId == characterId) return
        trackedCharacterId = characterId
        _nextCloneJumpEpochMs.value = repository.cachedStatus(characterId)?.nextCloneJumpEpochMs
        load(characterId, forceRefresh = false)
    }

    /** Pull-to-refresh / top-bar retry for the currently tracked character. */
    fun refresh() {
        val characterId = trackedCharacterId ?: return
        load(characterId, forceRefresh = true)
    }

    private fun load(characterId: Long, forceRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isRefreshing.value = true
            val result = runCatching {
                repository.loadStatus(characterId, forceRefresh = forceRefresh)
            }
            if (trackedCharacterId != characterId) return@launch
            result.onSuccess { status ->
                _nextCloneJumpEpochMs.value = status.nextCloneJumpEpochMs
            }
            _isRefreshing.value = false
        }
    }
}
