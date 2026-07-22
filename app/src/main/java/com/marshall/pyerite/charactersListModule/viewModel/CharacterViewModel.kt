package com.marshall.pyerite.charactersListModule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.charactersListModule.auth.EveSsoAuthRepository
import com.marshall.pyerite.charactersListModule.auth.EveSsoUiStatus
import com.marshall.pyerite.charactersListModule.model.CharacterSummary
import com.marshall.pyerite.charactersListModule.model.LoggedInCharacter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterViewModel(
    private val repository: CharacterRepository,
    private val authRepository: EveSsoAuthRepository,
) : ViewModel() {

    val currentCharacter: StateFlow<CharacterSummary?> = repository.currentCharacter
    val loggedInCharacters: StateFlow<List<LoggedInCharacter>> = repository.loggedInCharacters
    val ssoStatus: StateFlow<EveSsoUiStatus> = authRepository.status
    val isRefreshing: StateFlow<Boolean> = authRepository.isRefreshing
    val refreshFailed: StateFlow<Boolean> = authRepository.refreshFailed

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _openAuthorizationUrl = Channel<String>(Channel.BUFFERED)
    val openAuthorizationUrl: Flow<String> = _openAuthorizationUrl.receiveAsFlow()

    fun toggleEditMode() {
        _isEditMode.update { !it }
    }

    fun refreshLoggedInCharacters() {
        authRepository.requestLoggedInProfilesRefresh()
    }

    fun selectCurrentCharacter(character: LoggedInCharacter) =
        repository.selectCurrentCharacter(character)

    fun reorderLoggedInCharacters(fromIndex: Int, toIndex: Int) =
        repository.reorderLoggedInCharacters(fromIndex, toIndex)

    fun clearCurrentCharacter() = repository.setCurrentCharacter(null)

    fun removeLoggedInCharacter(characterId: Long) {
        viewModelScope.launch {
            authRepository.removeCharacterSession(characterId)
        }
    }

    fun onAddCharacterClicked() {
        viewModelScope.launch {
            val url = authRepository.prepareLogin() ?: return@launch
            _openAuthorizationUrl.send(url)
        }
    }

    fun clearSsoStatus() = authRepository.clearStatus()

    fun cancelSsoLogin() = authRepository.cancelLogin()

    /** Scope gates for upcoming feature UI — prefer over raw token access. */
    @Suppress("unused")
    fun currentGrantedScopes() = repository.currentGrantedScopes()
}
