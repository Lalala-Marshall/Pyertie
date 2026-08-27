package com.marshall.pyerite.characterCalendarModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.characterCalendarModule.model.CalendarDates
import com.marshall.pyerite.characterCalendarModule.model.CalendarEventResponse
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEvent
import com.marshall.pyerite.infra.network.PyeriteJson
import kotlinx.serialization.Serializable

/**
 * Disk cache of calendar event summaries. Events are merged by event id so days
 * already started this month still show after ESI drops them from the upcoming list.
 */
internal class CharacterCalendarCache(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(characterId: Long): List<CharacterCalendarEvent> {
        val raw = prefs.getString(keyFor(characterId), null) ?: return emptyList()
        return runCatching {
            PyeriteJson.decodeFromString<CachedCalendarEvents>(raw).toModels()
        }.getOrElse { emptyList() }
    }

    fun merge(
        characterId: Long,
        incoming: List<CharacterCalendarEvent>,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): List<CharacterCalendarEvent> {
        val byId = LinkedHashMap<Long, CharacterCalendarEvent>()
        get(characterId).forEach { byId[it.eventId] = it }
        incoming.forEach { byId[it.eventId] = it }
        val pruneBefore = CalendarDates.previousMonthStartEpochMs(nowEpochMs)
        val merged = byId.values
            .filter { it.startEpochMs >= pruneBefore }
            .sortedBy { it.startEpochMs }
        save(characterId, merged)
        return merged
    }

    private fun save(characterId: Long, events: List<CharacterCalendarEvent>) {
        val encoded = PyeriteJson.encodeToString(CachedCalendarEvents.from(characterId, events))
        prefs.edit { putString(keyFor(characterId), encoded) }
    }

    private fun keyFor(characterId: Long): String = "$KEY_PREFIX$characterId"

    private companion object {
        const val PREFS_NAME = "pyerite_character_calendar_cache"
        const val KEY_PREFIX = "calendar_"
    }
}

@Serializable
private data class CachedCalendarEvents(
    val characterId: Long,
    val events: List<CachedCalendarEvent> = emptyList(),
) {
    fun toModels(): List<CharacterCalendarEvent> = events.map { it.toModel() }

    companion object {
        fun from(
            characterId: Long,
            events: List<CharacterCalendarEvent>,
        ): CachedCalendarEvents = CachedCalendarEvents(
            characterId = characterId,
            events = events.map(CachedCalendarEvent::from),
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
