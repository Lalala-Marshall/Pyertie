package com.marshall.pyerite.characterMailModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMailModule.model.CharacterMailHeader
import com.marshall.pyerite.characterMailModule.navHost.CharacterMailRoute
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.CharacterAvatar
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeriteIconShape
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel

private object CharacterMailListItemLayout {
    const val SUBJECT_MAX_LINES = 1
}

@Composable
internal fun CharacterMailListPage(
    navController: NavController,
    viewModel: CharacterMailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val pageTitle = viewModel.mailbox?.let { mailbox ->
        mailboxDisplayName(mailbox, placeholder)
    } ?: stringResource(R.string.character_mail_all)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refresh,
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
                CharacterMailListSection(
                    mails = uiState.inbox.mails,
                    detailsPending = !uiState.detailsReady,
                    loadFailed = uiState.loadFailed,
                    placeholder = placeholder,
                    onMailClick = { mailId ->
                        navController.navigate(
                            CharacterMailRoute.Detail.create(viewModel.characterId, mailId),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun CharacterMailListSection(
    mails: List<CharacterMailHeader>,
    detailsPending: Boolean,
    loadFailed: Boolean,
    placeholder: String,
    onMailClick: (mailId: Long) -> Unit,
) {
    BaseContainer(
        title = null,
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
            loadFailed && mails.isEmpty() -> {
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
            mails.isEmpty() -> {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.character_mail_all_empty),
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = false,
                )
            }
            else -> {
                Column {
                    mails.forEachIndexed { index, mail ->
                        CharacterMailListItem(
                            mail = mail,
                            placeholder = placeholder,
                            showDivider = index != mails.lastIndex,
                            onClick = { onMailClick(mail.mailId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterMailListItem(
    mail: CharacterMailHeader,
    placeholder: String,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    val subject = mail.subject.takeIf { it.isNotBlank() } ?: placeholder
    val senderName = mail.senderName?.takeIf { it.isNotBlank() } ?: placeholder
    val receivedText = mail.receivedAtEpochMs?.let(::formatMailReceivedAt) ?: placeholder
    val senderHint = mailSenderHint(senderName)

    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            itemName = subject,
            itemNameBold = true,
            itemNameMaxLines = CharacterMailListItemLayout.SUBJECT_MAX_LINES,
            itemHints = listOf(
                BaseLazyColumnItemHint(annotatedText = senderHint),
                BaseLazyColumnItemHint(text = receivedText),
            ),
            showChevron = true,
            onClick = onClick,
        ),
        showDivider = showDivider,
        leadingContent = { iconSize ->
            CharacterAvatar(
                portraitUrl = mail.senderPortraitUrl,
                size = iconSize,
                shape = PyeriteIconShape.shape,
            )
        },
    )
}
