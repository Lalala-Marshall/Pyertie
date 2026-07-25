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
fun TypeDetailFittingSection(attributes: List<TypeAttributeDetail>) {
    val fittingRows = remember(attributes) {
        attributes
            .asSequence()
            .filter { it.displayName != null && it.value != null }
            .filter { it.name != FITTING_EXCLUDED_ATTRIBUTE_NAME }
            .sortedBy { it.attributeId }
            .toList()
    }

    if (fittingRows.isEmpty()) return

    BaseContainer(
        title = stringResource(R.string.category_fitting),
        useSystemBarsPadding = false,
    ) {
        Column {
            fittingRows.forEachIndexed { index, attr ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = attr.iconFilename,
                        label = attr.displayName ?: attr.name ?: stringResource(R.string.unknown_attribute),
                        value = formatMappedValue(attr.value, attr.unitName),
                    ),
                    showDivider = index != fittingRows.lastIndex,
                )
            }
        }
    }
}
