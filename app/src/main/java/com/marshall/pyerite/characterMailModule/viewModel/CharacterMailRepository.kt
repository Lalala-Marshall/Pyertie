package com.marshall.pyerite.characterMailModule.viewModel

import com.marshall.pyerite.characterMailModule.data.CharacterMailLoader
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CharacterMailRepository(
    private val mailLoader: CharacterMailLoader,
) {
    private val inboxByCharacterId = ConcurrentHashMap<Long, CharacterMailInbox>()

    fun cachedInbox(characterId: Long): CharacterMailInbox? = inboxByCharacterId[characterId]

    suspend fun loadInbox(characterId: Long): CharacterMailInbox = withContext(Dispatchers.IO) {
        val loaded = mailLoader.load(characterId)
        inboxByCharacterId[characterId] = loaded
        loaded
    }
}
