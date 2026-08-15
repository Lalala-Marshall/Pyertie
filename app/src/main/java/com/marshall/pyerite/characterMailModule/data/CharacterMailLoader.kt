package com.marshall.pyerite.characterMailModule.data

import com.marshall.pyerite.characterMailModule.model.CharacterMailDetail
import com.marshall.pyerite.characterMailModule.model.CharacterMailHeader
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailboxes
import com.marshall.pyerite.characterMailModule.model.CharacterMailParticipant
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.api.EsiUniverseApi
import com.marshall.pyerite.esiModule.data.allianceLogoUrl
import com.marshall.pyerite.esiModule.data.corporationLogoUrl
import com.marshall.pyerite.esiModule.data.portraitUrl
import com.marshall.pyerite.esiModule.model.EsiMailRecipientType
import com.marshall.pyerite.esiModule.model.EsiMailingListDto
import com.marshall.pyerite.esiModule.model.EsiUniverseNameCategory
import com.marshall.pyerite.esiModule.model.EsiUniverseNameDto
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/** Loads mail headers and resolves sender names / portraits. */
internal class CharacterMailLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
    private val universeApi: EsiUniverseApi,
) {
    suspend fun load(
        characterId: Long,
        labelId: Int? = null,
    ): CharacterMailInbox = withContext(Dispatchers.IO) {
        coroutineScope {
            val headersDeferred = async {
                tokenManager.executeWithAuthRetry(characterId) { auth ->
                    characterApi.fetchMailHeaders(
                        characterId,
                        auth,
                        labels = labelId?.let { listOf(it) },
                    )
                }
            }
            val listsDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(characterId) { auth ->
                        characterApi.fetchMailingLists(characterId, auth)
                    }
                }.getOrElse { emptyList() }
            }

            val headers = headersDeferred.await()
            val mailingLists = listsDeferred.await().associateBy { it.mailingListId }
            val fromIds = headers.mapNotNullTo(linkedSetOf()) { it.from }
            val idsForNames = fromIds.filterNot { it in mailingLists }
            val namesById = resolveUniverseNames(idsForNames)

            CharacterMailInbox(
                characterId = characterId,
                labelId = labelId,
                mails = headers.map { header ->
                    val sender = resolveParty(header.from, recipientType = null, mailingLists, namesById)
                    CharacterMailHeader(
                        mailId = header.mailId,
                        subject = header.subject.orEmpty(),
                        senderName = sender.name,
                        senderPortraitUrl = sender.portraitUrl,
                        receivedAtEpochMs = parseEsiDateMillis(header.timestamp),
                    )
                },
            )
        }
    }

    suspend fun loadMailboxes(characterId: Long): CharacterMailMailboxes =
        withContext(Dispatchers.IO) {
            val dto = tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchMailLabels(characterId, auth)
            }
            CharacterMailMailboxes(
                characterId = characterId,
                mailboxes = dto.labels.map { label ->
                    CharacterMailMailbox(
                        labelId = label.labelId,
                        name = label.name?.takeIf { it.isNotBlank() },
                    )
                },
            )
        }

    suspend fun loadDetail(
        characterId: Long,
        mailId: Long,
        cachedHeader: CharacterMailHeader? = null,
    ): CharacterMailDetail = withContext(Dispatchers.IO) {
        val dto = tokenManager.executeWithAuthRetry(characterId) { auth ->
            characterApi.fetchMail(characterId, mailId, auth)
        }
        val lists = runCatching {
            tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchMailingLists(characterId, auth)
            }
        }.getOrElse { emptyList() }.associateBy { it.mailingListId }
        val fromId = dto.from
        val idsForNames = buildList {
            if (cachedHeader == null && fromId != null && fromId !in lists) {
                add(fromId)
            }
            dto.recipients.forEach { recipient ->
                if (recipient.recipientId in lists) return@forEach
                if (recipient.recipientType == EsiMailRecipientType.MAILING_LIST) return@forEach
                add(recipient.recipientId)
            }
        }.distinct()
        val namesById = resolveUniverseNames(idsForNames)
        val sender = if (cachedHeader != null) {
            ResolvedMailParty(
                name = cachedHeader.senderName,
                portraitUrl = cachedHeader.senderPortraitUrl,
            )
        } else {
            resolveParty(fromId, recipientType = null, lists, namesById)
        }
        CharacterMailDetail(
            mailId = mailId,
            subject = dto.subject?.takeIf { it.isNotBlank() } ?: cachedHeader?.subject.orEmpty(),
            bodyHtml = dto.body.orEmpty(),
            senderName = sender.name,
            senderPortraitUrl = sender.portraitUrl,
            receivedAtEpochMs = parseEsiDateMillis(dto.timestamp) ?: cachedHeader?.receivedAtEpochMs,
            recipients = dto.recipients.map { recipient ->
                resolveParty(
                    id = recipient.recipientId,
                    recipientType = recipient.recipientType,
                    mailingLists = lists,
                    namesById = namesById,
                ).toParticipant(recipient.recipientId)
            },
        )
    }

    private suspend fun resolveUniverseNames(
        ids: List<Long>,
    ): Map<Long, EsiUniverseNameDto> {
        if (ids.isEmpty()) return emptyMap()
        val batch = runCatching { universeApi.fetchUniverseNames(ids) }.getOrNull()
        if (batch != null) return batch.associateBy { it.id }
        return coroutineScope {
            ids.map { id ->
                async {
                    runCatching { universeApi.fetchUniverseNames(listOf(id)) }
                        .getOrNull()
                        ?.firstOrNull()
                }
            }.awaitAll().filterNotNull().associateBy { it.id }
        }
    }

    private fun resolveParty(
        id: Long?,
        recipientType: String?,
        mailingLists: Map<Long, EsiMailingListDto>,
        namesById: Map<Long, EsiUniverseNameDto>,
    ): ResolvedMailParty {
        if (id == null) return ResolvedMailParty()
        mailingLists[id]?.let { list ->
            return ResolvedMailParty(name = list.name.takeIf { it.isNotBlank() })
        }
        val named = namesById[id]
        val category = named?.category ?: recipientType
        return ResolvedMailParty(
            name = named?.name?.takeIf { it.isNotBlank() },
            portraitUrl = portraitUrlForCategory(id, category),
        )
    }

    private fun portraitUrlForCategory(id: Long, category: String?): String? = when (category) {
        EsiUniverseNameCategory.CHARACTER -> portraitUrl(id)
        EsiUniverseNameCategory.CORPORATION -> corporationLogoUrl(id)
        EsiUniverseNameCategory.ALLIANCE -> allianceLogoUrl(id)
        else -> null
    }
}

private data class ResolvedMailParty(
    val name: String? = null,
    val portraitUrl: String? = null,
)

private fun ResolvedMailParty.toParticipant(id: Long) = CharacterMailParticipant(
    id = id,
    name = name,
    portraitUrl = portraitUrl,
)
