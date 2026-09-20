package com.marshall.pyerite.corporationModule.wallet.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletAccessException
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletConfig
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletDateFormatter
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletDayRow
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletDivisionTab
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletJournalEntry
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletLedger
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletMarketTransaction
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletPeriodSummary
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletSummaryWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CorporationWalletDivisionViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CorporationWalletRepository,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }
    val division: Int = checkNotNull(savedStateHandle[NAV_ARG_DIVISION]) {
        "Missing $NAV_ARG_DIVISION"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CorporationWalletDivisionUiState> = _uiState.asStateFlow()

    init {
        load(forceRefresh = repository.cachedLedger(characterId, division) == null)
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefresh = true)
    }

    fun onTabSelected(tab: CorporationWalletDivisionTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun cycleSummaryWindow() {
        _uiState.update { current ->
            val next = current.summaryWindow.next()
            current.copy(
                summaryWindow = next,
                periodSummary = summarizeJournal(current.journal, next),
            )
        }
    }

    private fun initialUiState(): CorporationWalletDivisionUiState {
        val cached = repository.cachedLedger(characterId, division)
        val divisionName = repository.cachedWallets(characterId)
            ?.divisions
            ?.firstOrNull { it.division == division }
            ?.name
        return if (cached != null) {
            stateFromLedger(
                ledger = cached,
                divisionName = divisionName,
                selectedTab = CorporationWalletDivisionTab.JOURNAL,
                summaryWindow = CorporationWalletSummaryWindow.DAYS_30,
            )
        } else {
            CorporationWalletDivisionUiState(divisionName = divisionName)
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
                        stateFromLedger(
                            ledger = ledger,
                            divisionName = current.divisionName,
                            selectedTab = current.selectedTab,
                            summaryWindow = current.summaryWindow,
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

    private fun stateFromLedger(
        ledger: CorporationWalletLedger,
        divisionName: String?,
        selectedTab: CorporationWalletDivisionTab,
        summaryWindow: CorporationWalletSummaryWindow,
    ): CorporationWalletDivisionUiState {
        return CorporationWalletDivisionUiState(
            divisionName = divisionName,
            selectedTab = selectedTab,
            summaryWindow = summaryWindow,
            journal = ledger.journal,
            transactions = ledger.transactions,
            journalDays = journalDays(ledger.journal),
            transactionDays = transactionDays(ledger.transactions),
            periodSummary = summarizeJournal(ledger.journal, summaryWindow),
            isLoading = false,
            loadFailed = false,
            permissionDenied = false,
        )
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = "characterId"
        const val NAV_ARG_DIVISION = "division"
    }
}

internal data class CorporationWalletDivisionUiState(
    val divisionName: String? = null,
    val selectedTab: CorporationWalletDivisionTab = CorporationWalletDivisionTab.JOURNAL,
    val summaryWindow: CorporationWalletSummaryWindow = CorporationWalletSummaryWindow.DAYS_30,
    val journal: List<CorporationWalletJournalEntry> = emptyList(),
    val transactions: List<CorporationWalletMarketTransaction> = emptyList(),
    val journalDays: List<CorporationWalletDayRow> = emptyList(),
    val transactionDays: List<CorporationWalletDayRow> = emptyList(),
    val periodSummary: CorporationWalletPeriodSummary = CorporationWalletPeriodSummary(
        income = CorporationWalletConfig.ZERO_ISK,
        expense = CorporationWalletConfig.ZERO_ISK,
        net = CorporationWalletConfig.ZERO_ISK,
    ),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val permissionDenied: Boolean = false,
)

internal fun summarizeJournal(
    entries: List<CorporationWalletJournalEntry>,
    window: CorporationWalletSummaryWindow,
    nowMs: Long = System.currentTimeMillis(),
): CorporationWalletPeriodSummary {
    var income = CorporationWalletConfig.ZERO_ISK
    var expense = CorporationWalletConfig.ZERO_ISK
    entries.forEach { entry ->
        if (!CorporationWalletDateFormatter.isWithinRollingDays(entry.dateEpochMs, window.days, nowMs)) {
            return@forEach
        }
        when {
            entry.amount > CorporationWalletConfig.ZERO_ISK -> income += entry.amount
            entry.amount < CorporationWalletConfig.ZERO_ISK -> expense += -entry.amount
        }
    }
    return CorporationWalletPeriodSummary(
        income = income,
        expense = expense,
        net = income - expense,
    )
}

internal fun journalDays(
    entries: List<CorporationWalletJournalEntry>,
): List<CorporationWalletDayRow> {
    return entries.groupBy { it.dayKey }
        .map { (dayKey, dayEntries) ->
            val income = dayEntries.fold(CorporationWalletConfig.ZERO_ISK) { acc, entry ->
                if (entry.amount > CorporationWalletConfig.ZERO_ISK) acc + entry.amount else acc
            }
            val expense = dayEntries.fold(CorporationWalletConfig.ZERO_ISK) { acc, entry ->
                if (entry.amount < CorporationWalletConfig.ZERO_ISK) acc - entry.amount else acc
            }
            CorporationWalletDayRow(
                dayKey = dayKey,
                dateEpochMs = CorporationWalletDateFormatter.dayStartEpochMs(dayKey),
                entryCount = dayEntries.size,
                buyCount = 0,
                sellCount = 0,
                netIsk = income - expense,
            )
        }
        .sortedByDescending { it.dayKey }
}

internal fun transactionDays(
    entries: List<CorporationWalletMarketTransaction>,
): List<CorporationWalletDayRow> {
    return entries.groupBy { it.dayKey }
        .map { (dayKey, dayEntries) ->
            CorporationWalletDayRow(
                dayKey = dayKey,
                dateEpochMs = CorporationWalletDateFormatter.dayStartEpochMs(dayKey),
                entryCount = dayEntries.size,
                buyCount = dayEntries.count { it.isBuy },
                sellCount = dayEntries.count { !it.isBuy },
                netIsk = dayEntries.sumOf { it.signedNet },
            )
        }
        .sortedByDescending { it.dayKey }
}
