package com.marshall.pyerite.characterMailModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailbox
import com.marshall.pyerite.characterMailModule.navHost.CharacterMailRoute
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailHubViewModel
import com.marshall.pyerite.esiModule.model.EsiMailLabelId
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CharacterMailPage(
    navController: NavController,
    viewModel: CharacterMailHubViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_mail)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    var showComposeSheet by rememberSaveable { mutableStateOf(false) }
    val composeDescription = stringResource(R.string.character_mail_compose)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refresh,
        ),
        PyeriteTopBarActionItem(
            onClick = { showComposeSheet = true },
            icon = Icons.Outlined.Edit,
            contentDescription = composeDescription,
        ),
    )

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = endActions,
    ) { topBarPadding ->
        PyeritePullToRefreshBox(
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = bottomPadding),
            ) {
                PageTitle(text = pageTitle)
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterMailInboxSection(
                    onAllMailClick = {
                        navController.navigate(
                            CharacterMailRoute.List.create(viewModel.characterId),
                        )
                    },
                )
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterMailMailboxesSection(
                    mailboxes = uiState.mailboxes.mailboxes,
                    detailsPending = !uiState.detailsReady,
                    loadFailed = uiState.loadFailed,
                    placeholder = placeholder,
                    onMailboxClick = { labelId ->
                        navController.navigate(
                            CharacterMailRoute.List.create(viewModel.characterId, labelId),
                        )
                    },
                )
            }
        }
    }

    if (showComposeSheet) {
        CharacterMailComposeBottomSheet(
            onDismiss = { showComposeSheet = false },
        )
    }
}

@Composable
private fun CharacterMailInboxSection(
    onAllMailClick: () -> Unit,
) {
    BaseContainer(
        title = null,
        useSystemBarsPadding = false,
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = CharacterMailHubIcon.Default,
                itemName = stringResource(R.string.character_mail_all),
                onClick = onAllMailClick,
            ),
            showDivider = false,
        )
    }
}

@Composable
private fun CharacterMailMailboxesSection(
    mailboxes: List<CharacterMailMailbox>,
    detailsPending: Boolean,
    loadFailed: Boolean,
    placeholder: String,
    onMailboxClick: (labelId: Int) -> Unit,
) {
    BaseContainer(
        title = stringResource(R.string.character_mail_mailboxes),
        useSystemBarsPadding = false,
    ) {
        when {
            detailsPending -> {
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
            loadFailed && mailboxes.isEmpty() -> {
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
            mailboxes.isEmpty() -> {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.character_mail_mailboxes_empty),
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = false,
                )
            }
            else -> {
                Column {
                    mailboxes.forEachIndexed { index, mailbox ->
                        BaseLazyColumnItem(
                            model = BaseLazyColumnItemModel(
                                iconRes = mailboxIconRes(mailbox.labelId),
                                iconTint = mailboxIconTint(mailbox.labelId),
                                itemName = mailboxDisplayName(mailbox, placeholder),
                                showChevron = true,
                                onClick = { onMailboxClick(mailbox.labelId) },
                            ),
                            showDivider = index != mailboxes.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

private object CharacterMailHubIcon {
    val Default = R.drawable.ic_character_mail
}

private fun mailboxIconRes(labelId: Int): Int = when (labelId) {
    EsiMailLabelId.INBOX -> R.drawable.ic_character_mail_inbox
    EsiMailLabelId.SENT -> R.drawable.ic_character_mail_sent
    EsiMailLabelId.CORPORATION -> R.drawable.ic_character_mail_corporation
    EsiMailLabelId.ALLIANCE -> R.drawable.ic_character_mail_alliance
    else -> CharacterMailHubIcon.Default
}

@Composable
private fun mailboxIconTint(labelId: Int): Color? = when (labelId) {
    EsiMailLabelId.INBOX, EsiMailLabelId.SENT -> colorResource(R.color.hint_text)
    else -> null
}
