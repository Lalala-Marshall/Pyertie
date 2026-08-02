package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.NumberDisplayFormatter

@Composable
internal fun SkillCatalogSkillRow(
    skill: SkillCatalogSkill,
    queuedTargetLevel: Int?,
    localeController: LocaleController,
    highlightQueueTarget: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    attributes: CharacterAttributes,
    attributesReady: Boolean,
    activeTrainingSkillId: Int? = null,
    activeTrainingLevel: Int? = null,
    /** When set (queue head live tick), replaces catalog [SkillCatalogSkill.trainedSp]. */
    trainedSpOverride: Long? = null,
    /**
     * When true, trailing duration uses [queueRemainingSeconds] only (no attribute estimate).
     * Used for skills-page queue rows.
     */
    useQueueRemaining: Boolean = false,
    /** Remaining seconds for the queue entry; null hides the duration line. */
    queueRemainingSeconds: Long? = null,
    remainingDurationPrecision: DurationDisplayFormatter.Precision =
        DurationDisplayFormatter.Precision.MINUTE,
    remainingDurationMaxUnit: DurationDisplayFormatter.MaxUnit =
        DurationDisplayFormatter.MaxUnit.YEAR,
    showLeadingIcon: Boolean = true,
    /**
     * Skills-page queue entry: ESI `finished_level` for **this** row.
     * Label = this level; filled segments = level − 1; gray = this level only.
     * Catalog details must leave this null.
     */
    queueEntryFinishedLevel: Int? = null,
    /**
     * Queue-head row: omit bottom content padding so [belowContent] sits flush
     * under the SP hint.
     */
    omitContentBottomPadding: Boolean = false,
    /** Inside the clickable item, under the main row (e.g. training progress). */
    belowContent: (@Composable () -> Unit)? = null,
) {
    val trainedSpText = NumberDisplayFormatter.format(
        trainedSpOverride ?: skill.trainedSp,
        NumberDisplayFormatter.Style.FULL,
    )
    val maxSpText = NumberDisplayFormatter.format(
        skill.maxSp,
        NumberDisplayFormatter.Style.FULL,
    )
    val inQueue = highlightQueueTarget && (
        queuedTargetLevel != null || queueEntryFinishedLevel != null
    )
    val showAsInjected = skill.isInjected || inQueue
    val entryFinishedLevel = queueEntryFinishedLevel
        ?.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
    val blinkingLevel = when {
        entryFinishedLevel != null &&
            activeTrainingSkillId == skill.typeId &&
            activeTrainingLevel == entryFinishedLevel -> entryFinishedLevel
        entryFinishedLevel == null &&
            activeTrainingSkillId == skill.typeId &&
            activeTrainingLevel != null -> activeTrainingLevel
        else -> null
    }
    val trailingSeconds = when {
        useQueueRemaining -> queueRemainingSeconds
        attributesReady -> skill.secondsToTrainNextLevel(attributes)
        else -> null
    }
    val rank = skill.skillTimeConstant.toInt().coerceAtLeast(1)
    val title = stringResource(
        R.string.character_skills_catalog_skill_title_with_rank,
        skill.displayName(localeController),
        rank,
    )
    val leadingIconVisible = showLeadingIcon && !skill.iconFilename.isNullOrBlank()
    val filledLevel: Int
    val labelLevel: Int?
    val rowQueuedTarget: Int?
    if (entryFinishedLevel != null) {
        // This queue entry trains toward [entryFinishedLevel]: prior levels filled, target gray.
        filledLevel = entryFinishedLevel - 1
        labelLevel = entryFinishedLevel
        rowQueuedTarget = entryFinishedLevel
    } else {
        filledLevel = skill.trainedLevel
        labelLevel = null
        rowQueuedTarget = if (inQueue) queuedTargetLevel else null
    }

    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconFileName = skill.iconFilename,
            showLeadingIcon = leadingIconVisible,
            itemName = title,
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
        omitContentBottomPadding = omitContentBottomPadding,
        belowContent = belowContent,
        trailingContent = {
            SkillCatalogLevelTrailing(
                isInjected = showAsInjected,
                level = filledLevel,
                levelLabel = labelLevel,
                queuedTargetLevel = rowQueuedTarget,
                blinkingLevel = blinkingLevel,
                nextLevelTrainingSeconds = trailingSeconds,
                durationPrecision = remainingDurationPrecision,
                durationMaxUnit = remainingDurationMaxUnit,
            )
        },
    )
}
