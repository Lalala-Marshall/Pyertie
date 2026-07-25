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
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private const val CARGO_SCAN_RESISTANCE_NAME = "cargoScanResistance"

private val ewarPercentFormatter = NumberFormat.getNumberInstance(Locale.US)

@Composable
fun TypeDetailEwarResistancesSection(attributes: List<TypeAttributeDetail>) {
    val rows = remember(attributes) {
        attributes
            .filter { it.displayName != null && it.value != null }
            .sortedBy { it.attributeId }
    }

    if (rows.isEmpty()) return

    BaseContainer(
        title = stringResource(R.string.category_ewar),
        useSystemBarsPadding = false,
    ) {
        Column {
            rows.forEachIndexed { index, attr ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = attr.iconFilename,
                        label = attr.displayName ?: attr.name ?: stringResource(R.string.unknown_attribute),
                        value = attr.formatEwarResistanceValue(),
                    ),
                    showDivider = index != rows.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun TypeAttributeDetail.formatEwarResistanceValue(): String =
    when (name) {
        CARGO_SCAN_RESISTANCE_NAME -> {
            if ((value ?: 0.0) > 0.0) {
                stringResource(R.string.ewar_cargo_scan_immune)
            } else {
                stringResource(R.string.ewar_cargo_scan_not_immune)
            }
        }

        else -> when {
            unitName == "%" && isEwarFractionPercent(value) ->
                "${ewarPercentFormatter.format((1.0 - (value ?: 0.0)) * 100.0)}%"

            else -> formatMappedValue(value, unitName)
        }
    }

/** SDE stores susceptibility as 0..1; UI shows resistance as (1 − x). Bonus attrs use raw points (e.g. -28). */
private fun isEwarFractionPercent(value: Double?): Boolean {
    val v = value ?: 0.0
    return abs(v) <= 1.5
}
