package com.marshall.pyerite.characterModule.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterModule.model.CharacterLocationInfo
import com.marshall.pyerite.characterModule.model.CharacterLocationPresence
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import com.marshall.pyerite.characterModule.model.SkillQueueDisplayConfig
import com.marshall.pyerite.characterModule.model.SkillQueueProgress
import com.marshall.pyerite.characterModule.model.SkillQueueTrainingState
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
import com.marshall.pyerite.util.formatDurationDisplay
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CharacterLoggedInListItem(
    modifier: Modifier = Modifier,
    character: LoggedInCharacter,
    showDivider: Boolean,
    isEditMode: Boolean = false,
    onClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    val innerPadding = dimensionResource(R.dimen.character_card_inner_padding)
    val clickAction = when {
        isEditMode && onDeleteClick != null -> onDeleteClick
        !isEditMode && onClick != null -> onClick
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (clickAction != null) {
                    Modifier
                        .semantics { role = Role.Button }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = clickAction,
                        )
                } else {
                    Modifier
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterAvatar(
                portraitUrl = character.portraitUrl,
                size = dimensionResource(R.dimen.character_list_avatar_size),
                corporationIconUrl = character.corporationIconUrl,
                allianceIconUrl = character.allianceIconUrl,
                showBadges = true,
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.detail_row_icon_gap)))
            Column(modifier = Modifier.weight(1f)) {
                val nameSize = dimensionResource(R.dimen.character_list_name_text_size).value.sp
                val metaSize = dimensionResource(R.dimen.character_list_meta_text_size).value.sp
                Text(
                    text = character.name,
                    color = colorResource(R.color.text_primary),
                    fontWeight = FontWeight.Bold,
                    fontSize = nameSize,
                    lineHeight = nameSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = compactTextStyle(nameSize),
                )
                CharacterLocationLine(
                    location = character.location,
                    textSize = metaSize,
                )
                character.walletBalance?.let { balance ->
                    Text(
                        text = stringResource(R.string.character_wallet, balance),
                        color = colorResource(R.color.text_caption),
                        fontSize = metaSize,
                        lineHeight = metaSize,
                        style = compactTextStyle(metaSize),
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.character_list_meta_spacing)),
                    )
                }
                if (character.totalSkillPoints != null) {
                    Text(
                        text = stringResource(
                            R.string.character_total_skill_points,
                            character.totalSkillPoints,
                            character.unallocatedSkillPoints.orEmpty(),
                        ),
                        color = colorResource(R.color.text_caption),
                        fontSize = metaSize,
                        lineHeight = metaSize,
                        style = compactTextStyle(metaSize),
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.character_list_meta_spacing)),
                    )
                }
                character.skillQueue?.let { queue ->
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.character_skill_queue_top_spacing)))
                    CharacterSkillQueueSection(queue = queue, textSize = metaSize)
                }
            }
            if (isEditMode) {
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.character_delete_icon_gap)))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.character_delete),
                    tint = colorResource(R.color.character_delete),
                    modifier = Modifier.size(dimensionResource(R.dimen.character_delete_icon_size)),
                )
            }
        }
        if (showDivider) {
            ItemDivider()
        }
    }
}

@Composable
private fun CharacterLocationLine(
    location: CharacterLocationInfo?,
    textSize: TextUnit,
) {
    if (location == null) return

    val securityColor = when {
        location.systemSecurityStatus <= 0.0 -> colorResource(R.color.character_security_negative)
        location.systemSecurityStatus < 0.5 -> colorResource(R.color.character_security_low)
        else -> colorResource(R.color.character_security_high)
    }
    val placeText = if (location.regionName.isNotBlank()) {
        stringResource(
            R.string.character_location_system_region,
            location.systemName,
            location.regionName,
        )
    } else {
        location.systemName
    }
    val presenceText = when (location.presence) {
        CharacterLocationPresence.IN_STRUCTURE ->
            stringResource(R.string.character_location_in_structure)
        CharacterLocationPresence.IN_SPACE ->
            stringResource(R.string.character_location_in_space)
    }

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = securityColor)) {
                append(
                    String.format(
                        Locale.US,
                        CharacterLocationDisplayConfig.SECURITY_FORMAT,
                        location.systemSecurityStatus,
                    ),
                )
            }
            append(CharacterLocationDisplayConfig.SEGMENT_GAP)
            withStyle(SpanStyle(color = colorResource(R.color.text_primary))) {
                append(placeText)
            }
            append(CharacterLocationDisplayConfig.SEGMENT_GAP)
            withStyle(SpanStyle(color = colorResource(R.color.text_caption))) {
                append(presenceText)
            }
        },
        fontSize = textSize,
        lineHeight = textSize,
        style = compactTextStyle(textSize),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = dimensionResource(R.dimen.character_list_meta_spacing)),
    )
}

private object CharacterLocationDisplayConfig {
    const val SECURITY_FORMAT = "%.1f"
    const val SEGMENT_GAP = " "
}

@Composable
private fun CharacterSkillQueueSection(
    queue: SkillQueueProgress,
    textSize: TextUnit,
) {
    val lastFinishMs = remember(queue) {
        queue.entries.mapNotNull { it.finishAtEpochMs }.maxOrNull()
    }
    val nowMs by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = queue,
        key2 = lastFinishMs,
    ) {
        while (isActive) {
            value = System.currentTimeMillis()
            if (lastFinishMs == null || value >= lastFinishMs) break
            delay(SkillQueueDisplayConfig.UI_TICK_MS.milliseconds)
        }
    }
    val snapshot = queue.snapshotAt(nowMs)

    when (snapshot.state) {
        SkillQueueTrainingState.IDLE -> {
            Text(
                text = stringResource(R.string.character_skill_queue_idle),
                color = colorResource(R.color.text_caption),
                fontSize = textSize,
                lineHeight = textSize,
                style = compactTextStyle(textSize),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SkillQueueTrainingState.PAUSED,
        SkillQueueTrainingState.TRAINING,
        -> {
            val entry = snapshot.entry ?: return
            val isPaused = snapshot.state == SkillQueueTrainingState.PAUSED
            val fillColor = colorResource(
                if (isPaused) {
                    R.color.character_skill_progress_paused
                } else {
                    R.color.character_skill_progress_active
                },
            )
            val trackColor = colorResource(R.color.character_skill_progress_track)
            val progressShape = RoundedCornerShape(
                dimensionResource(R.dimen.character_skill_progress_corner_radius),
            )
            val statusIcon: ImageVector =
                if (isPaused) Icons.Default.Pause else Icons.Default.PlayArrow
            val trailingText = if (isPaused) {
                stringResource(R.string.character_skill_queue_paused)
            } else {
                formatRemainingDuration(snapshot.remainingSeconds)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = fillColor,
                    modifier = Modifier.padding(
                        end = dimensionResource(R.dimen.character_skill_queue_icon_gap),
                    ),
                )
                Text(
                    text = stringResource(
                        R.string.character_training_skill,
                        entry.skillName,
                        entry.level,
                    ),
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.text_primary),
                    fontSize = textSize,
                    lineHeight = textSize,
                    style = compactTextStyle(textSize),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = trailingText,
                    color = colorResource(R.color.text_caption),
                    fontSize = textSize,
                    lineHeight = textSize,
                    style = compactTextStyle(textSize),
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.character_skill_queue_label_spacing))
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.character_skill_progress_height))
                    .clip(progressShape)
                    .background(trackColor),
            ) {
                if (snapshot.progress > SkillQueueDisplayConfig.PROGRESS_MIN) {
                    SkillProgressFill(
                        progress = snapshot.progress,
                        activeColor = fillColor,
                        shimmerColor = colorResource(R.color.character_skill_progress_shimmer),
                        animate = !isPaused && snapshot.remainingSeconds > 0L,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillProgressFill(
    progress: Float,
    activeColor: Color,
    shimmerColor: Color,
    animate: Boolean,
) {
    val shimmerProgress by rememberInfiniteTransition(label = "skillProgressShimmer")
        .animateFloat(
            initialValue = SkillQueueDisplayConfig.PROGRESS_MIN,
            targetValue = SkillQueueDisplayConfig.PROGRESS_MAX,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = SkillQueueDisplayConfig.SHIMMER_DURATION_MS,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "skillProgressShimmerX",
        )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(progress)
            .clip(RoundedCornerShape(dimensionResource(R.dimen.character_skill_progress_corner_radius)))
            .background(activeColor),
    ) {
        if (animate && maxWidth > Dp.Hairline) {
            val bandWidth = maxWidth * SkillQueueDisplayConfig.SHIMMER_WIDTH_FRACTION
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
                                shimmerColor.copy(alpha = SkillQueueDisplayConfig.SHIMMER_PEAK_ALPHA),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
    }
}

private fun formatRemainingDuration(remainingSeconds: Long): String =
    formatDurationDisplay(
        totalSeconds = remainingSeconds,
        includeSeconds = false,
    )

@Composable
private fun compactTextStyle(lineHeight: TextUnit): TextStyle = TextStyle(
    lineHeight = lineHeight,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)
