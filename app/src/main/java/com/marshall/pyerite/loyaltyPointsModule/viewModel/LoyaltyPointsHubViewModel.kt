package com.marshall.pyerite.loyaltyPointsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationBalance
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoyaltyPointsHubViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: LoyaltyPointsRepository,
    private val localeController: LocaleController,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[LoyaltyPointsNavArgs.CHARACTER_ID]) {
        "Missing ${LoyaltyPointsNavArgs.CHARACTER_ID}"
    }

    private val _uiState = MutableStateFlow(LoyaltyPointsHubUiState())
    val uiState: StateFlow<LoyaltyPointsHubUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var loadedLanguage: ContentLanguage? = null

    init {
        load(forceRefresh = false)
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefresh = true)
    }

    fun reloadForLanguage() {
        val current = localeController.contentLanguage
        if (loadedLanguage == null || loadedLanguage == current) return
        load(forceRefresh = false)
    }

    private fun load(forceRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val result = loyaltyPointsRunCatching {
                repository.loadBalances(characterId, forceRefresh = forceRefresh)
            }
            loadedLanguage = localeController.contentLanguage
            _uiState.update { current ->
                result.fold(
                    onSuccess = { balances ->
                        current.copy(
                            balances = balances,
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

internal data class LoyaltyPointsHubUiState(
    val balances: List<LoyaltyCorporationBalance> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val detailsReady: Boolean = false,
)
