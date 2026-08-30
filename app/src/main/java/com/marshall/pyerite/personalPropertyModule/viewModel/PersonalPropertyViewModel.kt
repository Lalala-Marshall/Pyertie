package com.marshall.pyerite.personalPropertyModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PersonalPropertyViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PersonalPropertyRepository,
) : ViewModel() {

    private val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<PersonalPropertyUiState> = _uiState.asStateFlow()

    init {
        if (!_uiState.value.detailsReady) {
            loadSummary(forceRefresh = false)
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        loadSummary(forceRefresh = true)
    }

    private fun initialUiState(): PersonalPropertyUiState {
        val cached = repository.cachedSummary(characterId)
        return if (cached != null) {
            PersonalPropertyUiState(
                summary = cached,
                isLoading = false,
                loadFailed = !cached.hasAnyValue(),
                detailsReady = true,
            )
        } else {
            PersonalPropertyUiState(
                summary = repository.seedSummary(characterId),
                isLoading = true,
                loadFailed = false,
                detailsReady = false,
            )
        }
    }

    private fun loadSummary(forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val result = runCatching {
                repository.loadSummary(characterId, forceRefresh = forceRefresh)
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { summary ->
                        current.copy(
                            summary = summary,
                            isLoading = false,
                            loadFailed = !summary.hasAnyValue(),
                            detailsReady = true,
                        )
                    },
                    onFailure = {
                        current.copy(isLoading = false, loadFailed = true)
                    },
                )
            }
        }
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = "characterId"
    }
}

internal data class PersonalPropertyUiState(
    val summary: PersonalPropertySummary,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    /** True after at least one successful summary load (or cache hit). */
    val detailsReady: Boolean,
)
