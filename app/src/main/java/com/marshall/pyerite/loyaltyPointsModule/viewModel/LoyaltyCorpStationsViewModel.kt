package com.marshall.pyerite.loyaltyPointsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyStationRegionGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoyaltyCorpStationsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: LoyaltyPointsRepository,
    private val localeController: LocaleController,
) : ViewModel() {

    private val corporationId: Long =
        checkNotNull(savedStateHandle[LoyaltyPointsNavArgs.CORPORATION_ID]) {
            "Missing ${LoyaltyPointsNavArgs.CORPORATION_ID}"
        }

    private val _uiState = MutableStateFlow(LoyaltyCorpStationsUiState())
    val uiState: StateFlow<LoyaltyCorpStationsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var loadedLanguage: ContentLanguage? = null

    init {
        load()
    }

    fun reloadForLanguage() {
        val current = localeController.contentLanguage
        if (loadedLanguage == null || loadedLanguage == current) return
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val result = loyaltyPointsRunCatching {
                coroutineScope {
                    val corpDeferred = async { repository.loadCorporation(corporationId) }
                    val stationsDeferred = async { repository.loadStations(corporationId) }
                    corpDeferred.await() to stationsDeferred.await()
                }
            }
            loadedLanguage = localeController.contentLanguage
            _uiState.update { current ->
                result.fold(
                    onSuccess = { (corporation, regions) ->
                        current.copy(
                            corporation = corporation,
                            regions = regions,
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
}

internal data class LoyaltyCorpStationsUiState(
    val corporation: LoyaltyCorporationItem? = null,
    val regions: List<LoyaltyStationRegionGroup> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val detailsReady: Boolean = false,
)
