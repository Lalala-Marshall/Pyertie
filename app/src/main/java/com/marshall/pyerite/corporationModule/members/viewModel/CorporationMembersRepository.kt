package com.marshall.pyerite.corporationModule.members.viewModel

import com.marshall.pyerite.corporationModule.members.data.CorporationMembersLoader
import com.marshall.pyerite.corporationModule.members.model.CorporationMembersSnapshot
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

internal class CorporationMembersRepository(
    private val loader: CorporationMembersLoader,
) {
    private val membersByCharacterId = ConcurrentHashMap<Long, CorporationMembersSnapshot>()
    private val locks = ConcurrentHashMap<Long, Mutex>()

    fun cachedMembers(characterId: Long): CorporationMembersSnapshot? =
        membersByCharacterId[characterId]

    suspend fun loadMembers(
        characterId: Long,
        forceRefresh: Boolean = false,
    ): CorporationMembersSnapshot {
        val lock = locks.getOrPut(characterId) { Mutex() }
        return lock.withLock {
            if (!forceRefresh) {
                membersByCharacterId[characterId]?.let { return@withLock it }
            }
            val loaded = loader.loadMembers(characterId)
            membersByCharacterId[characterId] = loaded
            loaded
        }
    }
}
