package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.util.NumberDisplayFormatter

@Composable
internal fun SkillCatalogSkillRow(
    skill: SkillCatalogSkill,
    queuedTargetLevel: Int?,
    localeController: LocaleController,
    highlightQueueTarget: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    activeTrainingSkillId: Int? = null,
    activeTrainingLevel: Int? = null,
) {
    val trainedSpText = NumberDisplayFormatter.format(
        skill.trainedSp,
        NumberDisplayFormatter.Style.COMPACT,
    )
    val maxSpText = NumberDisplayFormatter.format(
        skill.maxSp,
        NumberDisplayFormatter.Style.COMPACT,
    )
    val inQueue = highlightQueueTarget && queuedTargetLevel != null
    val showAsInjected = skill.isInjected || inQueue
    val blinkingLevel = if (
        activeTrainingSkillId == skill.typeId &&
        activeTrainingLevel != null
    ) {
        activeTrainingLevel
    } else {
        null
    }

    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconFileName = skill.iconFilename,
            showLeadingIcon = !skill.iconFilename.isNullOrBlank(),
            itemName = skill.displayName(localeController),
            itemHints = listOf(
                BaseLazyColumnItemHint(
                    text = stringResource(
                        R.string.character_skills_catalog_skill_sp_hint,
                        trainedSpText,
                        maxSpText,
                    ),
                ),
            ),
            showChevron = true,
            onClick = onClick,
        ),
        showDivider = showDivider,
        trailingContent = {
            SkillCatalogLevelTrailing(
                isInjected = showAsInjected,
                level = skill.trainedLevel,
                queuedTargetLevel = if (inQueue) queuedTargetLevel else null,
                blinkingLevel = blinkingLevel,
            )
        },
    )
}
