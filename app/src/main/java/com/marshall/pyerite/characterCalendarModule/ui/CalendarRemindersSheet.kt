package com.marshall.pyerite.characterCalendarModule.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.marshall.pyerite.R
import com.marshall.pyerite.characterCalendarModule.model.CalendarReminder
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

@Composable
internal fun CalendarRemindersSheet(
    reminders: List<CalendarReminder>,
    onRemove: (CalendarReminder) -> Unit,
    onDismiss: () -> Unit,
) {
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    CalendarBottomSheet(
        title = stringResource(R.string.character_calendar_reminders),
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
                if (reminders.isEmpty()) {
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            showLeadingIcon = false,
                            itemName = stringResource(R.string.character_calendar_reminders_empty),
                            showChevron = false,
                            onClick = null,
                        ),
                        showDivider = false,
                    )
                } else {
                    val removeLabel = stringResource(R.string.character_calendar_reminder_remove)
                    val trailingIconSize = dimensionResource(R.dimen.detail_row_chevron_size)
                    Column {
                        reminders.forEachIndexed { index, reminder ->
                            BaseLazyColumnItem(
                                model = BaseLazyColumnItemModel(
                                    showLeadingIcon = false,
                                    itemName = reminder.eventTitle.ifBlank { placeholder },
                                    itemNameBold = true,
                                    itemHint = stringResource(
                                        R.string.character_calendar_event_hint,
                                        calendarReminderLeadLabel(reminder.lead),
                                        formatCalendarDateTime(reminder.fireAtEpochMs),
                                    ),
                                    showChevron = false,
                                    onClick = null,
                                ),
                                showDivider = index != reminders.lastIndex,
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = removeLabel,
                                        tint = colorResource(R.color.character_delete),
                                        modifier = Modifier
                                            .size(trailingIconSize)
                                            .clickable(
                                                interactionSource = remember {
                                                    MutableInteractionSource()
                                                },
                                                indication = null,
                                                onClick = { onRemove(reminder) },
                                            )
                                            .semantics { role = Role.Button },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
