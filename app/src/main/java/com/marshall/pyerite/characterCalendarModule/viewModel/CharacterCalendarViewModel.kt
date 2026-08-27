package com.marshall.pyerite.characterCalendarModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterCalendarModule.model.CalendarAddReminderResult
import com.marshall.pyerite.characterCalendarModule.model.CalendarDate
import com.marshall.pyerite.characterCalendarModule.model.CalendarDates
import com.marshall.pyerite.characterCalendarModule.model.CalendarEventResponse
import com.marshall.pyerite.characterCalendarModule.model.CalendarReminder
import com.marshall.pyerite.characterCalendarModule.model.CalendarReminderLead
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEvent
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEventDetail
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharacterCalendarViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterCalendarRepository,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(initialUiState())
    val uiState: StateFlow<CharacterCalendarUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var detailJob: Job? = null

    init {
        repository.ensureNotificationChannel()
        repository.pruneFiredReminders()
        viewModelScope.launch {
            repository.reminders.collect { reminders ->
                _uiState.update { current ->
                    current.copy(
                        reminders = reminders
                            .filter { reminder ->
                                reminder.characterId == characterId &&
                                    reminder.fireAtEpochMs > System.currentTimeMillis()
                            }
                            .sortedBy { it.fireAtEpochMs },
                    )
                }
            }
        }
        load(indicateLoading = !_uiState.value.detailsReady)
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        load(indicateLoading = true)
    }

    fun selectDate(date: CalendarDate) {
        val monthChanged =
            date.year != _uiState.value.visibleYear || date.month != _uiState.value.visibleMonth
        _uiState.update {
            it.copy(
                visibleYear = date.year,
                visibleMonth = date.month,
                selectedDate = date,
            )
        }
        if (monthChanged) {
            ensureCoveringVisibleMonth()
        }
    }

    fun shiftVisibleMonth(delta: Int) {
        val shifted = CalendarDates.shiftMonth(
            year = _uiState.value.visibleYear,
            month = _uiState.value.visibleMonth,
            delta = delta,
        )
        val today = CalendarDates.today()
        val selected = if (shifted.year == today.year && shifted.month == today.month) {
            today
        } else {
            shifted
        }
        _uiState.update {
            it.copy(
                visibleYear = shifted.year,
                visibleMonth = shifted.month,
                selectedDate = selected,
            )
        }
        ensureCoveringVisibleMonth()
    }

    fun openEvent(eventId: Long) {
        val cached = repository.cachedDetail(characterId, eventId)
        _uiState.update {
            it.copy(
                openedEventId = eventId,
                openedDetail = cached,
                detailLoading = cached == null,
                detailFailed = false,
            )
        }
        if (cached != null) return
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            val result = runCatching { repository.loadDetail(characterId, eventId) }
            _uiState.update { current ->
                if (current.openedEventId != eventId) return@update current
                result.fold(
                    onSuccess = { detail ->
                        current.copy(
                            openedDetail = detail,
                            detailLoading = false,
                            detailFailed = false,
                        )
                    },
                    onFailure = {
                        current.copy(
                            detailLoading = false,
                            detailFailed = true,
                        )
                    },
                )
            }
        }
    }

    fun dismissEvent() {
        detailJob?.cancel()
        _uiState.update {
            it.copy(
                openedEventId = null,
                openedDetail = null,
                detailLoading = false,
                detailFailed = false,
            )
        }
    }

    fun addReminder(lead: CalendarReminderLead): CalendarAddReminderResult {
        val state = _uiState.value
        val event = eventForReminder(state) ?: return CalendarAddReminderResult.FIRE_TIME_PASSED
        val result = repository.addReminder(characterId, event, lead)
        refreshReminders()
        return result
    }

    fun removeReminder(reminder: CalendarReminder) {
        repository.removeReminder(reminder)
        refreshReminders()
    }

    fun exactAlarmSettingsIntent() = repository.exactAlarmSettingsIntent()

    private fun refreshReminders() {
        _uiState.update {
            it.copy(reminders = repository.pendingReminders(characterId))
        }
    }

    private fun eventForReminder(state: CharacterCalendarUiState): CharacterCalendarEvent? {
        val eventId = state.openedEventId ?: return null
        val summary = state.events.firstOrNull { it.eventId == eventId }
        val detail = state.openedDetail
        val startEpochMs = when {
            detail != null && detail.startEpochMs > 0L -> detail.startEpochMs
            else -> summary?.startEpochMs
        } ?: return null
        val title = detail?.title?.takeIf { it.isNotBlank() } ?: summary?.title.orEmpty()
        val response = detail?.response
            ?: summary?.response
            ?: CalendarEventResponse.NOT_RESPONDED
        return CharacterCalendarEvent(
            eventId = eventId,
            title = title,
            startEpochMs = startEpochMs,
            importance = detail?.importance ?: summary?.importance ?: 0,
            response = response,
        )
    }

    private fun initialUiState(): CharacterCalendarUiState {
        val today = CalendarDates.today()
        val cached = repository.cachedEvents(characterId)
        return CharacterCalendarUiState(
            events = cached,
            visibleYear = today.year,
            visibleMonth = today.month,
            selectedDate = today,
            isLoading = cached.isEmpty(),
            loadFailed = false,
            detailsReady = cached.isNotEmpty(),
            reminders = repository.pendingReminders(characterId),
            openedEventId = null,
            openedDetail = null,
            detailLoading = false,
            detailFailed = false,
        )
    }

    private fun ensureCoveringVisibleMonth() {
        val state = _uiState.value
        val monthEnd = CalendarDates.endOfMonthEpochMs(state.visibleYear, state.visibleMonth)
        val latestStart = state.events.maxOfOrNull { it.startEpochMs } ?: 0L
        if (latestStart >= monthEnd) return
        load(indicateLoading = false)
    }

    private fun load(indicateLoading: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hadCachedDetails = _uiState.value.detailsReady
            val coverUntil = CalendarDates.endOfMonthEpochMs(
                year = _uiState.value.visibleYear,
                month = _uiState.value.visibleMonth,
            )
            _uiState.update {
                it.copy(isLoading = indicateLoading, loadFailed = false)
            }
            val result = runCatching {
                repository.loadEvents(characterId, coverUntil)
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { events ->
                        current.copy(
                            events = events,
                            isLoading = false,
                            loadFailed = false,
                            detailsReady = true,
                        )
                    },
                    onFailure = {
                        current.copy(
                            isLoading = false,
                            loadFailed = !hadCachedDetails,
                        )
                    },
                )
            }
        }
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = "characterId"
    }
}

internal data class CharacterCalendarUiState(
    val events: List<CharacterCalendarEvent>,
    val visibleYear: Int,
    val visibleMonth: Int,
    val selectedDate: CalendarDate,
    val isLoading: Boolean,
    val loadFailed: Boolean,
    val detailsReady: Boolean,
    val reminders: List<CalendarReminder>,
    val openedEventId: Long?,
    val openedDetail: CharacterCalendarEventDetail?,
    val detailLoading: Boolean,
    val detailFailed: Boolean,
) {
    val selectedDayEvents: List<CharacterCalendarEvent>
        get() {
            val start = CalendarDates.startOfDayEpochMs(selectedDate)
            val end = CalendarDates.startOfNextDayEpochMs(selectedDate)
            return events
                .filter { it.startEpochMs in start until end }
                .sortedBy { it.startEpochMs }
        }

    fun eventCountOn(date: CalendarDate): Int {
        val start = CalendarDates.startOfDayEpochMs(date)
        val end = CalendarDates.startOfNextDayEpochMs(date)
        return events.count { it.startEpochMs in start until end }
    }
}
