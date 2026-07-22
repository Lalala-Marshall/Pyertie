package com.marshall.pyerite.characterSheetModule.viewModel

import com.marshall.pyerite.characterSheetModule.data.CharacterSheetLoader
import com.marshall.pyerite.characterSheetModule.model.CharacterSheet
import com.marshall.pyerite.charactersListModule.viewModel.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CharacterSheetRepository(
    private val sheetLoader: CharacterSheetLoader,
    private val characterRepository: CharacterRepository,
) {
    private val sheetByCharacterId = ConcurrentHashMap<Long, CharacterSheet>()

    fun seedSheet(characterId: Long): CharacterSheet {
        pruneRemovedCharacters()
        val cached = characterRepository.loggedInCharacters.value
            .find { it.characterId == characterId }
        return CharacterSheet.seed(characterId, cached)
    }

    fun cachedSheet(characterId: Long): CharacterSheet? {
        pruneRemovedCharacters()
        return sheetByCharacterId[characterId]
    }

    fun clearCachedSheet(characterId: Long) {
        sheetByCharacterId.remove(characterId)
    }

    /**
     * @param forceRefresh when true, always hits ESI and replaces the cache entry
     * (error retry). Otherwise returns the in-memory sheet for [characterId] if present.
     */
    suspend fun loadSheet(
        characterId: Long,
        forceRefresh: Boolean = false,
    ): CharacterSheet = withContext(Dispatchers.IO) {
        pruneRemovedCharacters()
        if (!forceRefresh) {
            sheetByCharacterId[characterId]?.let { return@withContext it }
        }
        val fallbackName = characterRepository.loggedInCharacters.value
            .find { it.characterId == characterId }
            ?.name
            .orEmpty()
        val loaded = sheetLoader.load(characterId, fallbackName)
        sheetByCharacterId[characterId] = loaded
        loaded
    }

    /** Drop sheet cache for characters no longer in the logged-in list. */
    private fun pruneRemovedCharacters() {
        val validIds = characterRepository.loggedInCharacters.value
            .mapTo(HashSet()) { it.characterId }
        sheetByCharacterId.keys
            .filter { it !in validIds }
            .forEach { clearCachedSheet(it) }
    }
}
