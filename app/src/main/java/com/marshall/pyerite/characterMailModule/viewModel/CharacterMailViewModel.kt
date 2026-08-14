package com.marshall.pyerite.characterMailModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharacterMailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterMailRepository,
) : ViewModel() {

    private val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CharacterMailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load(indicateLoading = !_uiState.value.detailsReady)
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(indicateLoading = true)
    }

    private fun initialUiState(): CharacterMailUiState {
        val cached = repository.cachedInbox(characterId)
        return if (cached != null) {
            CharacterMailUiState(
                inbox = cached,
                isLoading = false,
                loadFailed = false,
                detailsReady = true,
            )
        } else {
            CharacterMailUiState(
                inbox = CharacterMailInbox.empty(characterId),
                isLoading = true,
                loadFailed = false,
                detailsReady = false,
            )
        }
    }

    private fun load(indicateLoading: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hadCachedDetails = _uiState.value.detailsReady
            _uiState.update {
                it.copy(isLoading = indicateLoading, loadFailed = false)
            }
            val result = runCatching { repository.loadInbox(characterId) }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { inbox ->
                        current.copy(
                            inbox = inbox,
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

internal data class CharacterMailUiState(
    val inbox: CharacterMailInbox,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    val detailsReady: Boolean,
)
