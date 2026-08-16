package com.marshall.pyerite.characterMailModule.viewModel

import com.marshall.pyerite.characterMailModule.data.CharacterMailLoader
import com.marshall.pyerite.characterMailModule.model.CharacterMailDetail
import com.marshall.pyerite.characterMailModule.model.CharacterMailHeader
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailboxes
import com.marshall.pyerite.characterMailModule.model.MailComposeRecipient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CharacterMailRepository(
    private val mailLoader: CharacterMailLoader,
) {
    private val inboxByKey = ConcurrentHashMap<MailListKey, CharacterMailInbox>()
    private val mailboxesByCharacterId = ConcurrentHashMap<Long, CharacterMailMailboxes>()
    private val detailByKey = ConcurrentHashMap<MailDetailKey, CharacterMailDetail>()
    private val recentContactsByCharacterId = ConcurrentHashMap<Long, List<MailComposeRecipient>>()
    private val mailingListsByCharacterId = ConcurrentHashMap<Long, List<MailComposeRecipient>>()

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

    suspend fun loadRecentMailContacts(characterId: Long): List<MailComposeRecipient> =
        withContext(Dispatchers.IO) {
            recentContactsByCharacterId[characterId]?.let { return@withContext it }
            mailLoader.loadRecentMailContacts(characterId).also { loaded ->
                recentContactsByCharacterId[characterId] = loaded
            }
        }

    suspend fun loadSubscribedMailingLists(characterId: Long): List<MailComposeRecipient> =
        withContext(Dispatchers.IO) {
            mailingListsByCharacterId[characterId]?.let { return@withContext it }
            mailLoader.loadSubscribedMailingLists(characterId).also { loaded ->
                mailingListsByCharacterId[characterId] = loaded
            }
        }

    suspend fun searchUniverseRecipients(query: String): List<MailComposeRecipient> =
        mailLoader.searchUniverseRecipients(query)

    suspend fun sendMail(
        characterId: Long,
        recipients: List<MailComposeRecipient>,
        subject: String,
        body: String,
    ) = withContext(Dispatchers.IO) {
        mailLoader.sendMail(characterId, recipients, subject, body)
        invalidateAfterSend(characterId)
    }

    private fun invalidateAfterSend(characterId: Long) {
        inboxByKey.keys.filter { key -> key.characterId == characterId }.forEach { key ->
            inboxByKey.remove(key)
        }
        recentContactsByCharacterId.remove(characterId)
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
