package com.marshall.pyerite.characterSheetModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterSheetModule.model.CharacterSheet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharacterSheetViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterSheetRepository,
) : ViewModel() {

    private val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CharacterSheetUiState> = _uiState.asStateFlow()

    init {
        if (!_uiState.value.detailsReady) {
            loadSheet(forceRefresh = false)
        }
    }

    fun refresh() {
        loadSheet(forceRefresh = true)
    }

    private fun initialUiState(): CharacterSheetUiState {
        val cached = repository.cachedSheet(characterId)
        return if (cached != null && !cached.missingTypeIcons()) {
            CharacterSheetUiState(
                sheet = cached,
                isLoading = false,
                loadFailed = false,
                detailsReady = true,
            )
        } else {
            CharacterSheetUiState(
                sheet = cached ?: repository.seedSheet(characterId),
                isLoading = true,
                loadFailed = false,
                detailsReady = false,
            )
        }
    }

    private fun loadSheet(forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val result = runCatching {
                repository.loadSheet(characterId, forceRefresh = forceRefresh)
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { sheet ->
                        current.copy(
                            sheet = sheet,
                            isLoading = false,
                            loadFailed = false,
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

data class CharacterSheetUiState(
    val sheet: CharacterSheet,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    /** True after at least one successful sheet ESI load (or cache hit). */
    val detailsReady: Boolean,
)

/** True when a fully loaded sheet is missing local type icons (stale cache). */
internal fun CharacterSheet.missingTypeIcons(): Boolean {
    val detailsWereLoaded = birthdayEpochMs != null ||
        shipTypeId != null ||
        securityStatus != null
    if (!detailsWereLoaded) return false
    val shipMissing = shipTypeId != null && shipIconFilename.isNullOrBlank()
    val locationMissing = location != null && (
        location.placeTypeId == null || location.placeIconFilename.isNullOrBlank()
    )
    return shipMissing || locationMissing
}
