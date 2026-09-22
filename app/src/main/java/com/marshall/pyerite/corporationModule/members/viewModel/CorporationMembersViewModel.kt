package com.marshall.pyerite.corporationModule.members.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.corporationModule.members.data.CorporationMemberWatchStore
import com.marshall.pyerite.corporationModule.members.model.CorporationMember
import com.marshall.pyerite.corporationModule.members.model.CorporationMemberSort
import com.marshall.pyerite.corporationModule.members.model.CorporationMembersAccessException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CorporationMembersViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CorporationMembersRepository,
    private val watchStore: CorporationMemberWatchStore,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CorporationMembersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            watchStore.watchIds(characterId).collect { ids ->
                _uiState.update { it.copy(watchedIds = ids) }
            }
        }
        if (repository.cachedMembers(characterId) == null) {
            load(forceRefresh = false)
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(forceRefresh = true)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSortChange(sort: CorporationMemberSort) {
        _uiState.update { it.copy(sort = sort) }
    }

    fun toggleWatch(memberId: Long) {
        watchStore.toggle(characterId, memberId)
    }

    private fun initialUiState(): CorporationMembersUiState {
        val cached = repository.cachedMembers(characterId)
        val watchedIds = watchStore.watchIds(characterId).value
        return if (cached != null) {
            CorporationMembersUiState(
                members = cached.members,
                watchedIds = watchedIds,
                isLoading = false,
            )
        } else {
            CorporationMembersUiState(watchedIds = watchedIds)
        }
    }

    private fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false, permissionDenied = false) }
            val result = runCatching {
                repository.loadMembers(characterId, forceRefresh = forceRefresh)
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { snapshot ->
                        current.copy(
                            members = snapshot.members,
                            isLoading = false,
                            loadFailed = false,
                            permissionDenied = false,
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isLoading = false,
                            loadFailed = error !is CorporationMembersAccessException,
                            permissionDenied = error is CorporationMembersAccessException,
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

internal data class CorporationMembersUiState(
    val members: List<CorporationMember> = emptyList(),
    val watchedIds: Set<Long> = emptySet(),
    val query: String = "",
    val sort: CorporationMemberSort = CorporationMemberSort.NAME,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val permissionDenied: Boolean = false,
)
