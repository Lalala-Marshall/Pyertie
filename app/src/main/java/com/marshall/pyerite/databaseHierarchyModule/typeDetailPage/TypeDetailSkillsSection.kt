package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillRequirement
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue

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
                    BaseDetailRow(
                        model = BaseDetailRowModel(
                            iconFileName = skill.iconFilename,
                            label = skill.name,
                            value = stringResource(R.string.skill_level, skill.level),
                        ),
                        showDivider = index != skillRequirements.lastIndex || dogmaRows.isNotEmpty(),
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
