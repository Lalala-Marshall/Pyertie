package com.marshall.pyerite.corporationModule.wallet.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletAccessException
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletJournalEntry
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletJournalFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CorporationWalletJournalDayViewModel(
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

    private var sourceJournal: List<CorporationWalletJournalEntry> =
        repository.cachedLedger(characterId, division)?.journal.orEmpty()

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CorporationWalletJournalDayUiState> = _uiState.asStateFlow()

    init {
        load(forceRefresh = repository.cachedLedger(characterId, division) == null)
        viewModelScope.launch {
            repository.journalFilter.collect { publishEntries() }
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefresh = true)
    }

    private fun initialUiState(): CorporationWalletJournalDayUiState {
        val cached = repository.cachedLedger(characterId, division)
        return if (cached != null) {
            CorporationWalletJournalDayUiState(
                dayKey = dayKey,
                entries = visibleEntries(cached.journal, repository.journalFilter.value),
                isLoading = false,
            )
        } else {
            CorporationWalletJournalDayUiState(dayKey = dayKey)
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
                        sourceJournal = ledger.journal
                        current.copy(
                            entries = visibleEntries(ledger.journal, repository.journalFilter.value),
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
        val filter = repository.journalFilter.value
        _uiState.update { it.copy(entries = visibleEntries(sourceJournal, filter)) }
    }

    private fun visibleEntries(
        journal: List<CorporationWalletJournalEntry>,
        filter: CorporationWalletJournalFilter,
    ): List<CorporationWalletJournalEntry> {
        return journal.filter { entry ->
            entry.dayKey == dayKey && filter.matches(entry)
        }
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = "characterId"
        const val NAV_ARG_DIVISION = "division"
        const val NAV_ARG_DAY_KEY = "dayKey"
    }
}

internal data class CorporationWalletJournalDayUiState(
    val dayKey: String,
    val entries: List<CorporationWalletJournalEntry> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val permissionDenied: Boolean = false,
)
