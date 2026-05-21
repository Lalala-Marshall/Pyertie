package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.util.certificateLevelDrawable
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.ItemDivider

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
                SkillLevelAppliesRow(
                    level = level,
                    showDivider = index != levels.lastIndex,
                    onClick = { onLevelClick(level) },
                )
            }
        }
    }
}

@Composable
private fun SkillLevelAppliesRow(
    level: Int,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(certificateLevelDrawable(level)),
                contentDescription = null,
                tint = Color.Unspecified,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.skill_level, level),
                color = colorResource(R.color.text_primary),
                fontSize = 16.sp,
            )

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
