package com.marshall.pyerite.characterCalendarModule.model

import com.marshall.pyerite.localization.ContentLanguage
import java.util.Calendar
import java.util.Locale

internal object CalendarDates {

    fun today(): CalendarDate = fromEpochMs(System.currentTimeMillis())

    fun fromEpochMs(epochMs: Long): CalendarDate {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        return fromJavaCalendar(cal)
    }

    fun contentLocale(language: ContentLanguage): Locale =
        when (language) {
            ContentLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
            ContentLanguage.ENGLISH -> Locale.US
        }

    fun firstDayOfWeek(locale: Locale): Int = Calendar.getInstance(locale).firstDayOfWeek

    fun startOfDayEpochMs(date: CalendarDate): Long {
        val cal = atDate(date)
        clearTime(cal)
        return cal.timeInMillis
    }

    fun startOfNextDayEpochMs(date: CalendarDate): Long {
        val cal = atDate(date)
        clearTime(cal)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return cal.timeInMillis
    }

    fun endOfMonthEpochMs(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - CalendarTimeConfig.JAVA_CALENDAR_MONTH_OFFSET)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        clearTime(cal)
        cal.add(Calendar.MONTH, 1)
        return cal.timeInMillis
    }

    fun previousMonthStartEpochMs(nowEpochMs: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = nowEpochMs
        cal.set(Calendar.DAY_OF_MONTH, 1)
        clearTime(cal)
        cal.add(Calendar.MONTH, -1)
        return cal.timeInMillis
    }

    fun shiftMonth(year: Int, month: Int, delta: Int): CalendarDate {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - CalendarTimeConfig.JAVA_CALENDAR_MONTH_OFFSET)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, delta)
        return CalendarDate(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + CalendarTimeConfig.JAVA_CALENDAR_MONTH_OFFSET,
            day = 1,
        )
    }

    fun monthCells(year: Int, month: Int, firstDayOfWeek: Int): List<CalendarDayCell> {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - CalendarTimeConfig.JAVA_CALENDAR_MONTH_OFFSET)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDow = cal.get(Calendar.DAY_OF_WEEK)
        val leading =
            (firstDow - firstDayOfWeek + CalendarTimeConfig.DAYS_PER_WEEK) %
                CalendarTimeConfig.DAYS_PER_WEEK
        val total = leading + daysInMonth
        val trailing =
            (CalendarTimeConfig.DAYS_PER_WEEK - (total % CalendarTimeConfig.DAYS_PER_WEEK)) %
                CalendarTimeConfig.DAYS_PER_WEEK
        cal.add(Calendar.DAY_OF_MONTH, -leading)
        val cellCount = total + trailing
        return List(cellCount) {
            val date = fromJavaCalendar(cal)
            val inCurrentMonth = date.year == year && date.month == month
            cal.add(Calendar.DAY_OF_MONTH, 1)
            CalendarDayCell(date = date, inCurrentMonth = inCurrentMonth)
        }
    }

    private fun atDate(date: CalendarDate): Calendar {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(Calendar.YEAR, date.year)
        cal.set(Calendar.MONTH, date.month - CalendarTimeConfig.JAVA_CALENDAR_MONTH_OFFSET)
        cal.set(Calendar.DAY_OF_MONTH, date.day)
        return cal
    }

    private fun fromJavaCalendar(cal: Calendar): CalendarDate = CalendarDate(
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH) + CalendarTimeConfig.JAVA_CALENDAR_MONTH_OFFSET,
        day = cal.get(Calendar.DAY_OF_MONTH),
    )

    private fun clearTime(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }
}
