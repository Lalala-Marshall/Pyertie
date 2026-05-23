package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import android.annotation.SuppressLint
import androidx.annotation.StringRes
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

private val BlueprintResearchExpandAnimation = expandVertically(expandFrom = Alignment.Top)
private val BlueprintResearchCollapseAnimation = shrinkVertically(shrinkTowards = Alignment.Top)

@SuppressLint("LocalContextResourcesRead")
@Composable
fun TypeDetailBlueprintResearchSection(
    typeId: Int,
    baseTimeSeconds: Int?,
    @StringRes sectionTitleRes: Int,
    @StringRes researchTimeLabelRes: Int,
    researchLevelStep: Int = 1,
) {
    val context = LocalContext.current
    val levelTimeModifiers = remember {
        context.resources.getIntArray(R.array.blueprint_research_level_time_modifiers)
    }
    val timeDivisor = integerResource(R.integer.blueprint_research_time_divisor)
    val maxResearchLevel = levelTimeModifiers.lastIndex
    val levelTimes = buildBlueprintResearchLevelTimes(
        baseTimeSeconds = baseTimeSeconds ?: 0,
        levelTimeModifiers = levelTimeModifiers,
        timeDivisor = timeDivisor,
    )
    if (levelTimes.isEmpty()) return

    var researchTimeExpanded by rememberSaveable(typeId, sectionTitleRes) { mutableStateOf(false) }
    val maxDisplayLevel = maxResearchLevel * researchLevelStep
    val levelCountLabel = stringResource(
        R.string.type_detail_blueprint_research_level_count,
        maxDisplayLevel,
    )

    BaseContainer(
        title = stringResource(sectionTitleRes),
        useSystemBarsPadding = false,
    ) {
        Column {
            BlueprintResearchExpandableHeaderRow(
                label = stringResource(researchTimeLabelRes),
                value = levelCountLabel,
                expanded = researchTimeExpanded,
                onClick = { researchTimeExpanded = !researchTimeExpanded },
            )
            AnimatedVisibility(
                visible = researchTimeExpanded,
                enter = BlueprintResearchExpandAnimation,
                exit = BlueprintResearchCollapseAnimation,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    levelTimes.forEachIndexed { index, levelTime ->
                        val displayLevel = levelTime.level * researchLevelStep
                        BaseSubMenuValueRow(
                            model = BaseSubMenuValueRowModel(
                                label = stringResource(
                                    R.string.skill_level,
                                    displayLevel,
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
private fun BlueprintResearchExpandableHeaderRow(
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
        BlueprintResearchPrimaryRowContent(
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
private fun BlueprintResearchPrimaryRowContent(
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
