package com.marshall.pyerite.characterClonesModule.viewModel

import com.marshall.pyerite.characterClonesModule.data.CharacterClonesLoader
import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class CharacterClonesRepository internal constructor(
    private val clonesLoader: CharacterClonesLoader,
) {
    private val statusByCharacterId = ConcurrentHashMap<Long, CharacterCloneStatus>()

    fun cachedStatus(characterId: Long): CharacterCloneStatus? = statusByCharacterId[characterId]

    suspend fun loadStatus(
        characterId: Long,
        forceRefresh: Boolean = false,
    ): CharacterCloneStatus = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            statusByCharacterId[characterId]?.let { return@withContext it }
        }
        val loaded = clonesLoader.load(characterId)
        statusByCharacterId[characterId] = loaded
        loaded
    }
}
