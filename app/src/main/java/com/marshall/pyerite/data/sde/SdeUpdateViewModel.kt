package com.marshall.pyerite.data.sde

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SdeUpdateUiState {
    data object Idle : SdeUpdateUiState
    data object Checking : SdeUpdateUiState
    data class UpdateAvailable(val buildNumber: String) : SdeUpdateUiState
    data object UpToDate : SdeUpdateUiState
    data class Downloading(val progressPercent: Int) : SdeUpdateUiState
    data object Applying : SdeUpdateUiState
    data object Completed : SdeUpdateUiState
    data class Failed(val message: String) : SdeUpdateUiState
}

class SdeUpdateViewModel(
    private val repository: SdeUpdateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SdeUpdateUiState>(SdeUpdateUiState.Idle)
    val uiState: StateFlow<SdeUpdateUiState> = _uiState.asStateFlow()

    private var pendingUpdate: SdeRemotePackage? = null

    fun checkForUpdates() {
        if (_uiState.value is SdeUpdateUiState.Checking || _uiState.value is SdeUpdateUiState.Downloading) return
        viewModelScope.launch {
            _uiState.value = SdeUpdateUiState.Checking
            runCatching { repository.checkForRemoteUpdate() }
                .onSuccess { remote ->
                    if (remote == null) {
                        pendingUpdate = null
                        _uiState.value = SdeUpdateUiState.UpToDate
                    } else {
                        pendingUpdate = remote
                        _uiState.value = SdeUpdateUiState.UpdateAvailable(remote.meta.buildNumber)
                    }
                }
                .onFailure { error ->
                    _uiState.value = SdeUpdateUiState.Failed(error.message.orEmpty())
                }
        }
    }

    fun downloadUpdate() {
        val remote = pendingUpdate ?: return
        if (_uiState.value is SdeUpdateUiState.Downloading) return
        viewModelScope.launch {
            _uiState.value = SdeUpdateUiState.Downloading(0)
            repository.downloadAndApply(remote) { progress ->
                _uiState.value = SdeUpdateUiState.Downloading(progress)
            }.onSuccess {
                pendingUpdate = null
                _uiState.value = SdeUpdateUiState.Completed
            }.onFailure { error ->
                _uiState.value = SdeUpdateUiState.Failed(error.message.orEmpty())
            }
        }
    }

    fun dismissCompleted() {
        if (_uiState.value is SdeUpdateUiState.Completed) {
            _uiState.value = SdeUpdateUiState.UpToDate
        }
    }

    fun dismissError() {
        if (_uiState.value is SdeUpdateUiState.Failed) {
            _uiState.value = if (pendingUpdate != null) {
                SdeUpdateUiState.UpdateAvailable(pendingUpdate!!.meta.buildNumber)
            } else {
                SdeUpdateUiState.Idle
            }
        }
    }
}
