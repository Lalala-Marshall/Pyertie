package com.marshall.pyerite.corporationModule.structures.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.corporationModule.structures.data.CorporationStructureFuelMonitorStore
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructure
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureFuelMonitor
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructuresAccessException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CorporationStructuresViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CorporationStructuresRepository,
    private val fuelMonitorStore: CorporationStructureFuelMonitorStore,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CorporationStructuresUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fuelMonitorStore.monitor().collect { monitor ->
                _uiState.update { it.copy(fuelMonitor = monitor) }
            }
        }
        if (repository.cachedStructures(characterId) == null) {
            load(forceRefresh = false)
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefresh = true)
    }

    fun setFuelMonitor(monitor: CorporationStructureFuelMonitor) {
        fuelMonitorStore.set(monitor)
    }

    private fun initialUiState(): CorporationStructuresUiState {
        val cached = repository.cachedStructures(characterId)
        val fuelMonitor = fuelMonitorStore.monitor().value
        return if (cached != null) {
            CorporationStructuresUiState(
                structures = cached.structures,
                fuelMonitor = fuelMonitor,
                isLoading = false,
            )
        } else {
            CorporationStructuresUiState(fuelMonitor = fuelMonitor)
        }
    }

    private fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false, permissionDenied = false) }
            val result = runCatching {
                repository.loadStructures(characterId, forceRefresh = forceRefresh)
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { snapshot ->
                        current.copy(
                            structures = snapshot.structures,
                            isLoading = false,
                            loadFailed = false,
                            permissionDenied = false,
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isLoading = false,
                            loadFailed = error !is CorporationStructuresAccessException,
                            permissionDenied = error is CorporationStructuresAccessException,
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

internal data class CorporationStructuresUiState(
    val structures: List<CorporationStructure> = emptyList(),
    val fuelMonitor: CorporationStructureFuelMonitor = CorporationStructureFuelMonitor.NONE,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val permissionDenied: Boolean = false,
)
