package com.marshall.pyerite.characterModule.viewModel

import androidx.lifecycle.ViewModel
import com.marshall.pyerite.characterModule.model.CharacterSummary
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import kotlinx.coroutines.flow.StateFlow

class CharacterViewModel(
    private val repository: CharacterRepository,
) : ViewModel() {

    val currentCharacter: StateFlow<CharacterSummary?> = repository.currentCharacter
    val loggedInCharacters: StateFlow<List<LoggedInCharacter>> = repository.loggedInCharacters
    val isEditMode: StateFlow<Boolean> = repository.isEditMode

    fun toggleEditMode() = repository.toggleEditMode()

    fun selectCurrentCharacter(character: LoggedInCharacter) =
        repository.selectCurrentCharacter(character)

    fun clearCurrentCharacter() = repository.setCurrentCharacter(null)

    fun removeLoggedInCharacter(characterId: Long) =
        repository.removeLoggedInCharacter(characterId)

    fun onAddCharacterClicked() {
        // OAuth flow will be wired here in the next phase.
    }
}
