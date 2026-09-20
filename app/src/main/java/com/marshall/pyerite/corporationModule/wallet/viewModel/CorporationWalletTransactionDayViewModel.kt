package com.marshall.pyerite.corporationModule.wallet.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletAccessException
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletMarketTransaction
import com.marshall.pyerite.corporationModule.wallet.model.mergeSimilarMarketTransactions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CorporationWalletTransactionDayViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CorporationWalletRepository,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }
    val division: Int = checkNotNull(savedStateHandle[NAV_ARG_DIVISION]) {
        "Missing $NAV_ARG_DIVISION"
    }
    val dayKey: String = checkNotNull(savedStateHandle[NAV_ARG_DAY_KEY]) {
        "Missing $NAV_ARG_DAY_KEY"
    }

    private var sourceTransactions: List<CorporationWalletMarketTransaction> =
        repository.cachedLedger(characterId, division)?.transactions.orEmpty()

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CorporationWalletTransactionDayUiState> = _uiState.asStateFlow()

    init {
        load(forceRefresh = repository.cachedLedger(characterId, division) == null)
        viewModelScope.launch {
            repository.mergeSimilarTransactions.collect { publishEntries() }
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefresh = true)
    }

    private fun initialUiState(): CorporationWalletTransactionDayUiState {
        val cached = repository.cachedLedger(characterId, division)
        return if (cached != null) {
            CorporationWalletTransactionDayUiState(
                dayKey = dayKey,
                entries = visibleEntries(
                    cached.transactions,
                    repository.mergeSimilarTransactions.value,
                ),
                isLoading = false,
            )
        } else {
            CorporationWalletTransactionDayUiState(dayKey = dayKey)
        }
    }

    private fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, loadFailed = false, permissionDenied = false)
            }
            val result = runCatching {
                repository.loadLedger(characterId, division, forceRefresh = forceRefresh)
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { ledger ->
                        sourceTransactions = ledger.transactions
                        current.copy(
                            entries = visibleEntries(
                                ledger.transactions,
                                repository.mergeSimilarTransactions.value,
                            ),
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

    private fun publishEntries() {
        _uiState.update {
            it.copy(
                entries = visibleEntries(
                    sourceTransactions,
                    repository.mergeSimilarTransactions.value,
                ),
            )
        }
    }

    private fun visibleEntries(
        transactions: List<CorporationWalletMarketTransaction>,
        mergeSimilar: Boolean,
    ): List<CorporationWalletMarketTransaction> {
        val dayEntries = transactions.filter { it.dayKey == dayKey }
        return if (mergeSimilar) mergeSimilarMarketTransactions(dayEntries) else dayEntries
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = "characterId"
        const val NAV_ARG_DIVISION = "division"
        const val NAV_ARG_DAY_KEY = "dayKey"
    }
}

internal data class CorporationWalletTransactionDayUiState(
    val dayKey: String,
    val entries: List<CorporationWalletMarketTransaction> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val permissionDenied: Boolean = false,
)
