package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemLayout
import com.marshall.pyerite.ui.golbalComponents.baseLazyColumnItemAdaptiveIconSize

/**
 * Thin blue level-progress under the queue-head skill row, with a left-to-right
 * white shimmer. Right edge aligns with remaining-time (skips chevron).
 */
@Composable
internal fun SkillQueueTrainingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    animateShimmer: Boolean = true,
    showLeadingIcon: Boolean = false,
    /** Same line count as [SkillCatalogSkillRow] (title + SP hint). */
    contentLineCount: Int = BaseLazyColumnItemLayout.TITLE_LINE_COUNT + 1,
) {
    val shape = RoundedCornerShape(
        dimensionResource(R.dimen.skill_queue_training_progress_corner_radius),
    )
    val trackColor = colorResource(R.color.character_skill_progress_track)
    val fillColor = colorResource(R.color.skill_queue_training_progress_fill)
    val shimmerColor = colorResource(R.color.character_skill_progress_shimmer)
    val clamped = progress.coerceIn(
        SkillCatalogConfig.PROGRESS_MIN,
        SkillCatalogConfig.PROGRESS_MAX,
    )
    val rowPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val chevronSize = dimensionResource(R.dimen.detail_row_chevron_size)
    val chevronGap = dimensionResource(R.dimen.detail_row_trailing_gap)
    val startPadding = if (showLeadingIcon) {
        val iconSize = baseLazyColumnItemAdaptiveIconSize(contentLineCount = contentLineCount)
        val iconGap = dimensionResource(R.dimen.detail_row_icon_gap)
        rowPadding + iconSize + iconGap
    } else {
        rowPadding
    }
    val endPadding = rowPadding + chevronSize + chevronGap

    Box(
        modifier = modifier
            .padding(
                start = startPadding,
                end = endPadding,
                // Flush to SP hint above; only keep space before the divider below.
                bottom = dimensionResource(R.dimen.skill_queue_training_progress_vertical_padding),
            )
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.skill_queue_training_progress_height))
            .clip(shape)
            .background(trackColor),
    ) {
        if (clamped > SkillCatalogConfig.PROGRESS_MIN) {
            SkillQueueTrainingProgressFill(
                progress = clamped,
                fillColor = fillColor,
                shimmerColor = shimmerColor,
                corner = dimensionResource(R.dimen.skill_queue_training_progress_corner_radius),
                animateShimmer = animateShimmer,
            )
        }
    }
}

@Composable
private fun SkillQueueTrainingProgressFill(
    progress: Float,
    fillColor: Color,
    shimmerColor: Color,
    corner: Dp,
    animateShimmer: Boolean,
) {
    val shape = RoundedCornerShape(corner)
    val shimmerProgress by rememberInfiniteTransition(label = "skill_queue_progress_shimmer")
        .animateFloat(
            initialValue = SkillCatalogConfig.PROGRESS_MIN,
            targetValue = SkillCatalogConfig.PROGRESS_MAX,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = CharacterSkillQueueConfig.PROGRESS_SHIMMER_DURATION_MS,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "skill_queue_progress_shimmer_x",
        )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(progress)
            .clip(shape)
            .background(fillColor),
    ) {
        if (animateShimmer && maxWidth > Dp.Hairline) {
            val bandWidth = maxWidth * CharacterSkillQueueConfig.PROGRESS_SHIMMER_WIDTH_FRACTION
            val travel = maxWidth + bandWidth
            val offsetX = -bandWidth + travel * shimmerProgress
            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .width(bandWidth)
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                shimmerColor.copy(
                                    alpha = CharacterSkillQueueConfig.PROGRESS_SHIMMER_PEAK_ALPHA,
                                ),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
    }
}
