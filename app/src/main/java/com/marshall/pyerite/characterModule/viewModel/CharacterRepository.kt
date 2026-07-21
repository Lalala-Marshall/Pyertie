package com.marshall.pyerite.characterModule.viewModel

import com.marshall.pyerite.characterModule.auth.CharacterOrderStore
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
    private val orderStore: CharacterOrderStore,
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

    /**
     * Existing id: update in place (display order unchanged).
     * New id: append last and extend the persisted manual order.
     */
    fun upsertLoggedInCharacter(character: LoggedInCharacter) {
        val current = _loggedInCharacters.value
        val existingIndex = current.indexOfFirst { it.characterId == character.characterId }
        if (existingIndex >= 0) {
            _loggedInCharacters.value = current.toMutableList().also { it[existingIndex] = character }
        } else {
            _loggedInCharacters.value = current + character
            appendToPersistedOrder(character.characterId)
        }
        if (_currentCharacter.value?.characterId == character.characterId) {
            _currentCharacter.value = character.toSummary()
        }
    }

    /**
     * After cold-start hydrate: show characters in the persisted manual order.
     * Does not invent a new order — only drops removed ids and appends unknown ids last.
     */
    fun applyPersistedCharacterOrder() {
        val current = _loggedInCharacters.value
        if (current.isEmpty()) return

        val byId = current.associateBy { it.characterId }
        val stored = orderStore.getOrderedIds()
        if (stored.isEmpty()) {
            // First run / empty prefs: seed with current list so later restarts have a baseline.
            orderStore.setOrderedIds(current.map { it.characterId })
            return
        }

        val storedSet = stored.toSet()
        val stillPresent = stored.filter { it in byId }
        val newlySeen = current.map { it.characterId }.filter { it !in storedSet }
        val nextOrder = stillPresent + newlySeen
        if (nextOrder != stored) {
            orderStore.setOrderedIds(nextOrder)
        }
        _loggedInCharacters.value = nextOrder.mapNotNull { byId[it] }
    }

    /** Manual drag reorder — the only path that rearranges existing characters. */
    fun reorderLoggedInCharacters(fromIndex: Int, toIndex: Int) {
        val current = _loggedInCharacters.value
        if (fromIndex == toIndex) return
        if (fromIndex !in current.indices || toIndex !in current.indices) return

        val reordered = current.toMutableList().also { list ->
            val moved = list.removeAt(fromIndex)
            list.add(toIndex, moved)
        }
        _loggedInCharacters.value = reordered
        orderStore.setOrderedIds(reordered.map { it.characterId })
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
        orderStore.setOrderedIds(orderStore.getOrderedIds().filterNot { it == characterId })
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

    private fun appendToPersistedOrder(characterId: Long) {
        val stored = orderStore.getOrderedIds()
        if (characterId in stored) return
        orderStore.setOrderedIds(stored + characterId)
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
