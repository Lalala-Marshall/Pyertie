package com.marshall.pyerite.characterMailModule.data

import com.marshall.pyerite.characterMailModule.model.CharacterMailHeader
import com.marshall.pyerite.characterMailModule.model.CharacterMailInbox
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.api.EsiUniverseApi
import com.marshall.pyerite.esiModule.data.allianceLogoUrl
import com.marshall.pyerite.esiModule.data.corporationLogoUrl
import com.marshall.pyerite.esiModule.data.portraitUrl
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
    suspend fun load(characterId: Long): CharacterMailInbox = withContext(Dispatchers.IO) {
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
            val mailingLists = listsDeferred.await().associateBy { it.mailingListId }
            val fromIds = headers.mapNotNullTo(linkedSetOf()) { it.from }
            val idsForNames = fromIds.filterNot { it in mailingLists }
            val namesById = resolveUniverseNames(idsForNames)

            CharacterMailInbox(
                characterId = characterId,
                mails = headers.map { header ->
                    val sender = resolveSender(header.from, mailingLists, namesById)
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

    private fun resolveSender(
        fromId: Long?,
        mailingLists: Map<Long, EsiMailingListDto>,
        namesById: Map<Long, EsiUniverseNameDto>,
    ): ResolvedMailSender {
        if (fromId == null) return ResolvedMailSender()
        mailingLists[fromId]?.let { list ->
            return ResolvedMailSender(name = list.name.takeIf { it.isNotBlank() })
        }
        val named = namesById[fromId] ?: return ResolvedMailSender()
        val portraitUrl = when (named.category) {
            EsiUniverseNameCategory.CHARACTER -> portraitUrl(fromId)
            EsiUniverseNameCategory.CORPORATION -> corporationLogoUrl(fromId)
            EsiUniverseNameCategory.ALLIANCE -> allianceLogoUrl(fromId)
            else -> null
        }
        return ResolvedMailSender(
            name = named.name.takeIf { it.isNotBlank() },
            portraitUrl = portraitUrl,
        )
    }
}

private data class ResolvedMailSender(
    val name: String? = null,
    val portraitUrl: String? = null,
)
