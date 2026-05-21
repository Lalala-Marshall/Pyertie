package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeCompatibleGroupDetail
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
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

    if (!hasMiscSectionContent(attributes, compatibleGroups)) return

    val hasContentBelowCompatible = rows.isNotEmpty()

    BaseContainer(
        title = stringResource(R.string.category_misc),
        useSystemBarsPadding = false,
    ) {
        Column {
            compatibleGroups.forEachIndexed { index, group ->
                val showDivider = index != compatibleGroups.lastIndex || hasContentBelowCompatible
                TypeDetailMiscCompatibleGroupRow(
                    group = group,
                    showDivider = showDivider,
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
    iconManager: IconManager = koinInject(),
) {
    val label = group.attributeDisplayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.type_detail_compatible_with)
    val groupDisplayName = group.groupZhName ?: group.groupEnName ?: group.groupName
        ?: stringResource(R.string.unknown_group)
    val iconFileName = group.groupIconFilename?.takeIf { fileName ->
        iconManager.getIconFile(fileName) != null
    } ?: group.attributeIconFilename?.takeIf { fileName ->
        iconManager.getIconFile(fileName) != null
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconFile = iconFileName?.let { iconManager.getIconFile(it) }
            if (iconFile != null) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = rememberAsyncImagePainter(iconFile),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                modifier = Modifier.weight(1f),
                text = label,
                color = colorResource(R.color.text_primary),
                fontSize = 16.sp,
            )

            Text(
                text = groupDisplayName,
                color = colorResource(R.color.hint_text),
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colorResource(R.color.hint_text),
            )
        }

        if (showDivider) ItemDivider()
    }
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
): Boolean {
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
