package com.marshall.pyerite.characterSkillsModule.viewModel

import com.marshall.pyerite.characterSkillsModule.data.CharacterSkillsCache
import com.marshall.pyerite.characterSkillsModule.data.CharacterSkillsLoader
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class CharacterSkillsRepository internal constructor(
    private val skillsLoader: CharacterSkillsLoader,
    private val skillsCache: CharacterSkillsCache,
) {
    private val statusByCharacterId = ConcurrentHashMap<Long, CharacterSkillQueueStatus>()
    private val attributesByCharacterId = ConcurrentHashMap<Long, CharacterAttributes>()
    private val catalogByCharacterId = ConcurrentHashMap<Long, List<SkillCatalogGroup>>()

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

    fun cachedAttributes(characterId: Long): CharacterAttributes? {
        attributesByCharacterId[characterId]?.let { return it }
        return skillsCache.getAttributes(characterId)?.also { cached ->
            attributesByCharacterId[characterId] = cached
        }
    }

    suspend fun loadAttributes(characterId: Long): CharacterAttributes =
        withContext(Dispatchers.IO) {
            val loaded = skillsLoader.loadAttributes(characterId)
            attributesByCharacterId[characterId] = loaded
            skillsCache.saveAttributes(loaded)
            loaded
        }

    fun cachedCatalog(characterId: Long): List<SkillCatalogGroup>? =
        catalogByCharacterId[characterId]

    suspend fun loadCatalog(characterId: Long): List<SkillCatalogGroup> =
        withContext(Dispatchers.IO) {
            val loaded = skillsLoader.loadCatalog(characterId)
            catalogByCharacterId[characterId] = loaded
            loaded
        }
}
