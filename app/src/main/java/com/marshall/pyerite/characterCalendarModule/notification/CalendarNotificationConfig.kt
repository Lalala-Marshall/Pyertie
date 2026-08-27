package com.marshall.pyerite.characterCalendarModule.notification

internal object CalendarNotificationConfig {
    const val CHANNEL_ID = "calendar_events"
    const val ACTION_REMINDER = "com.marshall.pyerite.CALENDAR_REMINDER"
    const val EXTRA_CHARACTER_ID = "character_id"
    const val EXTRA_EVENT_ID = "event_id"
    const val EXTRA_LEAD = "lead"
    const val EXTRA_TITLE = "title"
    /** Same value as `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` (API 31). */
    const val ACTION_REQUEST_SCHEDULE_EXACT_ALARM =
        "android.settings.REQUEST_SCHEDULE_EXACT_ALARM"
    const val PACKAGE_URI_PREFIX = "package:"
}
