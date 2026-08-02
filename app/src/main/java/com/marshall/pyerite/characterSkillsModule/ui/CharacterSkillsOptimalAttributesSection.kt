package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.characterSkillsModule.model.ImplantAttributeBonuses
import com.marshall.pyerite.characterSkillsModule.model.OptimalAttributeAllocation
import com.marshall.pyerite.characterSkillsModule.model.OptimalAttributeAllocator
import com.marshall.pyerite.characterSkillsModule.model.OptimalAttributeKind
import com.marshall.pyerite.characterSkillsModule.model.QueueSkillTrainingNeed
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun CharacterSkillsOptimalAttributesSection(
    status: CharacterSkillQueueStatus,
    catalogGroups: List<SkillCatalogGroup>,
    attributes: CharacterAttributes,
    implantBonuses: ImplantAttributeBonuses,
    attributesReady: Boolean,
    catalogReady: Boolean,
    implantBonusesReady: Boolean,
    nowMs: Long,
    sectionGap: Dp,
) {
    if (!attributesReady || !catalogReady || status.queuedEntries.isEmpty()) {
        return
    }

    val trainingNeeds = remember(status.queuedEntries, catalogGroups, nowMs) {
        buildQueueTrainingNeeds(
            status = status,
            catalogGroups = catalogGroups,
            nowMs = nowMs,
        )
    }
    if (trainingNeeds.isEmpty()) return

    val allocation by produceState<OptimalAttributeAllocation?>(
        null,
        trainingNeeds,
        attributes,
        implantBonuses,
        implantBonusesReady,
    ) {
        value = withContext(Dispatchers.Default) {
            OptimalAttributeAllocator.allocate(
                trainingNeeds = trainingNeeds,
                currentAttributes = attributes,
                implantBonuses = if (implantBonusesReady) {
                    implantBonuses
                } else {
                    ImplantAttributeBonuses.ZERO
                },
            )
        }
    }
    val result = allocation ?: return

    val bonusRows = remember(result) {
        OptimalAttributeKind.entries.mapNotNull { kind ->
            val bonus = result.allocatedBonus(kind)
            if (bonus <= 0) return@mapNotNull null
            OptimalBonusRow(
                kind = kind,
                bonus = bonus,
            )
        }
    }

    val savedTimeText = formatDurationDisplay(
        totalSeconds = result.savedSeconds,
        precision = DurationDisplayFormatter.Precision.HOUR,
        maxUnit = DurationDisplayFormatter.MaxUnit.DAY,
    )
    val positiveColor = colorResource(R.color.character_status_positive)
    val hintColor = colorResource(R.color.hint_text)
    val footerTextSize =
        dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp
    val footerPaddingH = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val footerPaddingV = dimensionResource(R.dimen.detail_row_vertical_padding_multi_line)

    Spacer(modifier = Modifier.height(sectionGap))
    BaseContainer(
        title = stringResource(R.string.character_skills_optimal_attributes_section),
        useSystemBarsPadding = false,
    ) {
        bonusRows.forEach { row ->
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconRes = row.iconRes,
                    itemName = stringResource(row.nameRes),
                    trailingValue = stringResource(
                        R.string.character_skills_optimal_attributes_bonus,
                        row.bonus,
                    ),
                    trailingValueColor = positiveColor,
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = true,
            )
        }
        Text(
            text = if (result.savedSeconds > 0L) {
                stringResource(
                    R.string.character_skills_optimal_attributes_time_saved,
                    savedTimeText,
                )
            } else {
                stringResource(R.string.character_skills_optimal_attributes_already_optimal)
            },
            color = hintColor,
            fontSize = footerTextSize,
            modifier = Modifier.padding(
                horizontal = footerPaddingH,
                vertical = footerPaddingV,
            ),
        )
        if (result.detectedBoosterBonus > 0) {
            Text(
                text = stringResource(R.string.character_skills_optimal_attributes_booster_note),
                color = hintColor,
                fontSize = footerTextSize,
                modifier = Modifier.padding(
                    horizontal = footerPaddingH,
                    vertical = footerPaddingV,
                ),
            )
        }
    }
}

private data class OptimalBonusRow(
    val kind: OptimalAttributeKind,
    val bonus: Int,
) {
    val iconRes: Int
        get() = when (kind) {
            OptimalAttributeKind.PERCEPTION -> R.drawable.ic_attr_perception
            OptimalAttributeKind.MEMORY -> R.drawable.ic_attr_memory
            OptimalAttributeKind.WILLPOWER -> R.drawable.ic_attr_willpower
            OptimalAttributeKind.INTELLIGENCE -> R.drawable.ic_attr_intelligence
            OptimalAttributeKind.CHARISMA -> R.drawable.ic_attr_charisma
        }

    val nameRes: Int
        get() = when (kind) {
            OptimalAttributeKind.PERCEPTION -> R.string.character_skills_attr_perception
            OptimalAttributeKind.MEMORY -> R.string.character_skills_attr_memory
            OptimalAttributeKind.WILLPOWER -> R.string.character_skills_attr_willpower
            OptimalAttributeKind.INTELLIGENCE -> R.string.character_skills_attr_intelligence
            OptimalAttributeKind.CHARISMA -> R.string.character_skills_attr_charisma
        }
}

private fun buildQueueTrainingNeeds(
    status: CharacterSkillQueueStatus,
    catalogGroups: List<SkillCatalogGroup>,
    nowMs: Long,
): List<QueueSkillTrainingNeed> {
    val byTypeId = catalogGroups.asSequence()
        .flatMap { it.skills }
        .associateBy { it.typeId }
    return status.queuedEntries.mapNotNull { entry ->
        val remainingSp = OptimalAttributeAllocator.remainingSpForEntry(entry, nowMs)
            ?: return@mapNotNull null
        if (remainingSp <= 0L) return@mapNotNull null
        val skill = byTypeId[entry.skillId] ?: return@mapNotNull null
        val primary = skill.primaryAttributeTypeId ?: return@mapNotNull null
        val secondary = skill.secondaryAttributeTypeId ?: return@mapNotNull null
        QueueSkillTrainingNeed(
            remainingSp = remainingSp,
            primaryAttributeTypeId = primary,
            secondaryAttributeTypeId = secondary,
        )
    }
}
