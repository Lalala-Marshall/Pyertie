package com.marshall.pyerite.characterModule.viewModel

import com.marshall.pyerite.characterModule.model.CharacterSummary
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CharacterRepository {

    private val _currentCharacter = MutableStateFlow<CharacterSummary?>(null)
    val currentCharacter: StateFlow<CharacterSummary?> = _currentCharacter.asStateFlow()

    private val _loggedInCharacters = MutableStateFlow<List<LoggedInCharacter>>(emptyList())
    val loggedInCharacters: StateFlow<List<LoggedInCharacter>> = _loggedInCharacters.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    /**
     * Stack of previous current characters (oldest at first, newest previous at last).
     * When the current character is removed, candidates are popped from the end
     * until one is still logged in and session-usable.
     */
    private val currentCharacterHistory = ArrayDeque<CharacterSummary>()

    fun toggleEditMode() {
        _isEditMode.update { !it }
    }

    fun setCurrentCharacter(character: CharacterSummary?) {
        pushCurrentToHistoryIfChanging(character?.characterId)
        pruneHistory(character?.characterId)
        _currentCharacter.value = character
    }

    /** Switches current character only when the id differs. */
    fun selectCurrentCharacter(character: LoggedInCharacter) {
        if (_currentCharacter.value?.characterId == character.characterId) return
        pushCurrentToHistoryIfChanging(character.characterId)
        pruneHistory(character.characterId)
        _currentCharacter.value = character.toSummary()
    }

    fun removeLoggedInCharacter(characterId: Long) {
        val remaining = _loggedInCharacters.value.filterNot { it.characterId == characterId }
        _loggedInCharacters.value = remaining
        pruneHistory(characterId)

        if (_currentCharacter.value?.characterId != characterId) return

        _currentCharacter.value = findUsableCurrentFromHistory(remaining)
            ?: remaining.firstOrNull()?.toSummary()
    }

    private fun pushCurrentToHistoryIfChanging(nextCharacterId: Long?) {
        val current = _currentCharacter.value ?: return
        if (current.characterId == nextCharacterId) return
        currentCharacterHistory.addLast(current)
    }

    private fun pruneHistory(excludedCharacterId: Long?) {
        if (excludedCharacterId == null) return
        currentCharacterHistory.removeAll { it.characterId == excludedCharacterId }
    }

    private fun findUsableCurrentFromHistory(
        remaining: List<LoggedInCharacter>,
    ): CharacterSummary? {
        while (currentCharacterHistory.isNotEmpty()) {
            val previous = currentCharacterHistory.removeLast()
            val loggedIn = remaining.find { it.characterId == previous.characterId }
            if (loggedIn != null && isSessionUsable(loggedIn)) {
                return loggedIn.toSummary()
            }
        }
        return null
    }

    private fun LoggedInCharacter.toSummary(): CharacterSummary = CharacterSummary(
        characterId = characterId,
        name = name,
        portraitUrl = portraitUrl,
        corporationName = corporationName,
        corporationIconUrl = corporationIconUrl,
        allianceName = allianceName,
        allianceIconUrl = allianceIconUrl,
    )

    /**
     * Whether this logged-in session can become currentCharacter.
     * Token expiry / refresh failure will be wired here with OAuth.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun isSessionUsable(character: LoggedInCharacter): Boolean = true
}
