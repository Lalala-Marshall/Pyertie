package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.util.formatDurationFromMilliseconds
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue

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

@Composable
private fun TypeAttributeDetail.formatCapacitorValue(): String =
    if (unitName == "s") {
        formatDurationFromMilliseconds(value)
    } else {
        formatMappedValue(value, unitName)
    }
