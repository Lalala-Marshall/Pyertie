package com.marshall.pyerite.corporationModule.wallet.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletAccessException
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletDivision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CorporationWalletsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CorporationWalletRepository,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CorporationWalletsUiState> = _uiState.asStateFlow()

    init {
        load(forceRefresh = _uiState.value.divisions.isEmpty())
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefresh = true)
    }

    private fun initialUiState(): CorporationWalletsUiState {
        val cached = repository.cachedWallets(characterId)
        return if (cached != null) {
            CorporationWalletsUiState(
                divisions = cached.divisions,
                isLoading = false,
                loadFailed = false,
                permissionDenied = false,
            )
        } else {
            CorporationWalletsUiState()
        }
    }

    private fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false, permissionDenied = false) }
            val result = runCatching {
                repository.loadWallets(characterId, forceRefresh = forceRefresh)
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { snapshot ->
                        current.copy(
                            divisions = snapshot.divisions,
                            isLoading = false,
                            loadFailed = false,
                            permissionDenied = false,
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isLoading = false,
                            loadFailed = error !is CorporationWalletAccessException,
                            permissionDenied = error is CorporationWalletAccessException,
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

internal data class CorporationWalletsUiState(
    val divisions: List<CorporationWalletDivision> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val permissionDenied: Boolean = false,
)
