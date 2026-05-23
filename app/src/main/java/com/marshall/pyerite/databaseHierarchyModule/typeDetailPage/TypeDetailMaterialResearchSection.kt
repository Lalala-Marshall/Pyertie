package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.util.formatDurationFromSeconds
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuValueRow
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuValueRowModel

private val MaterialResearchExpandAnimation = expandVertically(expandFrom = Alignment.Top)
private val MaterialResearchCollapseAnimation = shrinkVertically(shrinkTowards = Alignment.Top)

@SuppressLint("LocalContextResourcesRead")
@Composable
fun TypeDetailMaterialResearchSection(
    typeId: Int,
    researchMaterialTimeSeconds: Int?,
) {
    val context = LocalContext.current
    val levelTimeModifiers = remember {
        context.resources.getIntArray(R.array.material_research_level_time_modifiers)
    }
    val timeDivisor = integerResource(R.integer.material_research_time_divisor)
    val maxResearchLevel = levelTimeModifiers.lastIndex
    val levelTimes = buildMaterialResearchLevelTimes(
        baseTimeSeconds = researchMaterialTimeSeconds ?: 0,
        levelTimeModifiers = levelTimeModifiers,
        timeDivisor = timeDivisor,
    )
    if (levelTimes.isEmpty()) return

    var researchTimeExpanded by rememberSaveable(typeId) { mutableStateOf(false) }
    val levelCountLabel = stringResource(
        R.string.type_detail_material_research_level_count,
        maxResearchLevel,
    )

    BaseContainer(
        title = stringResource(R.string.type_detail_section_material_research),
        useSystemBarsPadding = false,
    ) {
        Column {
            MaterialResearchExpandableHeaderRow(
                label = stringResource(R.string.type_detail_material_research_time),
                value = levelCountLabel,
                expanded = researchTimeExpanded,
                onClick = { researchTimeExpanded = !researchTimeExpanded },
            )
            AnimatedVisibility(
                visible = researchTimeExpanded,
                enter = MaterialResearchExpandAnimation,
                exit = MaterialResearchCollapseAnimation,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    levelTimes.forEachIndexed { index, levelTime ->
                        BaseSubMenuValueRow(
                            model = BaseSubMenuValueRowModel(
                                label = stringResource(
                                    R.string.type_detail_material_research_level_range,
                                    levelTime.level,
                                ),
                                value = formatDurationFromSeconds(levelTime.cumulativeTimeSeconds),
                            ),
                            showDivider = index != levelTimes.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialResearchExpandableHeaderRow(
    label: String,
    value: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        MaterialResearchPrimaryRowContent(
            label = label,
            value = value,
            trailingIcon = if (expanded) {
                Icons.Filled.KeyboardArrowDown
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            },
            trailingIconTint = colorResource(
                if (expanded) R.color.text_primary else R.color.hint_text,
            ),
        )
    }
}

@Composable
private fun MaterialResearchPrimaryRowContent(
    label: String,
    value: String,
    trailingIcon: ImageVector,
    trailingIconTint: Color,
) {
    val titleValueGap = dimensionResource(R.dimen.detail_row_title_value_gap)
    val rowHorizontalPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val rowVerticalPadding = dimensionResource(R.dimen.detail_row_vertical_padding)
    val chevronSize = dimensionResource(R.dimen.detail_row_chevron_size)
    val trailingGap = dimensionResource(R.dimen.detail_row_trailing_gap)
    val labelTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val labelLineHeight = dimensionResource(R.dimen.sub_menu_label_line_height).value.sp
    val valueTextSize = dimensionResource(R.dimen.sub_menu_value_text_size).value.sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = rowHorizontalPadding, vertical = rowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            color = colorResource(R.color.text_primary),
            fontSize = labelTextSize,
            lineHeight = labelLineHeight,
        )

        Spacer(modifier = Modifier.width(titleValueGap))

        Text(
            text = value,
            color = colorResource(R.color.hint_text),
            fontSize = valueTextSize,
        )

        Spacer(modifier = Modifier.width(trailingGap))

        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            modifier = Modifier.size(chevronSize),
            tint = trailingIconTint,
        )
    }
}
