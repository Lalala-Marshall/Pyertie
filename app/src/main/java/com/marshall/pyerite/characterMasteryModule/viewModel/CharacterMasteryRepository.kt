package com.marshall.pyerite.characterMasteryModule.viewModel

import com.marshall.pyerite.characterMasteryModule.data.CharacterMasteryLoader
import com.marshall.pyerite.characterMasteryModule.model.CharacterMasterySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CharacterMasteryRepository(
    private val loader: CharacterMasteryLoader,
) {
    private val skillsByCharacterId = ConcurrentHashMap<Long, Map<Int, Int>>()
    @Volatile
    private var catalogLanguageToken: Any? = null

    @Volatile
    private var cachedSnapshot: Pair<Long, CharacterMasterySnapshot>? = null

    fun invalidateCatalog() {
        catalogLanguageToken = null
        cachedSnapshot = null
    }

    suspend fun load(
        characterId: Long,
        languageToken: Any,
        forceRefresh: Boolean,
    ): CharacterMasterySnapshot = withContext(Dispatchers.IO) {
        if (forceRefresh) {
            skillsByCharacterId.remove(characterId)
            cachedSnapshot = null
        }
        val skills = skillsByCharacterId[characterId] ?: loader.loadSkills(characterId).also {
            skillsByCharacterId[characterId] = it
        }
        val cached = cachedSnapshot
        if (
            cached != null &&
            cached.first == characterId &&
            catalogLanguageToken == languageToken
        ) {
            return@withContext cached.second
        }
        val snapshot = loader.loadCatalog(skills)
        catalogLanguageToken = languageToken
        cachedSnapshot = characterId to snapshot
        snapshot
    }
}
