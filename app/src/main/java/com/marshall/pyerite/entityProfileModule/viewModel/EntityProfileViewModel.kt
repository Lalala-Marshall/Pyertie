package com.marshall.pyerite.entityProfileModule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.entityProfileModule.data.EntityProfileLoader
import com.marshall.pyerite.entityProfileModule.model.EntityProfile
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class EntityProfileViewModel(
    private val loader: EntityProfileLoader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntityProfileUiState())
    val uiState: StateFlow<EntityProfileUiState> = _uiState.asStateFlow()

    fun open(ref: UniverseEntityRef, viewerCharacterId: Long) {
        _uiState.value = EntityProfileUiState(
            stack = listOf(EntityProfilePageState(ref = ref, viewerCharacterId = viewerCharacterId)),
        )
        loadPage(index = 0, ref = ref, viewerCharacterId = viewerCharacterId)
    }

    fun openChild(ref: UniverseEntityRef) {
        val viewerCharacterId = _uiState.value.stack.lastOrNull()?.viewerCharacterId ?: return
        if (_uiState.value.stack.lastOrNull()?.ref == ref) return
        _uiState.update { state ->
            state.copy(
                stack = state.stack + EntityProfilePageState(
                    ref = ref,
                    viewerCharacterId = viewerCharacterId,
                ),
            )
        }
        loadPage(
            index = _uiState.value.stack.lastIndex,
            ref = ref,
            viewerCharacterId = viewerCharacterId,
        )
    }

    fun pop() {
        _uiState.update { state ->
            if (state.stack.size <= 1) {
                EntityProfileUiState()
            } else {
                state.copy(stack = state.stack.dropLast(1))
            }
        }
    }

    fun dismiss() {
        _uiState.value = EntityProfileUiState()
    }

    fun retry() {
        val page = _uiState.value.current ?: return
        val index = _uiState.value.stack.lastIndex
        loadPage(index = index, ref = page.ref, viewerCharacterId = page.viewerCharacterId)
    }

    private fun loadPage(
        index: Int,
        ref: UniverseEntityRef,
        viewerCharacterId: Long,
    ) {
        viewModelScope.launch {
            updatePage(index) { it.copy(isLoading = true, loadFailed = false) }
            val headerResult = runCatching { loader.loadHeader(ref) }
            val header = headerResult.getOrNull()
            if (header == null) {
                updatePage(index) { it.copy(isLoading = false, loadFailed = true) }
                return@launch
            }
            updatePage(index) {
                it.copy(profile = header, isLoading = false, loadFailed = false)
            }
            val detailed = runCatching {
                loader.loadDetails(header, viewerCharacterId)
            }.getOrDefault(header.copy(detailsReady = true))
            updatePage(index) { it.copy(profile = detailed) }
        }
    }

    private fun updatePage(index: Int, transform: (EntityProfilePageState) -> EntityProfilePageState) {
        _uiState.update { state ->
            if (index !in state.stack.indices) return@update state
            val next = state.stack.toMutableList()
            next[index] = transform(next[index])
            state.copy(stack = next)
        }
    }
}

internal data class EntityProfileUiState(
    val stack: List<EntityProfilePageState> = emptyList(),
) {
    val current: EntityProfilePageState? get() = stack.lastOrNull()
    val canPop: Boolean get() = stack.size > 1
}

internal data class EntityProfilePageState(
    val ref: UniverseEntityRef,
    val viewerCharacterId: Long,
    val profile: EntityProfile? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)
