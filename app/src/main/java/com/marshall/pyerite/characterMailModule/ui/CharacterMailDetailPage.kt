package com.marshall.pyerite.characterMailModule.ui

import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMailModule.model.CharacterMailDetail
import com.marshall.pyerite.characterMailModule.model.CharacterMailParticipant
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailDetailViewModel
import com.marshall.pyerite.ui.golbalComponents.CharacterAvatar
import com.marshall.pyerite.ui.golbalComponents.PyeriteIconShape
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel

private object CharacterMailDetailHeaderLayout {
    const val EXPAND_MIN_RECIPIENTS = 2
}

@Composable
internal fun CharacterMailDetailPage(
    navController: NavController,
    viewModel: CharacterMailDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val subject = uiState.detail.subject.takeIf { it.isNotBlank() } ?: placeholder
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val headerGap = dimensionResource(R.dimen.character_mail_detail_header_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val horizontalPadding = dimensionResource(R.dimen.detail_card_horizontal_padding)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refresh,
        ),
    )

    PyeritePageScaffold(
        title = subject,
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
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = bottomPadding),
            ) {
                MailDetailTitle(text = subject)
                Spacer(modifier = Modifier.height(headerGap))
                MailDetailMeta(
                    detail = uiState.detail,
                    placeholder = placeholder,
                )
                Spacer(modifier = Modifier.height(headerGap))
                when {
                    !uiState.detailsReady -> {
                        Text(
                            text = placeholder,
                            color = colorResource(R.color.hint_text),
                            fontSize = dimensionResource(R.dimen.type_detail_body_text_size).value.sp,
                        )
                    }
                    uiState.loadFailed && uiState.detail.bodyHtml.isBlank() -> {
                        Text(
                            text = stringResource(R.string.character_sheet_load_failed),
                            color = colorResource(R.color.text_primary),
                            fontSize = dimensionResource(R.dimen.type_detail_body_text_size).value.sp,
                        )
                    }
                    uiState.detail.bodyHtml.isBlank() -> {
                        Text(
                            text = placeholder,
                            color = colorResource(R.color.hint_text),
                            fontSize = dimensionResource(R.dimen.type_detail_body_text_size).value.sp,
                        )
                    }
                    else -> {
                        MailDetailBody(html = uiState.detail.bodyHtml)
                    }
                }
            }
        }
    }
}

@Composable
private fun MailDetailTitle(text: String) {
    val pageTitleTextSize = dimensionResource(R.dimen.list_page_title_text_size).value.sp
    val titleVerticalPadding = dimensionResource(R.dimen.type_detail_page_title_vertical_padding)
    Text(
        text = text,
        fontSize = pageTitleTextSize,
        fontWeight = FontWeight.Black,
        color = colorResource(R.color.text_primary),
        modifier = Modifier.padding(
            top = titleVerticalPadding,
            bottom = dimensionResource(R.dimen.list_page_title_bottom_padding),
        ),
    )
}

@Composable
private fun MailDetailMeta(
    detail: CharacterMailDetail,
    placeholder: String,
) {
    val metaGap = dimensionResource(R.dimen.character_mail_detail_meta_gap)
    val dividerPadding = dimensionResource(R.dimen.type_detail_summary_divider_vertical_padding)
    Column(modifier = Modifier.fillMaxWidth()) {
        MailDetailSenderRow(
            detail = detail,
            placeholder = placeholder,
        )
        Spacer(modifier = Modifier.height(metaGap))
        MailDetailRecipients(
            recipients = detail.recipients,
            mailId = detail.mailId,
            placeholder = placeholder,
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = dividerPadding),
            thickness = dimensionResource(R.dimen.detail_divider_thickness),
            color = colorResource(R.color.border),
        )
    }
}

@Composable
private fun MailDetailSenderRow(
    detail: CharacterMailDetail,
    placeholder: String,
) {
    val senderName = detail.senderName?.takeIf { it.isNotBlank() } ?: placeholder
    val receivedText = detail.receivedAtEpochMs?.let(::formatMailReceivedAt) ?: placeholder
    val avatarSize = dimensionResource(R.dimen.character_mail_detail_sender_avatar_size)
    val avatarGap = dimensionResource(R.dimen.detail_row_icon_gap)
    val nameTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val timeTextSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp
    val timeLineHeight = dimensionResource(R.dimen.detail_row_label_subtitle_line_height).value.sp
    val nameTimeGap = dimensionResource(R.dimen.detail_row_label_subtitle_spacing)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CharacterAvatar(
            portraitUrl = detail.senderPortraitUrl,
            size = avatarSize,
            shape = PyeriteIconShape.shape,
        )
        Spacer(modifier = Modifier.width(avatarGap))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = senderName,
                color = colorResource(R.color.text_primary),
                fontSize = nameTextSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(nameTimeGap))
            Text(
                text = receivedText,
                color = colorResource(R.color.hint_text),
                fontSize = timeTextSize,
                lineHeight = timeLineHeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MailDetailRecipients(
    recipients: List<CharacterMailParticipant>,
    mailId: Long,
    placeholder: String,
) {
    val recipientTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val extraGap = dimensionResource(R.dimen.detail_row_label_subtitle_spacing)
    val canExpand = recipients.size >= CharacterMailDetailHeaderLayout.EXPAND_MIN_RECIPIENTS
    var expanded by rememberSaveable(mailId) { mutableStateOf(false) }
    val firstName = recipients.firstOrNull()?.displayName(placeholder) ?: placeholder
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = mailRecipientsLabel(),
            color = colorResource(R.color.hint_text),
            fontSize = recipientTextSize,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = firstName,
                    color = colorResource(R.color.text_primary),
                    fontSize = recipientTextSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (canExpand) {
                    MailDetailRecipientsToggle(
                        expanded = expanded,
                        onClick = { expanded = !expanded },
                    )
                }
            }
            if (canExpand && expanded) {
                Column(
                    modifier = Modifier.padding(top = extraGap),
                    verticalArrangement = Arrangement.spacedBy(extraGap),
                ) {
                    recipients.drop(1).forEach { recipient ->
                        Text(
                            text = recipient.displayName(placeholder),
                            color = colorResource(R.color.text_primary),
                            fontSize = recipientTextSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MailDetailRecipientsToggle(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val linkColor = colorResource(R.color.hyperlink_text)
    val textSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp
    val iconSize = dimensionResource(R.dimen.character_mail_detail_expand_icon_size)
    val label = if (expanded) {
        stringResource(R.string.character_mail_show_less)
    } else {
        stringResource(R.string.character_mail_show_more)
    }
    Row(
        modifier = Modifier.clickable(
            onClick = onClick,
            role = Role.Button,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = linkColor,
            fontSize = textSize,
            maxLines = 1,
        )
        Icon(
            imageVector = if (expanded) {
                Icons.Filled.KeyboardArrowUp
            } else {
                Icons.Filled.KeyboardArrowDown
            },
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = linkColor,
        )
    }
}

@Composable
private fun MailDetailBody(html: String) {
    val textColor = colorResource(R.color.text_primary).toArgb()
    val textSizeSp = dimensionResource(R.dimen.type_detail_body_text_size).value
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                setTextColor(textColor)
                setLinkTextColor(textColor)
                textSize = textSizeSp
                setTextIsSelectable(true)
                linksClickable = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { view ->
            view.setTextColor(textColor)
            view.setLinkTextColor(textColor)
            view.text = mailBodyWithoutLinkStyling(html)
        },
    )
}

private fun CharacterMailParticipant.displayName(placeholder: String): String =
    name?.takeIf { it.isNotBlank() } ?: placeholder
