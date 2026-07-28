package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
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
) {
    val queueTextColor = colorResource(R.color.character_skills_catalog_queue_text)
    val trainedSpText = NumberDisplayFormatter.format(
        skill.trainedSp,
        NumberDisplayFormatter.Style.COMPACT,
    )
    val maxSpText = NumberDisplayFormatter.format(
        skill.maxSp,
        NumberDisplayFormatter.Style.COMPACT,
    )
    val trailingLevel = when {
        highlightQueueTarget && queuedTargetLevel != null -> queuedTargetLevel
        skill.isInjected -> skill.trainedLevel
        else -> null
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
            trailingValue = trailingLevel?.let {
                stringResource(R.string.skill_level, it)
            }.orEmpty(),
            trailingValueColor = if (highlightQueueTarget && queuedTargetLevel != null) {
                queueTextColor
            } else {
                null
            },
            showChevron = true,
            onClick = onClick,
        ),
        showDivider = showDivider,
    )
}
