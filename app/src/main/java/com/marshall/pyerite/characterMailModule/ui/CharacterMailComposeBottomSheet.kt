package com.marshall.pyerite.characterMailModule.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMailModule.model.CharacterMailComposeDraft
import com.marshall.pyerite.characterMailModule.model.MailComposeRecipient
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailComposeViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CharacterMailComposeBottomSheet(
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.character_mail_compose),
    draft: CharacterMailComposeDraft = CharacterMailComposeDraft(),
    viewModel: CharacterMailComposeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sendSuccessMessage = stringResource(R.string.character_mail_compose_send_success)
    val sendFailedMessage = stringResource(R.string.character_mail_compose_send_failed)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val bodyFocusRequester = remember { FocusRequester() }
    var subject by rememberSaveable { mutableStateOf(draft.subject) }
    var body by rememberSaveable { mutableStateOf(draft.body) }
    val selectedRecipients = remember {
        mutableStateListOf<MailComposeRecipient>().also { selected ->
            selected.addAll(draft.recipients)
        }
    }
    var showRecipientPicker by rememberSaveable { mutableStateOf(false) }
    var showMailingListPicker by rememberSaveable { mutableStateOf(false) }
    val canSend = selectedRecipients.isNotEmpty() &&
        subject.isNotBlank() &&
        body.isNotBlank() &&
        !uiState.isSending

    LaunchedEffect(uiState.sendSucceeded, uiState.sendFailed) {
        when {
            uiState.sendSucceeded -> {
                Toast.makeText(context, sendSuccessMessage, Toast.LENGTH_SHORT).show()
                viewModel.consumeSendResult()
                onDismiss()
            }
            uiState.sendFailed -> {
                Toast.makeText(context, sendFailedMessage, Toast.LENGTH_SHORT).show()
                viewModel.consumeSendResult()
            }
        }
    }

    CharacterMailModalSheet(
        onDismiss = { if (!uiState.isSending) onDismiss() },
        header = {
            CharacterMailSheetHeader(
                title = title,
                startLabel = stringResource(R.string.character_mail_compose_cancel),
                startEnabled = !uiState.isSending,
                onStart = { if (!uiState.isSending) onDismiss() },
                endLabel = stringResource(R.string.character_mail_compose_send),
                endEnabled = canSend,
                onEnd = {
                    viewModel.sendMail(
                        recipients = selectedRecipients.toList(),
                        subject = subject,
                        body = body,
                    )
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = sectionGap, bottom = bottomPadding),
        ) {
            CharacterMailComposeRecipientsSection(
                recipients = selectedRecipients,
                onAddRecipientClick = { showRecipientPicker = true },
                onAddMailingListClick = { showMailingListPicker = true },
                onRemoveRecipient = { party ->
                    val index = selectedRecipients.indexOfFirst { it.id == party.id }
                    if (index >= 0) selectedRecipients.removeAt(index)
                },
            )
            Spacer(modifier = Modifier.height(sectionGap))
            CharacterMailComposeSubjectSection(
                subject = subject,
                onSubjectChange = { subject = sanitizeMailSubject(it) },
                onNext = { bodyFocusRequester.requestFocus() },
            )
            Spacer(modifier = Modifier.height(sectionGap))
            CharacterMailComposeBodySection(
                body = body,
                onBodyChange = { body = it },
                focusRequester = bodyFocusRequester,
            )
        }
    }

    if (showRecipientPicker) {
        CharacterMailAddRecipientBottomSheet(
            viewModel = viewModel,
            onDone = { picked -> selectedRecipients.addDistinct(picked) },
            onDismiss = { showRecipientPicker = false },
        )
    }
    if (showMailingListPicker) {
        CharacterMailSelectMailingListBottomSheet(
            viewModel = viewModel,
            onDone = { picked -> selectedRecipients.addDistinct(picked) },
            onDismiss = { showMailingListPicker = false },
        )
    }
}

@Composable
private fun CharacterMailComposeRecipientsSection(
    recipients: List<MailComposeRecipient>,
    onAddRecipientClick: () -> Unit,
    onAddMailingListClick: () -> Unit,
    onRemoveRecipient: (MailComposeRecipient) -> Unit,
) {
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    BaseContainer(
        title = stringResource(R.string.character_mail_compose_recipients),
        useSystemBarsPadding = false,
    ) {
        recipients.forEach { party ->
            CharacterMailPartyRow(
                party = party,
                placeholder = placeholder,
                showDivider = true,
                onRemove = { onRemoveRecipient(party) },
            )
        }
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(R.string.character_mail_compose_add_recipient),
                showChevron = true,
                onClick = onAddRecipientClick,
            ),
            showDivider = true,
        )
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(R.string.character_mail_compose_add_mailing_list),
                showChevron = true,
                onClick = onAddMailingListClick,
            ),
            showDivider = false,
        )
    }
}

@Composable
private fun CharacterMailComposeSubjectSection(
    subject: String,
    onSubjectChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    val textColor = colorResource(R.color.text_primary)
    val textSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val lineHeight = dimensionResource(R.dimen.sub_menu_label_line_height).value.sp
    BaseContainer(
        title = stringResource(R.string.character_mail_compose_subject),
        useSystemBarsPadding = false,
    ) {
        BasicTextField(
            value = subject,
            onValueChange = onSubjectChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(R.dimen.detail_row_horizontal_padding),
                    vertical = dimensionResource(R.dimen.detail_row_vertical_padding_single_line),
                ),
            singleLine = true,
            maxLines = CharacterMailComposeConfig.SUBJECT_MAX_LINES,
            textStyle = TextStyle(
                color = textColor,
                fontSize = textSize,
                lineHeight = lineHeight,
            ),
            cursorBrush = SolidColor(textColor),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
        )
    }
}

@Composable
private fun CharacterMailComposeBodySection(
    body: String,
    onBodyChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    val textColor = colorResource(R.color.text_primary)
    val textSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val lineHeight = dimensionResource(R.dimen.sub_menu_label_line_height).value.sp
    BaseContainer(
        title = stringResource(R.string.character_mail_compose_body),
        useSystemBarsPadding = false,
    ) {
        BasicTextField(
            value = body,
            onValueChange = onBodyChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .padding(
                    horizontal = dimensionResource(R.dimen.detail_row_horizontal_padding),
                    vertical = dimensionResource(R.dimen.detail_row_vertical_padding_single_line),
                ),
            minLines = CharacterMailComposeConfig.BODY_MIN_LINES,
            textStyle = TextStyle(
                color = textColor,
                fontSize = textSize,
                lineHeight = lineHeight,
            ),
            cursorBrush = SolidColor(textColor),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
        )
    }
}

private fun MutableList<MailComposeRecipient>.addDistinct(
    parties: List<MailComposeRecipient>,
) {
    val existingIds = mapTo(hashSetOf()) { it.id }
    parties.forEach { party ->
        if (party.id !in existingIds) {
            add(party)
            existingIds.add(party.id)
        }
    }
}

private fun sanitizeMailSubject(value: String): String =
    value.filterNot { char ->
        char == CharacterMailComposeConfig.NEWLINE ||
            char == CharacterMailComposeConfig.CARRIAGE_RETURN
    }

private object CharacterMailComposeConfig {
    const val SUBJECT_MAX_LINES = 1
    const val BODY_MIN_LINES = 8
    const val NEWLINE = '\n'
    const val CARRIAGE_RETURN = '\r'
}
