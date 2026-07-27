package com.marshall.pyerite.characterClonesModule.viewModel

import com.marshall.pyerite.characterClonesModule.data.CharacterClonesCache
import com.marshall.pyerite.characterClonesModule.data.CharacterClonesLoader
import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class CharacterClonesRepository internal constructor(
    private val clonesLoader: CharacterClonesLoader,
    private val clonesCache: CharacterClonesCache,
) {
    private val statusByCharacterId = ConcurrentHashMap<Long, CharacterCloneStatus>()

    /** Memory first, then disk — for instant main-page hint / page paint. */
    fun cachedStatus(characterId: Long): CharacterCloneStatus? {
        statusByCharacterId[characterId]?.let { return it }
        return clonesCache.get(characterId)?.also { cached ->
            statusByCharacterId[characterId] = cached
        }
    }

    suspend fun loadStatus(characterId: Long): CharacterCloneStatus =
        withContext(Dispatchers.IO) {
            val loaded = clonesLoader.load(characterId)
            statusByCharacterId[characterId] = loaded
            clonesCache.save(loaded)
            loaded
        }
}
