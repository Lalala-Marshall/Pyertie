package com.marshall.pyerite.characterCalendarModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.characterCalendarModule.model.CalendarReminder
import com.marshall.pyerite.characterCalendarModule.model.CalendarReminderLead
import com.marshall.pyerite.infra.network.PyeriteJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

internal class CalendarReminderStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _reminders = MutableStateFlow(loadAll())
    val reminders: StateFlow<List<CalendarReminder>> = _reminders.asStateFlow()

    fun pending(
        characterId: Long? = null,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): List<CalendarReminder> = _reminders.value.filter { reminder ->
        reminder.fireAtEpochMs > nowEpochMs &&
            (characterId == null || reminder.characterId == characterId)
    }.sortedBy { it.fireAtEpochMs }

    fun add(reminder: CalendarReminder): Boolean {
        if (exists(reminder.characterId, reminder.eventId, reminder.lead)) return false
        persist(_reminders.value + reminder)
        return true
    }

    fun exists(
        characterId: Long,
        eventId: Long,
        lead: CalendarReminderLead,
    ): Boolean = _reminders.value.any {
        it.characterId == characterId && it.eventId == eventId && it.lead == lead
    }

    fun remove(characterId: Long, eventId: Long, lead: CalendarReminderLead) {
        persist(
            _reminders.value.filterNot {
                it.characterId == characterId && it.eventId == eventId && it.lead == lead
            },
        )
    }

    fun pruneFired(nowEpochMs: Long = System.currentTimeMillis()) {
        persist(_reminders.value.filter { it.fireAtEpochMs > nowEpochMs })
    }

    private fun persist(reminders: List<CalendarReminder>) {
        val encoded = PyeriteJson.encodeToString(CachedReminders(reminders))
        prefs.edit { putString(KEY_REMINDERS, encoded) }
        _reminders.value = reminders
    }

    private fun loadAll(): List<CalendarReminder> {
        val raw = prefs.getString(KEY_REMINDERS, null) ?: return emptyList()
        return runCatching {
            PyeriteJson.decodeFromString<CachedReminders>(raw).reminders
        }.getOrElse { emptyList() }
    }

    private companion object {
        const val PREFS_NAME = "pyerite_calendar_reminders"
        const val KEY_REMINDERS = "reminders"
    }
}

@Serializable
private data class CachedReminders(
    val reminders: List<CalendarReminder> = emptyList(),
)
