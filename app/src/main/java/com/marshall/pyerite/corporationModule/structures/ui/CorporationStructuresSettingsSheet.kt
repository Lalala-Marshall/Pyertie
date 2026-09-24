package com.marshall.pyerite.corporationModule.structures.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureFuelMonitor
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

internal enum class CorporationStructuresSheetPage {
    SETTINGS,
    FUEL_MONITOR,
}

@Composable
internal fun CorporationStructuresSettingsSheet(
    page: CorporationStructuresSheetPage,
    fuelMonitor: CorporationStructureFuelMonitor,
    onOpenFuelMonitor: () -> Unit,
    onBack: () -> Unit,
    onSelectFuelMonitor: (CorporationStructureFuelMonitor) -> Unit,
    onDismiss: () -> Unit,
) {
    val title = when (page) {
        CorporationStructuresSheetPage.SETTINGS ->
            stringResource(R.string.corporation_structures_settings)
        CorporationStructuresSheetPage.FUEL_MONITOR ->
            stringResource(R.string.corporation_structures_monitor_time)
    }
    val startLabel = if (page == CorporationStructuresSheetPage.FUEL_MONITOR) {
        stringResource(R.string.corporation_structures_sheet_back)
    } else {
        null
    }
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    CorporationStructuresModalSheet(
        title = title,
        onDismiss = onDismiss,
        startLabel = startLabel,
        onStart = if (page == CorporationStructuresSheetPage.FUEL_MONITOR) onBack else null,
        endLabel = stringResource(R.string.corporation_structures_sheet_done),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            BaseContainer(
                useSystemBarsPadding = false,
                modifier = Modifier.padding(bottom = bottomPadding),
            ) {
            when (page) {
                CorporationStructuresSheetPage.SETTINGS -> {
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            showLeadingIcon = false,
                            itemName = stringResource(R.string.corporation_structures_monitor_time),
                            trailingValue = stringResource(fuelMonitor.labelRes()),
                            onClick = onOpenFuelMonitor,
                        ),
                        showDivider = false,
                    )
                }
                CorporationStructuresSheetPage.FUEL_MONITOR -> {
                    val options = CorporationStructureFuelMonitor.entries
                    options.forEachIndexed { index, option ->
                        FuelMonitorOptionRow(
                            option = option,
                            selected = option == fuelMonitor,
                            showDivider = index < options.lastIndex,
                            onClick = { onSelectFuelMonitor(option) },
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun FuelMonitorOptionRow(
    option: CorporationStructureFuelMonitor,
    selected: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            itemName = stringResource(option.labelRes()),
            showChevron = false,
            onClick = onClick,
        ),
        showDivider = showDivider,
        trailingContent = { FuelMonitorSelectedIcon(selected = selected) },
    )
}

@Composable
private fun FuelMonitorSelectedIcon(selected: Boolean) {
    val checkSize = dimensionResource(R.dimen.detail_row_chevron_size)
    Box(
        modifier = Modifier.size(checkSize),
        contentAlignment = Alignment.Center,
    ) {
        if (!selected) return@Box
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = stringResource(R.string.corporation_structures_monitor_selected),
            tint = colorResource(R.color.hyperlink_text),
            modifier = Modifier.size(checkSize),
        )
    }
}
