package com.marshall.pyerite.characterCalendarModule.data

import com.marshall.pyerite.characterCalendarModule.model.CalendarEsiConfig
import com.marshall.pyerite.characterCalendarModule.model.CalendarEventResponse
import com.marshall.pyerite.characterCalendarModule.model.CalendarOwnerType
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEvent
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEventDetail
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.model.EsiCalendarEventDetailDto
import com.marshall.pyerite.esiModule.model.EsiCalendarEventResponseValue
import com.marshall.pyerite.esiModule.model.EsiCalendarEventSummaryDto
import com.marshall.pyerite.esiModule.model.EsiCalendarOwnerTypeValue
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class CharacterCalendarLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
) {
    suspend fun loadSummaries(
        characterId: Long,
        coverUntilEpochMs: Long,
    ): List<CharacterCalendarEvent> = withContext(Dispatchers.IO) {
        val collected = LinkedHashMap<Long, CharacterCalendarEvent>()
        var fromEvent: Long? = null
        var pagesRemaining = CalendarEsiConfig.MAX_PAGES
        while (pagesRemaining > 0) {
            pagesRemaining--
            val page = tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchCalendarEvents(characterId, auth, fromEvent)
            }
            val mapped = page.mapNotNull { it.toModel() }
            mapped.forEach { collected[it.eventId] = it }
            if (page.size < CalendarEsiConfig.PAGE_SIZE) break
            val last = mapped.lastOrNull() ?: break
            if (last.startEpochMs >= coverUntilEpochMs) break
            fromEvent = last.eventId
        }
        collected.values.toList()
    }

    suspend fun loadDetail(
        characterId: Long,
        eventId: Long,
    ): CharacterCalendarEventDetail = withContext(Dispatchers.IO) {
        val dto = tokenManager.executeWithAuthRetry(characterId) { auth ->
            characterApi.fetchCalendarEvent(characterId, eventId, auth)
        }
        dto.toModel()
    }
}

private fun EsiCalendarEventSummaryDto.toModel(): CharacterCalendarEvent? {
    val startEpochMs = parseEsiDateMillis(eventDate) ?: return null
    return CharacterCalendarEvent(
        eventId = eventId,
        title = title.orEmpty(),
        startEpochMs = startEpochMs,
        importance = importance,
        response = parseCalendarResponse(eventResponse),
    )
}

private fun EsiCalendarEventDetailDto.toModel(): CharacterCalendarEventDetail {
    val startEpochMs = parseEsiDateMillis(date) ?: 0L
    return CharacterCalendarEventDetail(
        eventId = eventId,
        title = title.orEmpty(),
        startEpochMs = startEpochMs,
        durationMinutes = duration,
        importance = importance,
        response = parseCalendarResponse(response),
        ownerId = ownerId,
        ownerName = ownerName.orEmpty(),
        ownerType = parseOwnerType(ownerType),
        textHtml = text.orEmpty(),
    )
}

private fun parseCalendarResponse(raw: String?): CalendarEventResponse {
    return when (raw?.trim()?.lowercase()) {
        EsiCalendarEventResponseValue.ACCEPTED -> CalendarEventResponse.ACCEPTED
        EsiCalendarEventResponseValue.DECLINED -> CalendarEventResponse.DECLINED
        EsiCalendarEventResponseValue.TENTATIVE -> CalendarEventResponse.TENTATIVE
        EsiCalendarEventResponseValue.NOT_RESPONDED,
        EsiCalendarEventResponseValue.UNDECIDED,
        -> CalendarEventResponse.NOT_RESPONDED
        else -> CalendarEventResponse.NOT_RESPONDED
    }
}

private fun parseOwnerType(raw: String?): CalendarOwnerType {
    return when (raw?.trim()?.lowercase()) {
        EsiCalendarOwnerTypeValue.CHARACTER -> CalendarOwnerType.CHARACTER
        EsiCalendarOwnerTypeValue.CORPORATION -> CalendarOwnerType.CORPORATION
        EsiCalendarOwnerTypeValue.ALLIANCE -> CalendarOwnerType.ALLIANCE
        EsiCalendarOwnerTypeValue.FACTION -> CalendarOwnerType.FACTION
        EsiCalendarOwnerTypeValue.EVE_SERVER -> CalendarOwnerType.EVE_SERVER
        else -> CalendarOwnerType.UNKNOWN
    }
}
