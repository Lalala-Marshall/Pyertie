package com.marshall.pyerite.characterCalendarModule.ui

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.marshall.pyerite.R
import com.marshall.pyerite.characterCalendarModule.model.CalendarEventImportance
import com.marshall.pyerite.characterCalendarModule.model.CalendarOwnerType
import com.marshall.pyerite.characterCalendarModule.model.CalendarReminderLead
import com.marshall.pyerite.characterCalendarModule.model.CalendarTimeConfig
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEvent
import com.marshall.pyerite.characterCalendarModule.model.CharacterCalendarEventDetail
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.LocalOpenEntityProfile
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityKind
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityRef
import com.marshall.pyerite.util.formatDurationDisplay

@Composable
internal fun CalendarEventDetailSheet(
    summary: CharacterCalendarEvent?,
    detail: CharacterCalendarEventDetail?,
    loading: Boolean,
    failed: Boolean,
    onDismiss: () -> Unit,
    onAddReminder: (CalendarReminderLead) -> Unit,
) {
    var pickingLead by rememberSaveable { mutableStateOf(false) }
    val title = detail?.title ?: summary?.title ?: stringResource(R.string.character_calendar)
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)

    CalendarBottomSheet(
        title = title.ifBlank { placeholder },
        onDismiss = onDismiss,
    ) {
        CalendarEventDetailBody(
            modifier = Modifier.weight(1f),
            summary = summary,
            detail = detail,
            loading = loading,
            failed = failed,
            placeholder = placeholder,
            onAddReminder = { pickingLead = true },
        )
    }

    if (pickingLead) {
        CalendarReminderLeadSheet(
            onDismiss = { pickingLead = false },
            onSelect = { lead ->
                pickingLead = false
                onAddReminder(lead)
            },
        )
    }
}

@Composable
private fun CalendarReminderLeadSheet(
    onDismiss: () -> Unit,
    onSelect: (CalendarReminderLead) -> Unit,
) {
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    CalendarBottomSheet(
        title = stringResource(R.string.character_calendar_add_reminder),
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding),
        ) {
            Spacer(modifier = Modifier.height(sectionGap))
            BaseContainer(
                title = null,
                useSystemBarsPadding = false,
            ) {
                CalendarReminderLeadList(onSelect = onSelect)
            }
        }
    }
}

@Composable
private fun CalendarEventDetailBody(
    summary: CharacterCalendarEvent?,
    detail: CharacterCalendarEventDetail?,
    loading: Boolean,
    failed: Boolean,
    placeholder: String,
    onAddReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val startEpochMs = detail?.startEpochMs ?: summary?.startEpochMs
    val response = detail?.response ?: summary?.response
    val importance = detail?.importance ?: summary?.importance ?: 0
    val durationValue = when {
        detail != null -> {
            val durationSeconds = detail.durationMinutes * CalendarTimeConfig.SECONDS_PER_MINUTE
            formatDurationDisplay(durationSeconds, includeSeconds = false)
        }
        loading -> placeholder
        failed -> stringResource(R.string.character_sheet_load_failed)
        else -> placeholder
    }
    val organizerName = when {
        detail != null -> detail.ownerName.ifBlank { placeholder }
        loading -> placeholder
        failed -> stringResource(R.string.character_sheet_load_failed)
        else -> placeholder
    }
    val descriptionBlank = detail?.textHtml.isNullOrBlank()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = dimensionResource(R.dimen.type_detail_bottom_padding)),
    ) {
        Spacer(modifier = Modifier.height(sectionGap))
        BaseContainer(
            title = stringResource(R.string.character_calendar_section_basic),
            useSystemBarsPadding = false,
        ) {
            Column {
                CalendarDetailInfoRow(
                    label = stringResource(R.string.character_calendar_field_start),
                    value = startEpochMs?.let(::formatCalendarDateTime) ?: placeholder,
                    showDivider = true,
                )
                CalendarDetailInfoRow(
                    label = stringResource(R.string.character_calendar_field_duration),
                    value = durationValue,
                    showDivider = true,
                )
                CalendarDetailInfoRow(
                    label = stringResource(R.string.character_calendar_field_importance),
                    value = if (CalendarEventImportance.isImportant(importance)) {
                        stringResource(R.string.character_calendar_importance_important)
                    } else {
                        stringResource(R.string.character_calendar_importance_normal)
                    },
                    showDivider = true,
                )
                CalendarDetailInfoRow(
                    label = stringResource(R.string.character_calendar_field_response),
                    value = response?.let { calendarResponseLabel(it) } ?: placeholder,
                    showDivider = false,
                )
            }
        }
        Spacer(modifier = Modifier.height(sectionGap))
        BaseContainer(
            title = stringResource(R.string.character_calendar_section_organizer),
            useSystemBarsPadding = false,
        ) {
            val ownerKind = detail?.ownerType?.toEntityKind()
            val openEntityProfile = LocalOpenEntityProfile.current
            val ownerTypeLabel = if (detail != null) {
                calendarOwnerTypeLabel(detail.ownerType)
            } else {
                ""
            }
            val ownerIconUrl = detail?.let { calendarOwnerIconUrl(it.ownerType, it.ownerId) }
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    showLeadingIcon = ownerIconUrl != null,
                    iconUrl = ownerIconUrl,
                    itemName = organizerName,
                    itemNameColor = if (ownerKind != null) {
                        colorResource(R.color.hyperlink_text)
                    } else {
                        null
                    },
                    itemHint = ownerTypeLabel,
                    showChevron = ownerKind != null,
                    onClick = if (detail != null && ownerKind != null) {
                        {
                            openEntityProfile(UniverseEntityRef(ownerKind, detail.ownerId))
                        }
                    } else {
                        null
                    },
                ),
                showDivider = false,
            )
        }
        Spacer(modifier = Modifier.height(sectionGap))
        BaseContainer(
            title = stringResource(R.string.character_calendar_section_description),
            useSystemBarsPadding = false,
        ) {
            when {
                detail != null && !descriptionBlank -> {
                    CalendarEventHtmlBody(html = detail.textHtml)
                }
                else -> {
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            showLeadingIcon = false,
                            itemName = when {
                                loading -> placeholder
                                failed && detail == null ->
                                    stringResource(R.string.character_sheet_load_failed)
                                else -> placeholder
                            },
                            showChevron = false,
                            onClick = null,
                        ),
                        showDivider = false,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(sectionGap))
        CalendarAddReminderButton(onClick = onAddReminder)
    }
}

@Composable
private fun CalendarDetailInfoRow(
    label: String,
    value: String,
    showDivider: Boolean,
) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            itemName = label,
            trailingValue = value,
            showChevron = false,
            onClick = null,
        ),
        showDivider = showDivider,
    )
}

@Composable
private fun CalendarAddReminderButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(
        dimensionResource(R.dimen.character_calendar_add_reminder_button_corner),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(
                    R.dimen.character_calendar_add_reminder_button_horizontal_padding,
                ),
            )
            .height(dimensionResource(R.dimen.character_calendar_add_reminder_button_height))
            .clip(shape)
            .background(colorResource(R.color.calendar_add_reminder_button))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.character_calendar_add_reminder),
            color = colorResource(R.color.calendar_add_reminder_button_text),
            fontSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CalendarReminderLeadList(
    onSelect: (CalendarReminderLead) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CalendarReminderLead.entries.forEachIndexed { index, lead ->
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    showLeadingIcon = false,
                    itemName = calendarReminderLeadLabel(lead),
                    onClick = { onSelect(lead) },
                ),
                showDivider = index != CalendarReminderLead.entries.lastIndex,
            )
        }
    }
}

@Composable
private fun CalendarEventHtmlBody(html: String) {
    val textColor = colorResource(R.color.text_primary).toArgb()
    val textSizeSp = dimensionResource(R.dimen.type_detail_body_text_size).value
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.entity_profile_description_padding)),
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
            view.text = calendarEventBodyWithoutLinkStyling(html)
        },
    )
}

private fun CalendarOwnerType.toEntityKind(): UniverseEntityKind? = when (this) {
    CalendarOwnerType.CHARACTER -> UniverseEntityKind.CHARACTER
    CalendarOwnerType.CORPORATION -> UniverseEntityKind.CORPORATION
    CalendarOwnerType.ALLIANCE -> UniverseEntityKind.ALLIANCE
    CalendarOwnerType.FACTION,
    CalendarOwnerType.EVE_SERVER,
    CalendarOwnerType.UNKNOWN,
    -> null
}
