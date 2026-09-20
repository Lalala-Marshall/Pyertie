package com.marshall.pyerite.characterMasteryModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterMasteryModule.model.CharacterMasterySnapshot
import com.marshall.pyerite.characterMasteryModule.model.MasteryFilter
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharacterMasteryOverviewViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterMasteryRepository,
    private val localeController: LocaleController,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[CharacterMasteryNavArgs.CHARACTER_ID]) {
        "Missing ${CharacterMasteryNavArgs.CHARACTER_ID}"
    }

    private val _uiState = MutableStateFlow(CharacterMasteryOverviewUiState())
    val uiState: StateFlow<CharacterMasteryOverviewUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var loadedLanguage: ContentLanguage? = null

    init {
        load(forceRefresh = false)
    }

    fun setFilter(filter: MasteryFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefresh = true)
    }

    fun reloadForLanguage() {
        val current = localeController.contentLanguage
        if (loadedLanguage == null || loadedLanguage == current) return
        repository.invalidateCatalog()
        load(forceRefresh = false)
    }

    private fun load(forceRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val result = runCatching {
                repository.load(
                    characterId = characterId,
                    languageToken = localeController.contentLanguage,
                    forceRefresh = forceRefresh,
                )
            }
            loadedLanguage = localeController.contentLanguage
            _uiState.update { current ->
                result.fold(
                    onSuccess = { snapshot ->
                        current.copy(
                            snapshot = snapshot,
                            isLoading = false,
                            loadFailed = false,
                            detailsReady = true,
                        )
                    },
                    onFailure = {
                        current.copy(
                            isLoading = false,
                            loadFailed = true,
                        )
                    },
                )
            }
        }
    }
}

internal data class CharacterMasteryOverviewUiState(
    val snapshot: CharacterMasterySnapshot? = null,
    val filter: MasteryFilter = MasteryFilter.ALL,
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val detailsReady: Boolean = false,
)
