package com.marshall.pyerite.corporationModule.structures.viewModel

import com.marshall.pyerite.corporationModule.structures.data.CorporationStructuresLoader
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructuresSnapshot
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

internal class CorporationStructuresRepository(
    private val loader: CorporationStructuresLoader,
) {
    private val structuresByCharacterId = ConcurrentHashMap<Long, CorporationStructuresSnapshot>()
    private val locks = ConcurrentHashMap<Long, Mutex>()

    fun cachedStructures(characterId: Long): CorporationStructuresSnapshot? =
        structuresByCharacterId[characterId]

    suspend fun loadStructures(
        characterId: Long,
        forceRefresh: Boolean = false,
    ): CorporationStructuresSnapshot {
        val lock = locks.getOrPut(characterId) { Mutex() }
        return lock.withLock {
            if (!forceRefresh) {
                structuresByCharacterId[characterId]?.let { return@withLock it }
            }
            val loaded = loader.loadStructures(characterId)
            structuresByCharacterId[characterId] = loaded
            loaded
        }
    }
}
