package com.marshall.pyerite.characterMailModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterMailModule.model.MailComposeRecipient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CharacterMailComposeViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CharacterMailRepository,
) : ViewModel() {

    private val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "Missing $NAV_ARG_CHARACTER_ID"
    }

    private val _uiState = MutableStateFlow(CharacterMailComposePickerUiState())
    val uiState: StateFlow<CharacterMailComposePickerUiState> = _uiState.asStateFlow()

    private var recentContactsJob: Job? = null
    private var mailingListsJob: Job? = null
    private var searchJob: Job? = null
    private var sendJob: Job? = null

    fun ensureRecentContacts() {
        if (_uiState.value.recentContactsReady || recentContactsJob?.isActive == true) return
        recentContactsJob = viewModelScope.launch {
            _uiState.update { it.copy(recentContactsFailed = false) }
            val result = runCatching { repository.loadRecentMailContacts(characterId) }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { contacts ->
                        current.copy(
                            recentContacts = contacts,
                            recentContactsReady = true,
                            recentContactsFailed = false,
                        )
                    },
                    onFailure = {
                        current.copy(
                            recentContactsReady = false,
                            recentContactsFailed = true,
                        )
                    },
                )
            }
        }
    }

    fun ensureMailingLists() {
        if (_uiState.value.mailingListsReady || mailingListsJob?.isActive == true) return
        mailingListsJob = viewModelScope.launch {
            _uiState.update { it.copy(mailingListsFailed = false) }
            val result = runCatching { repository.loadSubscribedMailingLists(characterId) }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { lists ->
                        current.copy(
                            mailingLists = lists,
                            mailingListsReady = true,
                            mailingListsFailed = false,
                        )
                    },
                    onFailure = {
                        current.copy(
                            mailingListsReady = false,
                            mailingListsFailed = true,
                        )
                    },
                )
            }
        }
    }

    fun searchRecipients(query: String) {
        val trimmed = query.trim()
        searchJob?.cancel()
        if (trimmed.isEmpty()) {
            clearSearch()
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    searchSubmitted = true,
                    isSearching = true,
                    searchFailed = false,
                )
            }
            val result = runCatching { repository.searchUniverseRecipients(trimmed) }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { matches ->
                        current.copy(
                            searchResults = matches,
                            isSearching = false,
                            searchFailed = false,
                        )
                    },
                    onFailure = {
                        current.copy(
                            searchResults = emptyList(),
                            isSearching = false,
                            searchFailed = true,
                        )
                    },
                )
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchResults = emptyList(),
                searchSubmitted = false,
                isSearching = false,
                searchFailed = false,
            )
        }
    }

    fun sendMail(
        recipients: List<MailComposeRecipient>,
        subject: String,
        body: String,
    ) {
        if (_uiState.value.isSending || sendJob?.isActive == true) return
        val trimmedSubject = subject.trim()
        val trimmedBody = body.trim()
        if (recipients.isEmpty() || trimmedSubject.isEmpty() || trimmedBody.isEmpty()) return
        sendJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isSending = true, sendSucceeded = false, sendFailed = false)
            }
            val result = runCatching {
                repository.sendMail(characterId, recipients, trimmedSubject, trimmedBody)
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = {
                        current.copy(isSending = false, sendSucceeded = true, sendFailed = false)
                    },
                    onFailure = {
                        current.copy(isSending = false, sendSucceeded = false, sendFailed = true)
                    },
                )
            }
        }
    }

    fun consumeSendResult() {
        _uiState.update { it.copy(sendSucceeded = false, sendFailed = false) }
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = CharacterMailViewModel.NAV_ARG_CHARACTER_ID
    }
}

internal data class CharacterMailComposePickerUiState(
    val recentContacts: List<MailComposeRecipient> = emptyList(),
    val recentContactsReady: Boolean = false,
    val recentContactsFailed: Boolean = false,
    val mailingLists: List<MailComposeRecipient> = emptyList(),
    val mailingListsReady: Boolean = false,
    val mailingListsFailed: Boolean = false,
    val searchResults: List<MailComposeRecipient> = emptyList(),
    val searchSubmitted: Boolean = false,
    val isSearching: Boolean = false,
    val searchFailed: Boolean = false,
    val isSending: Boolean = false,
    val sendSucceeded: Boolean = false,
    val sendFailed: Boolean = false,
)
