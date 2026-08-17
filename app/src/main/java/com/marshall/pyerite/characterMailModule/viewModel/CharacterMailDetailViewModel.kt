package com.marshall.pyerite.characterMailModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterMailModule.model.CharacterMailDetail
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharacterMailDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterMailRepository,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }
    private val mailId: Long = checkNotNull(savedStateHandle[NAV_ARG_MAIL_ID]) {
        "Missing $NAV_ARG_MAIL_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CharacterMailDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load(indicateLoading = !_uiState.value.detailsReady)
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(indicateLoading = true)
    }

    private fun initialUiState(): CharacterMailDetailUiState {
        val cached = repository.cachedDetail(characterId, mailId)
        return if (cached != null) {
            CharacterMailDetailUiState(
                detail = cached,
                isLoading = false,
                loadFailed = false,
                detailsReady = true,
            )
        } else {
            CharacterMailDetailUiState(
                detail = repository.seedDetail(characterId, mailId),
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
            val result = runCatching { repository.loadDetail(characterId, mailId) }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { detail ->
                        current.copy(
                            detail = detail,
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
        const val NAV_ARG_MAIL_ID = "mailId"
    }
}

internal data class CharacterMailDetailUiState(
    val detail: CharacterMailDetail,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    val detailsReady: Boolean,
)
