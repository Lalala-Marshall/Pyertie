package com.marshall.pyerite.characterCalendarModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.characterCalendarModule.model.CalendarDates
import com.marshall.pyerite.characterCalendarModule.model.CalendarEventResponse
import com.marshall.pyerite.characterCalendarModule.model.CalendarEventStatus
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEvent
import com.marshall.pyerite.infra.network.PyeriteJson
import kotlinx.serialization.Serializable

/**
 * Disk cache of calendar event summaries. Events are merged by event id so days
 * already started this month still show after ESI drops them from the upcoming list.
 * Upcoming ghosts that ESI no longer lists (or whose detail route 404s) are dropped.
 */
internal class CharacterCalendarCache(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(characterId: Long): List<CharacterCalendarEvent> =
        loadSnapshot(characterId).visibleEvents()

    fun merge(
        characterId: Long,
        incoming: List<CharacterCalendarEvent>,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): List<CharacterCalendarEvent> {
        val snapshot = loadSnapshot(characterId)
        val inaccessible = snapshot.inaccessibleEventIds
        val incomingById = LinkedHashMap<Long, CharacterCalendarEvent>()
        incoming.forEach { event ->
            if (event.eventId in inaccessible) return@forEach
            if (!CalendarEventStatus.isDisplayableTitle(event.title)) return@forEach
            incomingById[event.eventId] = event
        }
        val pruneBefore = CalendarDates.previousMonthStartEpochMs(nowEpochMs)
        val byId = LinkedHashMap<Long, CharacterCalendarEvent>()
        snapshot.events.forEach { cached ->
            if (cached.eventId in inaccessible) return@forEach
            if (!CalendarEventStatus.isDisplayableTitle(cached.title)) return@forEach
            if (cached.startEpochMs < pruneBefore) return@forEach
            if (CalendarEventStatus.isUpcoming(cached.startEpochMs, nowEpochMs) &&
                cached.eventId !in incomingById
            ) {
                return@forEach
            }
            byId[cached.eventId] = cached
        }
        incomingById.forEach { (eventId, event) -> byId[eventId] = event }
        val merged = byId.values.sortedBy { it.startEpochMs }
        saveSnapshot(characterId, merged, inaccessible)
        return merged
    }

    fun markInaccessible(
        characterId: Long,
        eventIds: Set<Long>,
    ): List<CharacterCalendarEvent> {
        if (eventIds.isEmpty()) return get(characterId)
        val snapshot = loadSnapshot(characterId)
        val inaccessible = snapshot.inaccessibleEventIds + eventIds
        val remaining = snapshot.events.filter { event ->
            event.eventId !in inaccessible &&
                CalendarEventStatus.isDisplayableTitle(event.title)
        }
        saveSnapshot(characterId, remaining, inaccessible)
        return remaining
    }

    private fun loadSnapshot(characterId: Long): CachedCalendarSnapshot {
        val raw = prefs.getString(keyFor(characterId), null) ?: return CachedCalendarSnapshot()
        return runCatching {
            PyeriteJson.decodeFromString<CachedCalendarEvents>(raw).toSnapshot()
        }.getOrElse { CachedCalendarSnapshot() }
    }

    private fun saveSnapshot(
        characterId: Long,
        events: List<CharacterCalendarEvent>,
        inaccessibleEventIds: Set<Long>,
    ) {
        val encoded = PyeriteJson.encodeToString(
            CachedCalendarEvents.from(characterId, events, inaccessibleEventIds),
        )
        prefs.edit { putString(keyFor(characterId), encoded) }
    }

    private fun keyFor(characterId: Long): String = "$KEY_PREFIX$characterId"

    private companion object {
        const val PREFS_NAME = "pyerite_character_calendar_cache"
        const val KEY_PREFIX = "calendar_"
    }
}

private data class CachedCalendarSnapshot(
    val events: List<CharacterCalendarEvent> = emptyList(),
    val inaccessibleEventIds: Set<Long> = emptySet(),
) {
    fun visibleEvents(): List<CharacterCalendarEvent> = events.filter { event ->
        event.eventId !in inaccessibleEventIds &&
            CalendarEventStatus.isDisplayableTitle(event.title)
    }
}

@Serializable
private data class CachedCalendarEvents(
    val characterId: Long,
    val events: List<CachedCalendarEvent> = emptyList(),
    val inaccessibleEventIds: List<Long> = emptyList(),
) {
    fun toSnapshot(): CachedCalendarSnapshot = CachedCalendarSnapshot(
        events = events.map { it.toModel() },
        inaccessibleEventIds = inaccessibleEventIds.toSet(),
    )

    companion object {
        fun from(
            characterId: Long,
            events: List<CharacterCalendarEvent>,
            inaccessibleEventIds: Set<Long>,
        ): CachedCalendarEvents = CachedCalendarEvents(
            characterId = characterId,
            events = events.map(CachedCalendarEvent::from),
            inaccessibleEventIds = inaccessibleEventIds.sorted(),
        )
    }
}

@Serializable
private data class CachedCalendarEvent(
    val eventId: Long,
    val title: String,
    val startEpochMs: Long,
    val importance: Int,
    val response: String,
) {
    fun toModel(): CharacterCalendarEvent = CharacterCalendarEvent(
        eventId = eventId,
        title = title,
        startEpochMs = startEpochMs,
        importance = importance,
        response = runCatching { CalendarEventResponse.valueOf(response) }
            .getOrDefault(CalendarEventResponse.NOT_RESPONDED),
    )

    companion object {
        fun from(event: CharacterCalendarEvent): CachedCalendarEvent = CachedCalendarEvent(
            eventId = event.eventId,
            title = event.title,
            startEpochMs = event.startEpochMs,
            importance = event.importance,
            response = event.response.name,
        )
    }
}
