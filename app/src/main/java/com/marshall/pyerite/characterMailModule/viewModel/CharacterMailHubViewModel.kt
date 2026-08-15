package com.marshall.pyerite.characterMailModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailboxes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharacterMailHubViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterMailRepository,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CharacterMailHubUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load(indicateLoading = !_uiState.value.detailsReady)
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(indicateLoading = true)
    }

    private fun initialUiState(): CharacterMailHubUiState {
        val cached = repository.cachedMailboxes(characterId)
        return if (cached != null) {
            CharacterMailHubUiState(
                mailboxes = cached,
                isLoading = false,
                loadFailed = false,
                detailsReady = true,
            )
        } else {
            CharacterMailHubUiState(
                mailboxes = CharacterMailMailboxes.empty(characterId),
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
            val result = runCatching { repository.loadMailboxes(characterId) }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { mailboxes ->
                        current.copy(
                            mailboxes = mailboxes,
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
        const val NAV_ARG_CHARACTER_ID = CharacterMailViewModel.NAV_ARG_CHARACTER_ID
    }
}

internal data class CharacterMailHubUiState(
    val mailboxes: CharacterMailMailboxes,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    val detailsReady: Boolean,
)
