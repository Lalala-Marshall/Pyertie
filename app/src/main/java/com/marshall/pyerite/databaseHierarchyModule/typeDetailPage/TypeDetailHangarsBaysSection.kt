package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.sdeModule.room.dogma.TypeAttributeDetail
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue

@Composable
fun TypeDetailHangarsBaysSection(attributes: List<TypeAttributeDetail>) {
    val rows = remember(attributes) {
        attributes
            .filter { it.displayName != null && it.value != null }
            .sortedBy { it.attributeId }
    }

    if (rows.isEmpty()) return

    BaseContainer(
        title = stringResource(R.string.category_hangars_bays),
        useSystemBarsPadding = false,
    ) {
        Column {
            rows.forEachIndexed { index, attr ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = attr.iconFilename,
                        label = attr.displayName ?: attr.name ?: stringResource(R.string.unknown_attribute),
                        value = formatMappedValue(attr.value, attr.unitName),
                    ),
                    showDivider = index != rows.lastIndex,
                )
            }
        }
    }
}
