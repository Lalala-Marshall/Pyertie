package com.marshall.pyerite.loyaltyPointsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferCategoryItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoyaltyCorpDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: LoyaltyPointsRepository,
    private val localeController: LocaleController,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[LoyaltyPointsNavArgs.CHARACTER_ID]) {
        "Missing ${LoyaltyPointsNavArgs.CHARACTER_ID}"
    }
    val corporationId: Long = checkNotNull(savedStateHandle[LoyaltyPointsNavArgs.CORPORATION_ID]) {
        "Missing ${LoyaltyPointsNavArgs.CORPORATION_ID}"
    }

    private val _uiState = MutableStateFlow(LoyaltyCorpDetailUiState())
    val uiState: StateFlow<LoyaltyCorpDetailUiState> = _uiState.asStateFlow()

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
                    val categoriesDeferred = async { repository.loadOfferCategories(corporationId) }
                    corpDeferred.await() to categoriesDeferred.await()
                }
            }
            loadedLanguage = localeController.contentLanguage
            _uiState.update { current ->
                result.fold(
                    onSuccess = { (corporation, categories) ->
                        current.copy(
                            corporation = corporation,
                            categories = categories,
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

internal data class LoyaltyCorpDetailUiState(
    val corporation: LoyaltyCorporationItem? = null,
    val categories: List<LoyaltyOfferCategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val detailsReady: Boolean = false,
)
