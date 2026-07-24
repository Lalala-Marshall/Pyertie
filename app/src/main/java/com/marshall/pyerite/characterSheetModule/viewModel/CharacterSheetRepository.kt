package com.marshall.pyerite.characterSheetModule.viewModel

import com.marshall.pyerite.characterSheetModule.data.CharacterSheetLoader
import com.marshall.pyerite.characterSheetModule.model.CharacterSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CharacterSheetRepository(
    private val sheetLoader: CharacterSheetLoader,
) {
    private val sheetByCharacterId = ConcurrentHashMap<Long, CharacterSheet>()

    fun seedSheet(characterId: Long): CharacterSheet {
        return sheetByCharacterId[characterId]
            ?: CharacterSheet.seed(characterId)
    }

    fun cachedSheet(characterId: Long): CharacterSheet? = sheetByCharacterId[characterId]

    /**
     * @param forceRefresh when true, always hits ESI and replaces the cache entry
     * (error retry). Otherwise returns the in-memory sheet for [characterId] if present.
     */
    suspend fun loadSheet(
        characterId: Long,
        forceRefresh: Boolean = false,
    ): CharacterSheet = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            sheetByCharacterId[characterId]?.let { cached ->
                if (!cached.missingTypeIcons()) {
                    return@withContext cached
                }
            }
        }
        val fallbackName = sheetByCharacterId[characterId]?.name.orEmpty()
        val loaded = sheetLoader.load(characterId, fallbackName)
        sheetByCharacterId[characterId] = loaded
        loaded
    }
}
