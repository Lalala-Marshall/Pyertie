package com.marshall.pyerite.loyaltyPointsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyFactionItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoyaltyFactionCorpsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: LoyaltyPointsRepository,
    private val localeController: LocaleController,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[LoyaltyPointsNavArgs.CHARACTER_ID]) {
        "Missing ${LoyaltyPointsNavArgs.CHARACTER_ID}"
    }
    private val factionId: Int = checkNotNull(savedStateHandle[LoyaltyPointsNavArgs.FACTION_ID]) {
        "Missing ${LoyaltyPointsNavArgs.FACTION_ID}"
    }

    private val _uiState = MutableStateFlow(LoyaltyFactionCorpsUiState())
    val uiState: StateFlow<LoyaltyFactionCorpsUiState> = _uiState.asStateFlow()

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

    fun refresh() {
        if (_uiState.value.isLoading) return
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val result = loyaltyPointsRunCatching {
                coroutineScope {
                    val factionDeferred = async { repository.loadFaction(factionId) }
                    val corpsDeferred = async { repository.loadCorporationsForFaction(factionId) }
                    factionDeferred.await() to corpsDeferred.await()
                }
            }
            loadedLanguage = localeController.contentLanguage
            _uiState.update { current ->
                result.fold(
                    onSuccess = { (faction, corporations) ->
                        current.copy(
                            faction = faction,
                            corporations = corporations,
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

internal data class LoyaltyFactionCorpsUiState(
    val faction: LoyaltyFactionItem? = null,
    val corporations: List<LoyaltyCorporationItem> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val detailsReady: Boolean = false,
)
