package com.marshall.pyerite.characterMailModule.data

import com.marshall.pyerite.characterMailModule.model.CharacterMailDetail
import com.marshall.pyerite.characterMailModule.model.CharacterMailHeader
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailbox
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailboxes
import com.marshall.pyerite.characterMailModule.model.CharacterMailParticipant
import com.marshall.pyerite.characterMailModule.model.MailComposeRecipient
import com.marshall.pyerite.characterMailModule.model.MailRecipientKind
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.api.EsiUniverseApi
import com.marshall.pyerite.esiModule.data.allianceLogoUrl
import com.marshall.pyerite.esiModule.data.corporationLogoUrl
import com.marshall.pyerite.esiModule.data.portraitUrl
import com.marshall.pyerite.esiModule.http.EsiConfig
import com.marshall.pyerite.esiModule.model.EsiMailLabelId
import com.marshall.pyerite.esiModule.model.EsiMailRecipientDto
import com.marshall.pyerite.esiModule.model.EsiMailRecipientType
import com.marshall.pyerite.esiModule.model.EsiMailingListDto
import com.marshall.pyerite.esiModule.model.EsiSendMailRequestDto
import com.marshall.pyerite.esiModule.model.EsiUniverseIdNameDto
import com.marshall.pyerite.esiModule.model.EsiUniverseIdsDto
import com.marshall.pyerite.esiModule.model.EsiUniverseNameCategory
import com.marshall.pyerite.esiModule.model.EsiUniverseNameDto
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException

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

    suspend fun loadSubscribedMailingLists(characterId: Long): List<MailComposeRecipient> =
        withContext(Dispatchers.IO) {
            tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchMailingLists(characterId, auth)
            }.map { list ->
                MailComposeRecipient(
                    id = list.mailingListId,
                    name = list.name.takeIf { it.isNotBlank() },
                    portraitUrl = null,
                    kind = MailRecipientKind.MAILING_LIST,
                )
            }
        }

    suspend fun loadRecentMailContacts(characterId: Long): List<MailComposeRecipient> =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val headersDeferred = async {
                    tokenManager.executeWithAuthRetry(characterId) { auth ->
                        characterApi.fetchMailHeaders(characterId, auth)
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
                val mailingListIds = listsDeferred.await().mapTo(hashSetOf()) { it.mailingListId }
                val ids = linkedSetOf<Long>()
                headers.forEach { header ->
                    if (EsiMailLabelId.SENT in header.labels) {
                        header.recipients.forEach { recipient ->
                            if (!isMailPartyRecipientType(recipient.recipientType)) return@forEach
                            if (recipient.recipientId in mailingListIds) return@forEach
                            ids.add(recipient.recipientId)
                        }
                    } else {
                        val fromId = header.from ?: return@forEach
                        if (fromId in mailingListIds) return@forEach
                        ids.add(fromId)
                    }
                }
                val namesById = resolveUniverseNames(ids.toList())
                ids.mapNotNull { id -> namesById[id]?.toComposeRecipient() }
            }
        }

    suspend fun searchUniverseRecipients(query: String): List<MailComposeRecipient> =
        withContext(Dispatchers.IO) {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) return@withContext emptyList()
            val parsedId = trimmed.toLongOrNull()
            if (parsedId != null) {
                return@withContext fetchUniverseNamesAllowingNotFound(listOf(parsedId))
                    .mapNotNull { it.toComposeRecipient() }
            }
            universeApi.fetchUniverseIds(listOf(trimmed)).toComposeRecipients()
        }

    suspend fun sendMail(
        characterId: Long,
        recipients: List<MailComposeRecipient>,
        subject: String,
        body: String,
    ) = withContext(Dispatchers.IO) {
        val request = EsiSendMailRequestDto(
            recipients = recipients.map { party ->
                EsiMailRecipientDto(
                    recipientId = party.id,
                    recipientType = party.kind.toEsiRecipientType(),
                )
            },
            subject = subject,
            body = body,
        )
        tokenManager.executeWithAuthRetry(characterId) { auth ->
            characterApi.sendMail(characterId, auth, request).use { }
        }
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

    private suspend fun fetchUniverseNamesAllowingNotFound(
        ids: List<Long>,
    ): List<EsiUniverseNameDto> {
        if (ids.isEmpty()) return emptyList()
        return try {
            universeApi.fetchUniverseNames(ids)
        } catch (httpError: HttpException) {
            if (httpError.code() == EsiConfig.HttpStatus.NOT_FOUND) {
                emptyList()
            } else {
                throw httpError
            }
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

private fun isMailPartyRecipientType(type: String): Boolean = when (type) {
    EsiMailRecipientType.CHARACTER,
    EsiMailRecipientType.CORPORATION,
    EsiMailRecipientType.ALLIANCE,
    -> true
    else -> false
}

private fun EsiUniverseNameDto.toComposeRecipient(): MailComposeRecipient? {
    val kind = mailRecipientKindForCategory(category) ?: return null
    return MailComposeRecipient(
        id = id,
        name = name.takeIf { it.isNotBlank() },
        portraitUrl = portraitUrlForKind(id, kind),
        kind = kind,
    )
}

private fun EsiUniverseIdsDto.toComposeRecipients(): List<MailComposeRecipient> = buildList {
    addAll(characters.toComposeRecipients(MailRecipientKind.CHARACTER))
    addAll(corporations.toComposeRecipients(MailRecipientKind.CORPORATION))
    addAll(alliances.toComposeRecipients(MailRecipientKind.ALLIANCE))
}

private fun List<EsiUniverseIdNameDto>.toComposeRecipients(
    kind: MailRecipientKind,
): List<MailComposeRecipient> = map { named ->
    MailComposeRecipient(
        id = named.id,
        name = named.name.takeIf { it.isNotBlank() },
        portraitUrl = portraitUrlForKind(named.id, kind),
        kind = kind,
    )
}

private fun mailRecipientKindForCategory(category: String): MailRecipientKind? = when (category) {
    EsiUniverseNameCategory.CHARACTER -> MailRecipientKind.CHARACTER
    EsiUniverseNameCategory.CORPORATION -> MailRecipientKind.CORPORATION
    EsiUniverseNameCategory.ALLIANCE -> MailRecipientKind.ALLIANCE
    else -> null
}

private fun MailRecipientKind.toEsiRecipientType(): String = when (this) {
    MailRecipientKind.CHARACTER -> EsiMailRecipientType.CHARACTER
    MailRecipientKind.CORPORATION -> EsiMailRecipientType.CORPORATION
    MailRecipientKind.ALLIANCE -> EsiMailRecipientType.ALLIANCE
    MailRecipientKind.MAILING_LIST -> EsiMailRecipientType.MAILING_LIST
}

private fun portraitUrlForKind(id: Long, kind: MailRecipientKind): String? = when (kind) {
    MailRecipientKind.CHARACTER -> portraitUrl(id)
    MailRecipientKind.CORPORATION -> corporationLogoUrl(id)
    MailRecipientKind.ALLIANCE -> allianceLogoUrl(id)
    MailRecipientKind.MAILING_LIST -> null
}
