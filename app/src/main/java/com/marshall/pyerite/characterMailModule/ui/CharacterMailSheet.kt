package com.marshall.pyerite.characterMailModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMailModule.model.MailComposeRecipient
import com.marshall.pyerite.characterMailModule.model.MailRecipientKind
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.CharacterAvatar
import com.marshall.pyerite.ui.golbalComponents.PyeriteIconShape

internal object CharacterMailSheetConfig {
    const val HEIGHT_FRACTION = 0.94f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CharacterMailModalSheet(
    onDismiss: () -> Unit,
    header: @Composable () -> Unit,
    belowHeader: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCorner = dimensionResource(R.dimen.character_mail_compose_sheet_corner)
    val sheetBackground = colorResource(R.color.search_field_idle_background)
    val sheetHorizontalPadding =
        dimensionResource(R.dimen.character_mail_compose_sheet_horizontal_padding)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        shape = RoundedCornerShape(topStart = sheetCorner, topEnd = sheetCorner),
        containerColor = sheetBackground,
        scrimColor = colorResource(R.color.search_scrim),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(CharacterMailSheetConfig.HEIGHT_FRACTION)
                .imePadding()
                .background(sheetBackground),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = sheetHorizontalPadding,
                    end = sheetHorizontalPadding,
                    top = dimensionResource(R.dimen.character_mail_compose_header_top_padding),
                ),
            ) {
                header()
                belowHeader()
            }
            content()
        }
    }
}

@Composable
internal fun CharacterMailSheetHeader(
    title: String,
    endLabel: String,
    onEnd: () -> Unit,
    endEnabled: Boolean = true,
    startLabel: String? = null,
    onStart: (() -> Unit)? = null,
    startEnabled: Boolean = true,
) {
    val buttonHeight = dimensionResource(R.dimen.top_bar_back_button_size)
    val actionColor = colorResource(R.color.hyperlink_text)
    val disabledColor = colorResource(R.color.text_caption)
    val actionTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
    ) {
        if (startLabel != null && onStart != null) {
            Text(
                text = startLabel,
                color = if (startEnabled) actionColor else disabledColor,
                fontSize = actionTextSize,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(
                        enabled = startEnabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStart,
                    )
                    .semantics { role = Role.Button },
            )
        }
        Text(
            text = title,
            color = colorResource(R.color.text_primary),
            fontSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    horizontal = dimensionResource(R.dimen.character_mail_compose_header_title_inset),
                ),
        )
        Text(
            text = endLabel,
            color = if (endEnabled) actionColor else disabledColor,
            fontSize = actionTextSize,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(
                    enabled = endEnabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onEnd,
                )
                .semantics { role = Role.Button },
        )
    }
}

@Composable
internal fun CharacterMailSheetSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val hintColor = colorResource(R.color.hint_text)
    val textColor = colorResource(R.color.text_primary)
    val iconSize = dimensionResource(R.dimen.character_mail_compose_search_icon_size)
    val iconGap = dimensionResource(R.dimen.character_mail_compose_search_icon_gap)
    val horizontalPadding =
        dimensionResource(R.dimen.character_mail_compose_search_horizontal_padding)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(percent = 50))
            .background(colorResource(R.color.second_background)),
        singleLine = true,
        textStyle = TextStyle(
            color = textColor,
            fontSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp,
        ),
        cursorBrush = SolidColor(textColor),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = hintColor,
                    modifier = Modifier
                        .size(iconSize)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSearch,
                        )
                        .semantics { role = Role.Button },
                )
                Spacer(modifier = Modifier.width(iconGap))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.character_mail_compose_recipient_search_hint),
                            color = hintColor,
                            fontSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
internal fun CharacterMailPartyRow(
    party: MailComposeRecipient,
    placeholder: String,
    showDivider: Boolean,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    val name = party.name?.takeIf { it.isNotBlank() } ?: placeholder
    val removeLabel = stringResource(R.string.character_mail_compose_remove_recipient)
    val trailingIconSize = dimensionResource(R.dimen.detail_row_chevron_size)
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            itemName = name,
            showChevron = false,
            onClick = onClick,
        ),
        showDivider = showDivider,
        leadingContent = { iconSize ->
            if (party.kind == MailRecipientKind.MAILING_LIST) {
                Icon(
                    painter = painterResource(R.drawable.ic_character_mail),
                    contentDescription = null,
                    tint = colorResource(R.color.hint_text),
                    modifier = Modifier.size(iconSize),
                )
            } else {
                CharacterAvatar(
                    portraitUrl = party.portraitUrl,
                    size = iconSize,
                    shape = PyeriteIconShape.shape,
                )
            }
        },
        trailingContent = when {
            onRemove != null -> {
                {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = removeLabel,
                        tint = colorResource(R.color.character_delete),
                        modifier = Modifier
                            .size(trailingIconSize)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onRemove,
                            )
                            .semantics { role = Role.Button },
                    )
                }
            }
            selected -> {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = colorResource(R.color.hyperlink_text),
                        modifier = Modifier.size(trailingIconSize),
                    )
                }
            }
            else -> null
        },
    )
}
