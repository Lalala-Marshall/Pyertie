package com.marshall.pyerite.characterModule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterModule.auth.EveSsoAuthRepository
import com.marshall.pyerite.characterModule.auth.EveSsoUiStatus
import com.marshall.pyerite.characterModule.model.CharacterSummary
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CharacterViewModel(
    private val repository: CharacterRepository,
    private val authRepository: EveSsoAuthRepository,
) : ViewModel() {

    val currentCharacter: StateFlow<CharacterSummary?> = repository.currentCharacter
    val loggedInCharacters: StateFlow<List<LoggedInCharacter>> = repository.loggedInCharacters
    val isEditMode: StateFlow<Boolean> = repository.isEditMode
    val ssoStatus: StateFlow<EveSsoUiStatus> = authRepository.status

    private val _openAuthorizationUrl = Channel<String>(Channel.BUFFERED)
    val openAuthorizationUrl: Flow<String> = _openAuthorizationUrl.receiveAsFlow()

    fun toggleEditMode() = repository.toggleEditMode()

    fun selectCurrentCharacter(character: LoggedInCharacter) =
        repository.selectCurrentCharacter(character)

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
}
