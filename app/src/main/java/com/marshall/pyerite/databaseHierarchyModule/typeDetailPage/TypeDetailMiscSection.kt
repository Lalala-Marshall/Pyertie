package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.sdeModule.room.dogma.TypeAttributeDetail
import com.marshall.pyerite.sdeModule.room.type.TypeCompatibleGroupDetail
import com.marshall.pyerite.sdeModule.room.type.TypeSkillMiscRow
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.iconModule.manager.IconManager
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
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
    compatibleGroups: List<TypeCompatibleGroupDetail> = emptyList(),
    skillMiscRows: List<TypeSkillMiscRow> = emptyList(),
    viewModel: DatabaseViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val fuelTypeId = remember(attributes) {
        attributes.find { it.name == JUMP_DRIVE_CONSUMPTION_TYPE }?.value?.toInt()
    }
    val fuelType by remember(fuelTypeId) {
        if (fuelTypeId != null) viewModel.typeDetail(fuelTypeId) else flowOf(null)
    }.collectAsState(initial = null)
    val fuelTypeName = fuelType?.displayName(localeController)?.takeIf { it.isNotBlank() }

    val rows = remember(attributes) {
        val byName = attributes
            .filter { it.name != null && it.value != null }
            .associateBy { it.name!! }
        miscSectionAttributeOrder.mapNotNull { name -> byName[name] }
    }

    if (!hasMiscSectionContent(attributes, compatibleGroups, skillMiscRows)) return

    val hasContentBelowSkillMisc = compatibleGroups.isNotEmpty() || rows.isNotEmpty()
    val hasContentBelowCompatible = rows.isNotEmpty()

    BaseContainer(
        title = stringResource(R.string.category_misc),
        useSystemBarsPadding = false,
    ) {
        Column {
            skillMiscRows.forEachIndexed { index, row ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = row.iconFilename,
                        label = row.label,
                        value = row.value,
                    ),
                    showDivider = index != skillMiscRows.lastIndex || hasContentBelowSkillMisc,
                )
            }

            compatibleGroups.forEachIndexed { index, group ->
                val showDivider = index != compatibleGroups.lastIndex || hasContentBelowCompatible
                TypeDetailMiscCompatibleGroupRow(
                    group = group,
                    showDivider = showDivider,
                    localeController = localeController,
                )
            }

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

@Composable
private fun TypeDetailMiscCompatibleGroupRow(
    group: TypeCompatibleGroupDetail,
    showDivider: Boolean,
    localeController: LocaleController,
    iconManager: IconManager = koinInject(),
) {
    val label = group.attributeDisplayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.type_detail_compatible_with)
    val groupDisplayName = group.displayName(localeController).ifBlank {
        stringResource(R.string.unknown_group)
    }
    val iconFileName = group.groupIconFilename?.takeIf { fileName ->
        iconManager.getIconFile(fileName) != null
    } ?: group.attributeIconFilename?.takeIf { fileName ->
        iconManager.getIconFile(fileName) != null
    }

    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconFileName = iconFileName,
            showLeadingIcon = iconFileName != null,
            itemName = label,
            trailingValue = groupDisplayName,
            showChevron = true,
            onClick = null,
        ),
        showDivider = showDivider,
    )
}

internal fun hasCompatibleGroupAttributes(attributes: List<TypeAttributeDetail>): Boolean =
    attributes.any { attr ->
        val name = attr.name ?: return@any false
        (name.startsWith("chargeGroup") || name.startsWith("launcherGroup")) &&
            (attr.value ?: 0.0) > 0
    }

internal fun hasMiscSectionContent(
    attributes: List<TypeAttributeDetail>,
    compatibleGroups: List<TypeCompatibleGroupDetail> = emptyList(),
    skillMiscRows: List<TypeSkillMiscRow> = emptyList(),
): Boolean {
    if (skillMiscRows.isNotEmpty()) return true
    if (compatibleGroups.isNotEmpty() || hasCompatibleGroupAttributes(attributes)) return true
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
