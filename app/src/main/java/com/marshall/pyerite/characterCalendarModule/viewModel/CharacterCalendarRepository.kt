package com.marshall.pyerite.characterCalendarModule.viewModel

import com.marshall.pyerite.characterCalendarModule.data.CalendarReminderScheduler
import com.marshall.pyerite.characterCalendarModule.data.CalendarReminderStore
import com.marshall.pyerite.characterCalendarModule.data.CharacterCalendarCache
import com.marshall.pyerite.characterCalendarModule.data.CharacterCalendarLoader
import com.marshall.pyerite.characterCalendarModule.model.CalendarAddReminderResult
import com.marshall.pyerite.characterCalendarModule.model.CalendarReminder
import com.marshall.pyerite.characterCalendarModule.model.CalendarReminderLead
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEvent
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEventDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CharacterCalendarRepository(
    private val loader: CharacterCalendarLoader,
    private val cache: CharacterCalendarCache,
    private val reminderStore: CalendarReminderStore,
    private val reminderScheduler: CalendarReminderScheduler,
) {
    private val eventsByCharacterId = ConcurrentHashMap<Long, List<CharacterCalendarEvent>>()
    private val detailsByKey = ConcurrentHashMap<DetailKey, CharacterCalendarEventDetail>()

    val reminders: StateFlow<List<CalendarReminder>> = reminderStore.reminders

    fun cachedEvents(characterId: Long): List<CharacterCalendarEvent> {
        eventsByCharacterId[characterId]?.let { return it }
        return cache.get(characterId).also { cached ->
            if (cached.isNotEmpty()) {
                eventsByCharacterId[characterId] = cached
            }
        }
    }

    fun cachedDetail(characterId: Long, eventId: Long): CharacterCalendarEventDetail? =
        detailsByKey[DetailKey(characterId, eventId)]

    suspend fun loadEvents(
        characterId: Long,
        coverUntilEpochMs: Long,
    ): List<CharacterCalendarEvent> = withContext(Dispatchers.IO) {
        val incoming = loader.loadSummaries(characterId, coverUntilEpochMs)
        val merged = cache.merge(characterId, incoming)
        eventsByCharacterId[characterId] = merged
        merged
    }

    suspend fun loadDetail(
        characterId: Long,
        eventId: Long,
    ): CharacterCalendarEventDetail = withContext(Dispatchers.IO) {
        val loaded = loader.loadDetail(characterId, eventId)
        detailsByKey[DetailKey(characterId, eventId)] = loaded
        loaded
    }

    fun pendingReminders(
        characterId: Long,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): List<CalendarReminder> = reminderStore.pending(characterId, nowEpochMs)

    fun addReminder(
        characterId: Long,
        event: CharacterCalendarEvent,
        lead: CalendarReminderLead,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): CalendarAddReminderResult {
        val fireAtEpochMs = event.startEpochMs - lead.offsetMs
        if (fireAtEpochMs <= nowEpochMs) {
            return CalendarAddReminderResult.FIRE_TIME_PASSED
        }
        val reminder = CalendarReminder(
            characterId = characterId,
            eventId = event.eventId,
            eventTitle = event.title,
            eventStartEpochMs = event.startEpochMs,
            lead = lead,
            fireAtEpochMs = fireAtEpochMs,
        )
        if (!reminderStore.add(reminder)) {
            return CalendarAddReminderResult.DUPLICATE
        }
        reminderScheduler.schedule(reminder)
        return CalendarAddReminderResult.ADDED
    }

    fun removeReminder(reminder: CalendarReminder) {
        reminderScheduler.cancel(reminder)
        reminderStore.remove(reminder.characterId, reminder.eventId, reminder.lead)
    }

    fun pruneFiredReminders(nowEpochMs: Long = System.currentTimeMillis()) {
        reminderStore.pruneFired(nowEpochMs)
    }

    fun reschedulePendingReminders(nowEpochMs: Long = System.currentTimeMillis()) {
        reminderStore.pruneFired(nowEpochMs)
        reminderScheduler.reschedule(reminderStore.pending(nowEpochMs = nowEpochMs))
    }

    fun exactAlarmSettingsIntent() = reminderScheduler.exactAlarmSettingsIntent()

    fun ensureNotificationChannel() = reminderScheduler.ensureChannel()

    private data class DetailKey(val characterId: Long, val eventId: Long)
}
