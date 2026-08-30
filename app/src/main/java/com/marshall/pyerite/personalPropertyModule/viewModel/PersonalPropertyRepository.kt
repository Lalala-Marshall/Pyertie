package com.marshall.pyerite.personalPropertyModule.viewModel

import com.marshall.pyerite.personalPropertyModule.data.PersonalPropertyLoader
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class PersonalPropertyRepository(
    private val loader: PersonalPropertyLoader,
) {
    private val summaryByCharacterId = ConcurrentHashMap<Long, PersonalPropertySummary>()

    fun seedSummary(characterId: Long): PersonalPropertySummary {
        return summaryByCharacterId[characterId]
            ?: PersonalPropertySummary.empty(characterId)
    }

    fun cachedSummary(characterId: Long): PersonalPropertySummary? =
        summaryByCharacterId[characterId]

    /**
     * @param forceRefresh when true, always hits ESI and replaces the cache entry.
     * Otherwise returns the in-memory summary for [characterId] if present.
     */
    suspend fun loadSummary(
        characterId: Long,
        forceRefresh: Boolean = false,
    ): PersonalPropertySummary = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            summaryByCharacterId[characterId]?.let { return@withContext it }
        }
        val loaded = loader.load(characterId)
        summaryByCharacterId[characterId] = loaded
        loaded
    }
}
