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
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillRequirement
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
import org.koin.compose.koinInject

/** SDE skill-slot links (`requiredSkill1` = 主技能需求, etc.); shown via [com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel.skillRequirements]. */
private val SKILL_SLOT_LINK_ATTRIBUTE_REGEX = Regex("^requiredSkill\\d+(Level)?$", RegexOption.IGNORE_CASE)

private fun TypeAttributeDetail.isSkillSlotLinkAttribute(): Boolean =
    name != null && SKILL_SLOT_LINK_ATTRIBUTE_REGEX.matches(name)

internal fun hasSkillsSectionContent(
    skillRequirements: List<SkillRequirement>,
    attributes: List<TypeAttributeDetail>,
): Boolean {
    if (skillRequirements.isNotEmpty()) return true
    return attributes
        .asSequence()
        .filter { !it.isSkillSlotLinkAttribute() }
        .any { it.displayName != null && it.value != null }
}

@Composable
fun TypeDetailSkillsSection(
    skillRequirements: List<SkillRequirement>,
    attributes: List<TypeAttributeDetail>,
    onSkillClick: (Int) -> Unit,
) {
    val dogmaRows = remember(attributes) {
        attributes
            .asSequence()
            .filter { !it.isSkillSlotLinkAttribute() }
            .filter { it.displayName != null && it.value != null }
            .sortedBy { it.attributeId }
            .toList()
    }

    if (!hasSkillsSectionContent(skillRequirements, attributes)) return

    BaseContainer(
        title = stringResource(R.string.category_skills),
        useSystemBarsPadding = false,
    ) {
        Column {
            if (skillRequirements.isNotEmpty()) {
                skillRequirements.forEachIndexed { index, skill ->
                    TypeDetailSkillRequirementRow(
                        skill = skill,
                        showDivider = index != skillRequirements.lastIndex || dogmaRows.isNotEmpty(),
                        onClick = { onSkillClick(skill.typeId) },
                    )
                }
            }
            dogmaRows.forEachIndexed { index, attr ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = attr.iconFilename,
                        label = attr.displayName ?: stringResource(R.string.unknown_attribute),
                        value = formatMappedValue(attr.value, attr.unitName),
                    ),
                    showDivider = index != dogmaRows.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun TypeDetailSkillRequirementRow(
    skill: SkillRequirement,
    showDivider: Boolean,
    onClick: () -> Unit,
    iconManager: IconManager = koinInject(),
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
            val iconFile = skill.iconFilename?.let { iconManager.getIconFile(it) }
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
                text = skill.name,
                color = colorResource(R.color.text_primary),
                fontSize = 16.sp,
            )

            Text(
                text = stringResource(R.string.skill_level, skill.level),
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
