package com.marshall.pyerite.loyaltyPointsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferItem
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoyaltyCorpOffersViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: LoyaltyPointsRepository,
    private val localeController: LocaleController,
) : ViewModel() {

    private val corporationId: Long =
        checkNotNull(savedStateHandle[LoyaltyPointsNavArgs.CORPORATION_ID]) {
            "Missing ${LoyaltyPointsNavArgs.CORPORATION_ID}"
        }
    private val categoryId: Int = checkNotNull(savedStateHandle[LoyaltyPointsNavArgs.CATEGORY_ID]) {
        "Missing ${LoyaltyPointsNavArgs.CATEGORY_ID}"
    }

    private val _uiState = MutableStateFlow(LoyaltyCorpOffersUiState())
    val uiState: StateFlow<LoyaltyCorpOffersUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var loadedLanguage: ContentLanguage? = null

    init {
        load(forceRefreshPrices = false)
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefreshPrices = true)
    }

    fun reloadForLanguage() {
        val current = localeController.contentLanguage
        if (loadedLanguage == null || loadedLanguage == current) return
        load(forceRefreshPrices = false)
    }

    private fun load(forceRefreshPrices: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val result = loyaltyPointsRunCatching {
                coroutineScope {
                    val categoryDeferred = async { repository.loadCategory(categoryId) }
                    val offersDeferred = async {
                        repository.loadOffers(
                            corporationId = corporationId,
                            categoryId = categoryId,
                            forceRefreshPrices = forceRefreshPrices,
                        )
                    }
                    categoryDeferred.await() to offersDeferred.await()
                }
            }
            loadedLanguage = localeController.contentLanguage
            _uiState.update { current ->
                result.fold(
                    onSuccess = { (category, offers) ->
                        current.copy(
                            category = category,
                            offers = offers,
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

internal data class LoyaltyCorpOffersUiState(
    val category: CategoryEntity? = null,
    val offers: List<LoyaltyOfferItem> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val detailsReady: Boolean = false,
)
