package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private const val JUMP_DRIVE_CONSUMPTION_TYPE = "jumpDriveConsumptionType"
private const val JUMP_DRIVE_CAPACITOR_NEED = "jumpDriveCapacitorNeed"

internal val miscSectionAttributeOrder = listOf(
    "warpScrambleStatus",
    "techLevel",
    "metaLevelOld",
    "canJump",
    JUMP_DRIVE_CONSUMPTION_TYPE,
    "jumpDriveRange",
    "jumpDriveConsumptionAmount",
    JUMP_DRIVE_CAPACITOR_NEED,
    "maxJumpClones",
    "jumpPortalCapacitorNeed",
    "upgradeCapacity",
    "isCapitalSize",
    "disallowInHighSec",
    "gateScrambleStatus",
    "maxDirectionalScanRange",
)

private val miscPercentFormatter = NumberFormat.getNumberInstance(Locale.US)

@Composable
fun TypeDetailMiscSection(
    attributes: List<TypeAttributeDetail>,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val fuelTypeId = remember(attributes) {
        attributes.find { it.name == JUMP_DRIVE_CONSUMPTION_TYPE }?.value?.toInt()
    }
    val fuelType by remember(fuelTypeId) {
        if (fuelTypeId != null) viewModel.typeDetail(fuelTypeId) else flowOf(null)
    }.collectAsState(initial = null)
    val fuelTypeName = fuelType?.zhName ?: fuelType?.enName ?: fuelType?.name

    val rows = remember(attributes) {
        val byName = attributes
            .filter { it.name != null && it.value != null }
            .associateBy { it.name!! }
        miscSectionAttributeOrder.mapNotNull { name -> byName[name] }
    }

    if (!hasMiscSectionContent(attributes)) return

    BaseContainer(
        title = stringResource(R.string.category_misc),
        useSystemBarsPadding = false,
    ) {
        Column {
            rows.forEachIndexed { index, attr ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = attr.iconFilename,
                        label = attr.displayName ?: attr.name ?: stringResource(R.string.unknown_attribute),
                        value = attr.formatMiscValue(
                            jumpDriveFuelTypeName = if (attr.name == JUMP_DRIVE_CONSUMPTION_TYPE) fuelTypeName else null,
                        ),
                    ),
                    showDivider = index != rows.lastIndex,
                )
            }
        }
    }
}

internal fun hasMiscSectionContent(attributes: List<TypeAttributeDetail>): Boolean {
    val byName = attributes
        .filter { it.name != null && it.value != null }
        .associateBy { it.name!! }
    return miscSectionAttributeOrder.any { name -> byName[name] != null }
}

private fun TypeAttributeDetail.formatMiscValue(jumpDriveFuelTypeName: String?): String =
    when (name) {
        JUMP_DRIVE_CONSUMPTION_TYPE -> jumpDriveFuelTypeName
            ?: formatMappedValue(value, unitName)

        JUMP_DRIVE_CAPACITOR_NEED -> if (unitName == "%" && abs(value ?: 0.0) <= 1.5) {
            "${miscPercentFormatter.format((value ?: 0.0) * 100.0)}%"
        } else {
            formatMappedValue(value, unitName)
        }

        else -> formatMappedValue(value, unitName)
    }
