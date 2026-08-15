package com.marshall.pyerite.characterMailModule.viewModel

import com.marshall.pyerite.characterMailModule.data.CharacterMailLoader
import com.marshall.pyerite.characterMailModule.model.CharacterMailDetail
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CharacterMailRepository(
    private val mailLoader: CharacterMailLoader,
) {
    private val inboxByCharacterId = ConcurrentHashMap<Long, CharacterMailInbox>()
    private val detailByKey = ConcurrentHashMap<MailDetailKey, CharacterMailDetail>()

    fun cachedInbox(characterId: Long): CharacterMailInbox? = inboxByCharacterId[characterId]

    fun cachedDetail(characterId: Long, mailId: Long): CharacterMailDetail? =
        detailByKey[MailDetailKey(characterId, mailId)]

    fun seedDetail(characterId: Long, mailId: Long): CharacterMailDetail {
        return cachedDetail(characterId, mailId)
            ?: CharacterMailDetail.seed(
                mailId = mailId,
                header = inboxByCharacterId[characterId]?.mails?.firstOrNull { it.mailId == mailId },
            )
    }

    suspend fun loadInbox(characterId: Long): CharacterMailInbox = withContext(Dispatchers.IO) {
        val loaded = mailLoader.load(characterId)
        inboxByCharacterId[characterId] = loaded
        loaded
    }

    suspend fun loadDetail(characterId: Long, mailId: Long): CharacterMailDetail =
        withContext(Dispatchers.IO) {
            val cachedHeader = inboxByCharacterId[characterId]?.mails
                ?.firstOrNull { it.mailId == mailId }
            val loaded = mailLoader.loadDetail(characterId, mailId, cachedHeader)
            detailByKey[MailDetailKey(characterId, mailId)] = loaded
            loaded
        }
}

private data class MailDetailKey(
    val characterId: Long,
    val mailId: Long,
)
