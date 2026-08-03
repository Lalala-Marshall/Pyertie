package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay

/**
 * Compact skill-level trailing: short level label + 5-segment bar, or 「未吸收」 when not injected.
 * Below that: optional [levelFooterText], else time to finish the next level when not maxed.
 *
 * Queued levels above [level] up to [queuedTargetLevel] render gray.
 * Only [blinkingLevel] (the actively training level) pulses — animation runs solely for that row.
 *
 * @param level Filled segment count (completed levels).
 * @param levelLabel Optional "Lv.N" text when different from [level]
 *   (skills-page queue: label = entry `finished_level`, filled = that − 1).
 * @param levelFooterText Optional status under the bar (e.g. plan entry completed);
 *   when set, hides the duration line.
 */
@Composable
internal fun SkillCatalogLevelTrailing(
    isInjected: Boolean,
    level: Int,
    modifier: Modifier = Modifier,
    levelLabel: Int? = null,
    queuedTargetLevel: Int? = null,
    blinkingLevel: Int? = null,
    nextLevelTrainingSeconds: Long? = null,
    durationPrecision: DurationDisplayFormatter.Precision =
        DurationDisplayFormatter.Precision.MINUTE,
    durationMaxUnit: DurationDisplayFormatter.MaxUnit =
        DurationDisplayFormatter.MaxUnit.YEAR,
    levelFooterText: String? = null,
    levelFooterColor: Color? = null,
) {
    val primary = colorResource(R.color.text_primary)
    val hintColor = colorResource(R.color.hint_text)
    val labelSize = dimensionResource(R.dimen.sub_menu_value_text_size).value.sp
    val durationSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp
    val labelGap = dimensionResource(R.dimen.skill_level_segments_label_gap)
    val durationGap = dimensionResource(R.dimen.skill_level_segments_duration_gap)
    val durationText = if (levelFooterText == null) {
        nextLevelTrainingSeconds?.let { seconds ->
            formatDurationDisplay(
                totalSeconds = seconds,
                precision = durationPrecision,
                maxUnit = durationMaxUnit,
            )
        }
    } else {
        null
    }
    val footerColor = levelFooterColor ?: hintColor

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(durationGap),
    ) {
        if (!isInjected) {
            Text(
                text = stringResource(R.string.character_skills_catalog_filter_untrained),
                color = hintColor,
                fontSize = labelSize,
                maxLines = 1,
            )
        } else {
            val filledLevel = level.coerceIn(0, SkillCatalogConfig.MAX_SKILL_LEVEL)
            val displayLabel = (levelLabel ?: level)
                .coerceIn(0, SkillCatalogConfig.MAX_SKILL_LEVEL)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.skill_level_short, displayLabel),
                    color = primary,
                    fontSize = labelSize,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(labelGap))
                SkillLevelSegments(
                    trainedLevel = filledLevel,
                    queuedTargetLevel = queuedTargetLevel,
                    blinkingLevel = blinkingLevel,
                    activeColor = primary,
                    queueSegmentColor = colorResource(R.color.skill_level_queue_segment),
                )
            }
        }
        if (levelFooterText != null) {
            Text(
                text = levelFooterText,
                color = footerColor,
                fontSize = durationSize,
                maxLines = 1,
            )
        } else if (durationText != null) {
            Text(
                text = durationText,
                color = hintColor,
                fontSize = durationSize,
                maxLines = 1,
            )
        }
    }
}

/** Rectangular frame with [SkillCatalogConfig.MAX_SKILL_LEVEL] padded fill segments. */
@Composable
internal fun SkillLevelSegments(
    trainedLevel: Int,
    modifier: Modifier = Modifier,
    queuedTargetLevel: Int? = null,
    blinkingLevel: Int? = null,
    activeColor: Color = colorResource(R.color.text_primary),
    queueSegmentColor: Color = colorResource(R.color.skill_level_queue_segment),
) {
    val segmentCount = SkillCatalogConfig.MAX_SKILL_LEVEL
    val filled = trainedLevel.coerceIn(0, segmentCount)
    val queuedThrough = queuedTargetLevel
        ?.coerceIn(0, segmentCount)
        ?: filled
    val blinkTarget = blinkingLevel
        ?.takeIf { it in 1..segmentCount }

    // Only the actively-training row hosts an infinite transition — every row doing so ANRs lists.
    if (blinkTarget != null) {
        SkillLevelSegmentsAnimated(
            modifier = modifier,
            filled = filled,
            queuedThrough = queuedThrough,
            blinkTarget = blinkTarget,
            segmentCount = segmentCount,
            activeColor = activeColor,
            queueSegmentColor = queueSegmentColor,
        )
    } else {
        SkillLevelSegmentsStatic(
            modifier = modifier,
            filled = filled,
            queuedThrough = queuedThrough,
            segmentCount = segmentCount,
            activeColor = activeColor,
            queueSegmentColor = queueSegmentColor,
        )
    }
}

@Composable
private fun SkillLevelSegmentsStatic(
    filled: Int,
    queuedThrough: Int,
    segmentCount: Int,
    activeColor: Color,
    queueSegmentColor: Color,
    modifier: Modifier = Modifier,
) {
    val segmentColors = remember(filled, queuedThrough, segmentCount, activeColor, queueSegmentColor) {
        skillLevelSegmentColors(
            filled = filled,
            queuedThrough = queuedThrough,
            segmentCount = segmentCount,
            blinkTarget = null,
            blinkAlpha = 1f,
            activeColor = activeColor,
            queueSegmentColor = queueSegmentColor,
        )
    }
    SkillLevelSegmentsFrame(
        modifier = modifier,
        segmentColors = segmentColors,
        activeColor = activeColor,
    )
}

@Composable
private fun SkillLevelSegmentsAnimated(
    filled: Int,
    queuedThrough: Int,
    blinkTarget: Int,
    segmentCount: Int,
    activeColor: Color,
    queueSegmentColor: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "skill_level_segment_blink")
    val blinkAlpha by transition.animateFloat(
        initialValue = SkillCatalogConfig.QUEUE_LEVEL_BLINK_ALPHA_MIN,
        targetValue = SkillCatalogConfig.QUEUE_LEVEL_BLINK_ALPHA_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SkillCatalogConfig.QUEUE_LEVEL_BLINK_PERIOD_MS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skill_level_segment_blink_alpha",
    )
    val segmentColors = skillLevelSegmentColors(
        filled = filled,
        queuedThrough = queuedThrough,
        segmentCount = segmentCount,
        blinkTarget = blinkTarget,
        blinkAlpha = blinkAlpha,
        activeColor = activeColor,
        queueSegmentColor = queueSegmentColor,
    )
    SkillLevelSegmentsFrame(
        modifier = modifier,
        segmentColors = segmentColors,
        activeColor = activeColor,
    )
}

@Composable
private fun SkillLevelSegmentsFrame(
    segmentColors: List<Color>,
    activeColor: Color,
    modifier: Modifier = Modifier,
) {
    val width = dimensionResource(R.dimen.skill_level_segments_width)
    val height = dimensionResource(R.dimen.skill_level_segments_height)
    val borderWidth = dimensionResource(R.dimen.skill_level_segments_border)
    val inset = dimensionResource(R.dimen.skill_level_segments_inset)
    val gap = dimensionResource(R.dimen.skill_level_segments_gap)
    val outerCorner = dimensionResource(R.dimen.skill_level_segments_outer_corner)
    val segmentCorner = dimensionResource(R.dimen.skill_level_segments_inner_corner)
    val outerShape = RoundedCornerShape(outerCorner)
    val innerShape = RoundedCornerShape(segmentCorner)

    Row(
        modifier = modifier
            .width(width)
            .height(height)
            .border(width = borderWidth, color = activeColor, shape = outerShape)
            .padding(inset),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        segmentColors.forEach { fillColor ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = fillColor, shape = innerShape),
            )
        }
    }
}

private fun skillLevelSegmentColors(
    filled: Int,
    queuedThrough: Int,
    segmentCount: Int,
    blinkTarget: Int?,
    blinkAlpha: Float,
    activeColor: Color,
    queueSegmentColor: Color,
): List<Color> = List(segmentCount) { index ->
    val levelNumber = index + 1
    when {
        blinkTarget != null && levelNumber == blinkTarget ->
            queueSegmentColor.copy(alpha = queueSegmentColor.alpha * blinkAlpha)
        index < filled -> activeColor
        levelNumber in (filled + 1)..queuedThrough -> queueSegmentColor
        else -> Color.Transparent
    }
}
