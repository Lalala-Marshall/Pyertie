package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
     * Label = this level; solid = actual [SkillCatalogSkill.trainedLevel];
     * sky-blue = levels (trained+1)…this entry’s finished level
     * (e.g. L3 row → first 3 cells sky when untrained).
     * Catalog details must leave this null.
     */
    queueEntryFinishedLevel: Int? = null,
    /**
     * Optional status under the level bar (e.g. plan “completed”); hides duration when set.
     */
    levelFooterText: String? = null,
    levelFooterColor: Color? = null,
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
    val entryFinishedLevel = queueEntryFinishedLevel
        ?.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
    // Plan / queue steps always show the level bar from 0 — never 「未吸收」.
    val showAsInjected = skill.isInjected || inQueue || entryFinishedLevel != null
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
        levelFooterText != null -> null
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
        // One queue/plan step ending at [entryFinishedLevel].
        labelLevel = entryFinishedLevel
        if (skill.trainedLevel >= entryFinishedLevel) {
            // Already finished this step: solid fill through this level (no sky queue tint).
            filledLevel = entryFinishedLevel
            rowQueuedTarget = null
        } else {
            // Solid = already trained; sky-blue = remaining cells through this step.
            filledLevel = skill.trainedLevel.coerceAtMost(entryFinishedLevel - 1)
            rowQueuedTarget = entryFinishedLevel
        }
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
                levelFooterText = levelFooterText,
                levelFooterColor = levelFooterColor,
            )
        },
    )
}
