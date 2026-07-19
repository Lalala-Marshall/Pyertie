package com.marshall.pyerite.characterModule.viewModel

import com.marshall.pyerite.characterModule.auth.CharacterSelectionStore
import com.marshall.pyerite.characterModule.auth.EveSsoScope
import com.marshall.pyerite.characterModule.auth.EveTokenManager
import com.marshall.pyerite.characterModule.model.CharacterSummary
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CharacterRepository internal constructor(
    private val tokenManager: EveTokenManager,
    private val selectionStore: CharacterSelectionStore,
) {

    private val _currentCharacter = MutableStateFlow<CharacterSummary?>(null)
    val currentCharacter: StateFlow<CharacterSummary?> = _currentCharacter.asStateFlow()

    private val _loggedInCharacters = MutableStateFlow<List<LoggedInCharacter>>(emptyList())
    val loggedInCharacters: StateFlow<List<LoggedInCharacter>> = _loggedInCharacters.asStateFlow()

    /**
     * Stack of previous current characters (oldest at first, newest previous at last).
     * When the current character is removed, candidates are popped from the end
     * until one is still logged in and session-usable.
     */
    private val currentCharacterHistory = ArrayDeque<CharacterSummary>()

    fun setCurrentCharacter(character: CharacterSummary?) {
        pushCurrentToHistoryIfChanging(character?.characterId)
        pruneHistory(character?.characterId)
        _currentCharacter.value = character
        selectionStore.setCurrentCharacterId(character?.characterId)
    }

    /** Switches current character only when the id differs. */
    fun selectCurrentCharacter(character: LoggedInCharacter) {
        if (_currentCharacter.value?.characterId == character.characterId) return
        pushCurrentToHistoryIfChanging(character.characterId)
        pruneHistory(character.characterId)
        _currentCharacter.value = character.toSummary()
        selectionStore.setCurrentCharacterId(character.characterId)
    }

    fun upsertLoggedInCharacter(character: LoggedInCharacter) {
        val without = _loggedInCharacters.value.filterNot { it.characterId == character.characterId }
        _loggedInCharacters.value = without + character
    }

    /** Scopes for the current character from the in-memory logged-in list (no token access). */
    fun currentGrantedScopes(): Set<EveSsoScope> {
        val currentId = _currentCharacter.value?.characterId ?: return emptySet()
        return _loggedInCharacters.value
            .find { it.characterId == currentId }
            ?.grantedScopes
            .orEmpty()
    }

    fun removeLoggedInCharacter(characterId: Long) {
        val remaining = _loggedInCharacters.value.filterNot { it.characterId == characterId }
        _loggedInCharacters.value = remaining
        pruneHistory(characterId)

        if (_currentCharacter.value?.characterId != characterId) return

        val next = findUsableCurrentFromHistory(remaining)
            ?: remaining.firstOrNull()?.toSummary()
        _currentCharacter.value = next
        selectionStore.setCurrentCharacterId(next?.characterId)
    }

    /** After tokens restore the logged-in list, re-apply persisted current selection. */
    fun restoreCurrentCharacterSelection() {
        val savedId = selectionStore.getCurrentCharacterId() ?: return
        val match = _loggedInCharacters.value.find { it.characterId == savedId } ?: return
        if (!isSessionUsable(match)) return
        _currentCharacter.value = match.toSummary()
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

    private fun isSessionUsable(character: LoggedInCharacter): Boolean =
        tokenManager.hasStoredSession(character.characterId)
}
