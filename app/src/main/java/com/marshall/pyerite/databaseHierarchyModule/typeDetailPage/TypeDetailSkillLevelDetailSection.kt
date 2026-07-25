package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.sdeModule.room.skill.SkillLevelSpRow
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.util.NumberDisplayFormatter

@Composable
fun TypeDetailSkillLevelDetailSection(rows: List<SkillLevelSpRow>) {
    if (rows.isEmpty()) return

    BaseContainer(
        title = stringResource(R.string.category_skill_level_detail),
        useSystemBarsPadding = false,
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        label = stringResource(
                            R.string.skill_sp_amount,
                            NumberDisplayFormatter.format(
                                row.spTotal,
                                NumberDisplayFormatter.Style.FULL,
                            ),
                        ),
                        value = stringResource(R.string.skill_sp_level_range, row.targetLevel),
                    ),
                    showDivider = index != rows.lastIndex,
                )
            }
        }
    }
}

internal fun hasSkillLevelDetailContent(rows: List<SkillLevelSpRow>): Boolean = rows.isNotEmpty()
