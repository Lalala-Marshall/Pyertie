package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue
import kotlin.math.roundToInt

@Composable
fun TypeDetailCapacitorSection(attributes: List<TypeAttributeDetail>) {
    val rows = remember(attributes) {
        attributes
            .filter { it.displayName != null && it.value != null }
            .sortedBy { it.attributeId }
    }

    if (rows.isEmpty()) return

    BaseContainer(
        title = stringResource(R.string.category_capacitor),
        useSystemBarsPadding = false,
    ) {
        Column {
            rows.forEachIndexed { index, attr ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = attr.iconFilename,
                        label = attr.displayName ?: attr.name ?: stringResource(R.string.unknown_attribute),
                        value = attr.formatCapacitorValue(),
                    ),
                    showDivider = index != rows.lastIndex,
                )
            }
        }
    }
}

private fun TypeAttributeDetail.formatCapacitorValue(): String =
    if (unitName == "s") {
        formatDurationFromMilliseconds(value)
    } else {
        formatMappedValue(value, unitName)
    }

private fun formatDurationFromMilliseconds(rawValue: Double?): String {
    var totalSeconds = ((rawValue ?: 0.0) / 1000.0).roundToInt().coerceAtLeast(0)
    val hours = totalSeconds / 3600
    totalSeconds %= 3600
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}
