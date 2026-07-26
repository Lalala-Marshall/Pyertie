package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.util.certificateLevelDrawable
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

internal fun hasSkillLevelAppliesContent(levels: List<Int>): Boolean = levels.isNotEmpty()

@Composable
fun TypeDetailSkillLevelAppliesSection(
    levels: List<Int>,
    onLevelClick: (Int) -> Unit,
) {
    if (!hasSkillLevelAppliesContent(levels)) return

    BaseContainer(
        title = stringResource(R.string.category_skill_level_applies),
        useSystemBarsPadding = false,
    ) {
        Column {
            levels.forEachIndexed { index, level ->
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        iconRes = certificateLevelDrawable(level),
                        itemName = stringResource(R.string.skill_level, level),
                        showChevron = true,
                        onClick = { onLevelClick(level) },
                    ),
                    showDivider = index != levels.lastIndex,
                )
            }
        }
    }
}
