package com.marshall.pyerite.personalPropertyModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyCategory
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyDecoratedRanking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PersonalPropertyRankingViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PersonalPropertyRepository,
) : ViewModel() {

    private val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    val category: PersonalPropertyCategory = checkNotNull(
        savedStateHandle.get<String>(NAV_ARG_CATEGORY)?.let(PersonalPropertyCategory::fromRouteValue),
    ) {
        "Missing $NAV_ARG_CATEGORY"
    }

    private val _uiState = MutableStateFlow(PersonalPropertyRankingUiState())
    val uiState: StateFlow<PersonalPropertyRankingUiState> = _uiState.asStateFlow()

    init {
        loadRanking(forceRefresh = false)
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        loadRanking(forceRefresh = true)
    }

    private fun loadRanking(forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val result = runCatching {
                repository.loadRanking(
                    characterId = characterId,
                    category = category,
                    forceRefresh = forceRefresh,
                )
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { ranking ->
                        current.copy(
                            ranking = ranking,
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

    companion object {
        const val NAV_ARG_CHARACTER_ID = "characterId"
        const val NAV_ARG_CATEGORY = "category"
    }
}

internal data class PersonalPropertyRankingUiState(
    val ranking: PersonalPropertyDecoratedRanking = PersonalPropertyDecoratedRanking(
        priced = emptyList(),
        unpriced = emptyList(),
    ),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val detailsReady: Boolean = false,
)
