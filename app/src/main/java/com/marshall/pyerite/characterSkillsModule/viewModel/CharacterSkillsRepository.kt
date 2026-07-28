package com.marshall.pyerite.characterSkillsModule.viewModel

import com.marshall.pyerite.characterSkillsModule.data.CharacterSkillsCache
import com.marshall.pyerite.characterSkillsModule.data.CharacterSkillsLoader
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class CharacterSkillsRepository internal constructor(
    private val skillsLoader: CharacterSkillsLoader,
    private val skillsCache: CharacterSkillsCache,
) {
    private val statusByCharacterId = ConcurrentHashMap<Long, CharacterSkillQueueStatus>()

    /** Memory first, then disk — for instant main-page hint. */
    fun cachedStatus(characterId: Long): CharacterSkillQueueStatus? {
        statusByCharacterId[characterId]?.let { return it }
        return skillsCache.get(characterId)?.also { cached ->
            statusByCharacterId[characterId] = cached
        }
    }

    suspend fun loadStatus(characterId: Long): CharacterSkillQueueStatus =
        withContext(Dispatchers.IO) {
            val loaded = skillsLoader.load(characterId)
            statusByCharacterId[characterId] = loaded
            skillsCache.save(loaded)
            loaded
        }
}
