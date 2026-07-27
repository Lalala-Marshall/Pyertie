package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.util.formatDurationFromSeconds
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
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
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    showLeadingIcon = false,
                    itemName = stringResource(researchTimeLabelRes),
                    trailingValue = levelCountLabel,
                    showChevron = true,
                    chevronExpanded = researchTimeExpanded,
                    onClick = { researchTimeExpanded = !researchTimeExpanded },
                ),
                showDivider = false,
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
