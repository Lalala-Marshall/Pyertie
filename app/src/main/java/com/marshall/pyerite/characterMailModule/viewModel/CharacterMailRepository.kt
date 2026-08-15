package com.marshall.pyerite.characterMailModule.viewModel

import com.marshall.pyerite.characterMailModule.data.CharacterMailLoader
import com.marshall.pyerite.characterMailModule.model.CharacterMailDetail
import com.marshall.pyerite.characterMailModule.model.CharacterMailHeader
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailboxes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CharacterMailRepository(
    private val mailLoader: CharacterMailLoader,
) {
    private val inboxByKey = ConcurrentHashMap<MailListKey, CharacterMailInbox>()
    private val mailboxesByCharacterId = ConcurrentHashMap<Long, CharacterMailMailboxes>()
    private val detailByKey = ConcurrentHashMap<MailDetailKey, CharacterMailDetail>()

    fun cachedInbox(characterId: Long, labelId: Int? = null): CharacterMailInbox? =
        inboxByKey[MailListKey(characterId, labelId)]

    fun cachedMailboxes(characterId: Long): CharacterMailMailboxes? =
        mailboxesByCharacterId[characterId]

    fun cachedMailbox(characterId: Long, labelId: Int): CharacterMailMailbox? =
        cachedMailboxes(characterId)?.mailboxes?.firstOrNull { it.labelId == labelId }

    fun cachedDetail(characterId: Long, mailId: Long): CharacterMailDetail? =
        detailByKey[MailDetailKey(characterId, mailId)]

    fun seedDetail(characterId: Long, mailId: Long): CharacterMailDetail {
        return cachedDetail(characterId, mailId)
            ?: CharacterMailDetail.seed(
                mailId = mailId,
                header = findHeader(characterId, mailId),
            )
    }

    suspend fun loadInbox(
        characterId: Long,
        labelId: Int? = null,
    ): CharacterMailInbox = withContext(Dispatchers.IO) {
        val loaded = mailLoader.load(characterId, labelId)
        inboxByKey[MailListKey(characterId, labelId)] = loaded
        loaded
    }

    suspend fun loadMailboxes(characterId: Long): CharacterMailMailboxes =
        withContext(Dispatchers.IO) {
            val loaded = mailLoader.loadMailboxes(characterId)
            mailboxesByCharacterId[characterId] = loaded
            loaded
        }

    suspend fun loadDetail(characterId: Long, mailId: Long): CharacterMailDetail =
        withContext(Dispatchers.IO) {
            val cachedHeader = findHeader(characterId, mailId)
            val loaded = mailLoader.loadDetail(characterId, mailId, cachedHeader)
            detailByKey[MailDetailKey(characterId, mailId)] = loaded
            loaded
        }

    private fun findHeader(characterId: Long, mailId: Long): CharacterMailHeader? {
        inboxByKey.values.forEach { inbox ->
            if (inbox.characterId != characterId) return@forEach
            inbox.mails.firstOrNull { it.mailId == mailId }?.let { return it }
        }
        return null
    }
}

private data class MailListKey(
    val characterId: Long,
    val labelId: Int?,
)

private data class MailDetailKey(
    val characterId: Long,
    val mailId: Long,
)
