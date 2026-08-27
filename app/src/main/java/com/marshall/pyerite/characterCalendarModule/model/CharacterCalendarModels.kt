package com.marshall.pyerite.characterCalendarModule.model

import kotlinx.serialization.Serializable

internal data class CalendarDate(
    val year: Int,
    val month: Int,
    val day: Int,
) : Comparable<CalendarDate> {
    override fun compareTo(other: CalendarDate): Int {
        val yearCmp = year.compareTo(other.year)
        if (yearCmp != 0) return yearCmp
        val monthCmp = month.compareTo(other.month)
        if (monthCmp != 0) return monthCmp
        return day.compareTo(other.day)
    }
}

internal data class CalendarDayCell(
    val date: CalendarDate,
    val inCurrentMonth: Boolean,
)

internal data class CharacterCalendarEvent(
    val eventId: Long,
    val title: String,
    val startEpochMs: Long,
    val importance: Int,
    val response: CalendarEventResponse,
)

internal data class CharacterCalendarEventDetail(
    val eventId: Long,
    val title: String,
    val startEpochMs: Long,
    val durationMinutes: Long,
    val importance: Int,
    val response: CalendarEventResponse,
    val ownerId: Long,
    val ownerName: String,
    val ownerType: CalendarOwnerType,
    val textHtml: String,
)

internal enum class CalendarEventResponse {
    ACCEPTED,
    DECLINED,
    NOT_RESPONDED,
    TENTATIVE,
}

internal enum class CalendarOwnerType {
    CHARACTER,
    CORPORATION,
    ALLIANCE,
    FACTION,
    EVE_SERVER,
    UNKNOWN,
}

@Serializable
internal enum class CalendarReminderLead(val offsetMinutes: Int) {
    TWO_HOURS(CalendarReminderOffsets.TWO_HOURS_MINUTES),
    ONE_HOUR(CalendarReminderOffsets.ONE_HOUR_MINUTES),
    THIRTY_MINUTES(CalendarReminderOffsets.THIRTY_MINUTES),
    FIFTEEN_MINUTES(CalendarReminderOffsets.FIFTEEN_MINUTES),
    AT_START(CalendarReminderOffsets.AT_START_MINUTES),
    ;

    val offsetMs: Long
        get() = offsetMinutes * CalendarTimeConfig.MILLIS_PER_MINUTE
}

@Serializable
internal data class CalendarReminder(
    val characterId: Long,
    val eventId: Long,
    val eventTitle: String,
    val eventStartEpochMs: Long,
    val lead: CalendarReminderLead,
    val fireAtEpochMs: Long,
)

internal enum class CalendarAddReminderResult {
    ADDED,
    DUPLICATE,
    FIRE_TIME_PASSED,
    EXACT_ALARM_DENIED,
}
