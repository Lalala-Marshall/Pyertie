package com.marshall.pyerite.characterMailModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailbox
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

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }
    val labelId: Int? = savedStateHandle.get<Int>(NAV_ARG_LABEL_ID)
        ?.takeUnless { it == NAV_LABEL_ID_UNFILTERED }
    val mailbox: CharacterMailMailbox? = labelId?.let { id ->
        repository.cachedMailbox(characterId, id) ?: CharacterMailMailbox(labelId = id, name = null)
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
        val cached = repository.cachedInbox(characterId, labelId)
        return if (cached != null) {
            CharacterMailUiState(
                inbox = cached,
                isLoading = false,
                loadFailed = false,
                detailsReady = true,
            )
        } else {
            CharacterMailUiState(
                inbox = CharacterMailInbox.empty(characterId, labelId),
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
            val result = runCatching { repository.loadInbox(characterId, labelId) }
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
        const val NAV_ARG_LABEL_ID = "labelId"
        const val NAV_LABEL_ID_UNFILTERED = -1
    }
}

internal data class CharacterMailUiState(
    val inbox: CharacterMailInbox,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    val detailsReady: Boolean,
)
