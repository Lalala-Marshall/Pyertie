package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.annotation.ColorRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
import org.koin.compose.koinInject
import kotlin.math.roundToInt

private data class DefenseResistanceRow(
    val attr: TypeAttributeDetail,
    val type: DefenseResistanceType,
)

private enum class DefenseResistanceType(
    private val dogmaToken: String,
    @param:ColorRes val activeColorRes: Int,
    @param:ColorRes val inactiveColorRes: Int,
) {
    EM(
        dogmaToken = "Em",
        activeColorRes = R.color.resistance_em_active,
        inactiveColorRes = R.color.resistance_em_inactive,
    ),
    THERMAL(
        dogmaToken = "Thermal",
        activeColorRes = R.color.resistance_thermal_active,
        inactiveColorRes = R.color.resistance_thermal_inactive,
    ),
    KINETIC(
        dogmaToken = "Kinetic",
        activeColorRes = R.color.resistance_kinetic_active,
        inactiveColorRes = R.color.resistance_kinetic_inactive,
    ),
    EXPLOSIVE(
        dogmaToken = "Explosive",
        activeColorRes = R.color.resistance_explosive_active,
        inactiveColorRes = R.color.resistance_explosive_inactive,
    );

    companion object {
        private const val DOGMA_RESONANCE_SUFFIX = "DamageResonance"
        private const val PERCENT_UNIT = "%"

        fun from(attr: TypeAttributeDetail): DefenseResistanceType? {
            val name = attr.name ?: return null
            if (!name.endsWith(DOGMA_RESONANCE_SUFFIX) || attr.unitName != PERCENT_UNIT) return null
            return entries.firstOrNull { name.contains(it.dogmaToken, ignoreCase = true) }
        }
    }
}

@Composable
fun TypeDetailShieldSection(
    attributes: List<TypeAttributeDetail>,
    iconManager: IconManager = koinInject(),
) {
    TypeDetailDefenseLayerSection(
        title = stringResource(R.string.category_shield),
        attributes = attributes,
        formatSecondsAsDuration = true,
        iconManager = iconManager,
    )
}

@Composable
fun TypeDetailArmorSection(
    attributes: List<TypeAttributeDetail>,
    iconManager: IconManager = koinInject(),
) {
    TypeDetailDefenseLayerSection(
        title = stringResource(R.string.category_armor),
        attributes = attributes,
        formatSecondsAsDuration = false,
        iconManager = iconManager,
    )
}

@Composable
fun TypeDetailStructureSection(
    attributes: List<TypeAttributeDetail>,
    iconManager: IconManager = koinInject(),
) {
    TypeDetailDefenseLayerSection(
        title = stringResource(R.string.category_structure),
        attributes = attributes,
        formatSecondsAsDuration = false,
        iconManager = iconManager,
    )
}

internal fun hasDefenseSectionContent(attributes: List<TypeAttributeDetail>): Boolean {
    val visibleAttributes = attributes.filter { it.displayName != null && it.value != null }
    if (visibleAttributes.isEmpty()) return false
    val resistanceIds = visibleAttributes
        .mapNotNull { attr -> DefenseResistanceType.from(attr)?.let { attr.attributeId } }
        .toSet()
    return resistanceIds.isNotEmpty() || visibleAttributes.any { it.attributeId !in resistanceIds }
}

@Composable
private fun TypeDetailDefenseLayerSection(
    title: String,
    attributes: List<TypeAttributeDetail>,
    formatSecondsAsDuration: Boolean,
    iconManager: IconManager,
) {
    val visibleAttributes = remember(attributes) {
        attributes.filter { it.displayName != null && it.value != null }
    }
    val resistances = remember(visibleAttributes) {
        visibleAttributes
            .mapNotNull { attr ->
                DefenseResistanceType.from(attr)?.let { type ->
                    DefenseResistanceRow(attr = attr, type = type)
                }
            }
            .sortedBy { it.type.ordinal }
    }
    val detailRows = remember(visibleAttributes, resistances) {
        val resistanceIds = resistances.map { it.attr.attributeId }.toSet()
        visibleAttributes
            .filter { it.attributeId !in resistanceIds }
            .sortedBy { it.attributeId }
    }

    if (!hasDefenseSectionContent(attributes)) return

    val rowHorizontalPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val rowVerticalPadding = dimensionResource(R.dimen.detail_row_vertical_padding)
    val resistanceExtraPadding = dimensionResource(R.dimen.defense_row_extra_horizontal_padding)

    BaseContainer(
        title = title,
        useSystemBarsPadding = false,
    ) {
        Column {
            if (resistances.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = rowHorizontalPadding, vertical = rowVerticalPadding),
                ) {
                    resistances.forEach { resistance ->
                        ResistanceBar(
                            row = resistance,
                            iconManager = iconManager,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = resistanceExtraPadding),
                        )
                    }
                }

                if (detailRows.isNotEmpty()) {
                    ItemDivider()
                }
            }

            detailRows.forEachIndexed { index, attr ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = attr.iconFilename,
                        label = attr.displayName ?: attr.name ?: stringResource(R.string.unknown_attribute),
                        value = attr.formatDefenseDetailValue(formatSecondsAsDuration),
                    ),
                    showDivider = index != detailRows.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun ResistanceBar(
    row: DefenseResistanceRow,
    iconManager: IconManager,
    modifier: Modifier = Modifier,
) {
    val attr = row.attr
    val activeColor = colorResource(row.type.activeColorRes)
    val inactiveColor = colorResource(row.type.inactiveColorRes)
    val percent = ((1 - (attr.value ?: 0.0)) * 100.0).roundToInt().coerceIn(0, 100)
    val brightWeight = percent.coerceAtLeast(0)
    val dimWeight = (100 - percent).coerceAtLeast(0)
    val statIconSize = dimensionResource(R.dimen.defense_stat_icon_size)
    val statLabelGap = dimensionResource(R.dimen.defense_stat_label_gap)
    val barGroupGap = dimensionResource(R.dimen.defense_bar_group_gap)
    val barHeight = dimensionResource(R.dimen.defense_bar_height)
    val barCornerRadius = dimensionResource(R.dimen.defense_bar_corner_radius)
    val valueTextSize = dimensionResource(R.dimen.sub_menu_value_text_size).value.sp

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            attr.iconFilename?.let { iconFileName ->
                Icon(
                    painter = rememberAsyncImagePainter(iconManager.getIconFile(iconFileName)),
                    contentDescription = null,
                    modifier = Modifier.size(statIconSize),
                    tint = Color.Unspecified,
                )
            }
            Text(
                text = "$percent%",
                fontSize = valueTextSize,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(start = statLabelGap),
            )
        }

        Spacer(modifier = Modifier.height(barGroupGap))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(barCornerRadius)),
        ) {
            if (brightWeight > 0) {
                Spacer(
                    modifier = Modifier
                        .weight(brightWeight.toFloat())
                        .height(barHeight)
                        .background(activeColor),
                )
            }
            if (dimWeight > 0) {
                Spacer(
                    modifier = Modifier
                        .weight(dimWeight.toFloat())
                        .height(barHeight)
                        .background(inactiveColor),
                )
            }
        }
    }
}

private fun TypeAttributeDetail.formatDefenseDetailValue(formatSecondsAsDuration: Boolean): String =
    if (formatSecondsAsDuration && unitName == "s") {
        formatShieldRechargeTime(value)
    } else {
        formatMappedValue(value, unitName)
    }

private fun formatShieldRechargeTime(rawValue: Double?): String {
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
