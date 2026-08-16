package com.marshall.pyerite.characterMailModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMailModule.model.MailComposeRecipient
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailComposeViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.search.SearchNoResultsItem
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CharacterMailAddRecipientBottomSheet(
    onDone: (List<MailComposeRecipient>) -> Unit,
    onDismiss: () -> Unit,
    viewModel: CharacterMailComposeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var query by rememberSaveable { mutableStateOf("") }
    val selectedParties = remember { mutableStateListOf<MailComposeRecipient>() }
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    LaunchedEffect(Unit) {
        viewModel.ensureRecentContacts()
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.clearSearch() }
    }

    fun submitSearch() {
        keyboardController?.hide()
        viewModel.searchRecipients(query)
    }

    fun confirm() {
        onDone(selectedParties.toList())
        onDismiss()
    }

    CharacterMailModalSheet(
        onDismiss = onDismiss,
        header = {
            CharacterMailSheetHeader(
                title = stringResource(R.string.character_mail_compose_add_recipient),
                endLabel = stringResource(R.string.character_done),
                onEnd = ::confirm,
            )
        },
        belowHeader = {
            CharacterMailSheetSearchField(
                query = query,
                onQueryChange = { next ->
                    query = next
                    if (next.isBlank()) viewModel.clearSearch()
                },
                onSearch = ::submitSearch,
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.character_mail_compose_search_top_gap),
                    bottom = dimensionResource(R.dimen.character_mail_compose_search_bottom_gap),
                ),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding),
        ) {
            if (uiState.searchSubmitted) {
                CharacterMailPickerResultsSection(
                    title = stringResource(R.string.search),
                    parties = uiState.searchResults,
                    selectedIds = selectedParties.ids(),
                    placeholder = placeholder,
                    isLoading = uiState.isSearching,
                    loadFailed = uiState.searchFailed,
                    showEmpty = !uiState.isSearching && !uiState.searchFailed,
                    onToggle = { selectedParties.toggleParty(it) },
                )
                Spacer(modifier = Modifier.height(sectionGap))
            }
            CharacterMailPickerResultsSection(
                title = stringResource(R.string.character_mail_compose_quick_select),
                parties = uiState.recentContacts,
                selectedIds = selectedParties.ids(),
                placeholder = placeholder,
                isLoading = !uiState.recentContactsReady && !uiState.recentContactsFailed,
                loadFailed = uiState.recentContactsFailed,
                showEmpty = false,
                onToggle = { selectedParties.toggleParty(it) },
            )
        }
    }
}

@Composable
internal fun CharacterMailSelectMailingListBottomSheet(
    onDone: (List<MailComposeRecipient>) -> Unit,
    onDismiss: () -> Unit,
    viewModel: CharacterMailComposeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedParties = remember { mutableStateListOf<MailComposeRecipient>() }
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    LaunchedEffect(Unit) {
        viewModel.ensureMailingLists()
    }

    fun confirm() {
        onDone(selectedParties.toList())
        onDismiss()
    }

    CharacterMailModalSheet(
        onDismiss = onDismiss,
        header = {
            CharacterMailSheetHeader(
                title = stringResource(R.string.character_mail_compose_select_mailing_list),
                endLabel = stringResource(R.string.character_done),
                onEnd = ::confirm,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    top = dimensionResource(R.dimen.type_detail_section_gap),
                    bottom = bottomPadding,
                ),
        ) {
            CharacterMailPickerResultsSection(
                title = null,
                parties = uiState.mailingLists,
                selectedIds = selectedParties.ids(),
                placeholder = placeholder,
                isLoading = !uiState.mailingListsReady && !uiState.mailingListsFailed,
                loadFailed = uiState.mailingListsFailed,
                showEmpty = uiState.mailingListsReady && uiState.mailingLists.isEmpty(),
                emptyMessage = stringResource(R.string.character_mail_compose_mailing_lists_empty),
                onToggle = { selectedParties.toggleParty(it) },
            )
        }
    }
}

@Composable
private fun CharacterMailPickerResultsSection(
    title: String?,
    parties: List<MailComposeRecipient>,
    selectedIds: List<Long>,
    placeholder: String,
    isLoading: Boolean,
    loadFailed: Boolean,
    showEmpty: Boolean,
    onToggle: (MailComposeRecipient) -> Unit,
    emptyMessage: String? = null,
) {
    when {
        isLoading -> {
            BaseContainer(title = title, useSystemBarsPadding = false) {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = placeholder,
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = false,
                )
            }
        }
        loadFailed -> {
            BaseContainer(title = title, useSystemBarsPadding = false) {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.character_sheet_load_failed),
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = false,
                )
            }
        }
        parties.isEmpty() && showEmpty -> {
            if (emptyMessage != null) {
                BaseContainer(title = title, useSystemBarsPadding = false) {
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            showLeadingIcon = false,
                            itemName = emptyMessage,
                            showChevron = false,
                            onClick = null,
                        ),
                        showDivider = false,
                    )
                }
            } else {
                SearchNoResultsItem()
            }
        }
        parties.isNotEmpty() -> {
            BaseContainer(title = title, useSystemBarsPadding = false) {
                parties.forEachIndexed { index, party ->
                    CharacterMailPartyRow(
                        party = party,
                        placeholder = placeholder,
                        showDivider = index != parties.lastIndex,
                        selected = party.id in selectedIds,
                        onClick = { onToggle(party) },
                    )
                }
            }
        }
    }
}

private fun MutableList<MailComposeRecipient>.toggleParty(party: MailComposeRecipient) {
    val index = indexOfFirst { it.id == party.id }
    if (index >= 0) {
        removeAt(index)
    } else {
        add(party)
    }
}

private fun List<MailComposeRecipient>.ids(): List<Long> = map { it.id }
