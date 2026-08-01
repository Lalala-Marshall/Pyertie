package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueConfig
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueState
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val HomeSkillQueueDurationMaxUnit = DurationDisplayFormatter.MaxUnit.DAY

/** Home-page skills row (under clone status inside the Character section card). */
@Composable
fun MainPageSkillQueueItem(
    status: CharacterSkillQueueStatus,
    detailsReady: Boolean = true,
    onClick: () -> Unit = {},
    showDivider: Boolean = false,
) {
    val hint = when {
        !detailsReady &&
            status.state == CharacterSkillQueueState.IDLE &&
            status.trainingFinishAtEpochMs.isEmpty() &&
            status.pausedSkillCount == 0 -> ""
        else -> skillQueueHint(status)
    }
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_character_skills,
            itemName = stringResource(R.string.character_skills),
            itemHint = hint,
            onClick = onClick,
        ),
        showDivider = showDivider,
    )
}

@Composable
private fun skillQueueHint(status: CharacterSkillQueueStatus): String {
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val nowMs by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = status,
    ) {
        if (status.state != CharacterSkillQueueState.TRAINING) {
            value = System.currentTimeMillis()
            return@produceState
        }
        while (isActive) {
            value = System.currentTimeMillis()
            val remainingEnds = status.trainingFinishAtEpochMs.filter { it > value }
            if (remainingEnds.isEmpty()) break
            delay(CharacterSkillQueueConfig.UI_TICK_MS.milliseconds)
        }
    }

    return when (status.state) {
        CharacterSkillQueueState.IDLE -> {
            stringResource(R.string.character_skills_hint_idle)
        }
        CharacterSkillQueueState.PAUSED -> {
            val durationText = status.pausedRemainingSeconds?.let { seconds ->
                formatDurationDisplay(
                    totalSeconds = seconds,
                    includeSeconds = false,
                    maxUnit = HomeSkillQueueDurationMaxUnit,
                )
            } ?: placeholder
            stringResource(
                R.string.character_skills_hint_paused,
                status.pausedSkillCount,
                durationText,
            )
        }
        CharacterSkillQueueState.TRAINING -> {
            val remainingEnds = status.trainingFinishAtEpochMs.filter { it > nowMs }
            if (remainingEnds.isEmpty()) {
                stringResource(R.string.character_skills_hint_idle)
            } else {
                val endMs = remainingEnds.maxOrNull() ?: nowMs
                val durationText = formatDurationDisplay(
                    totalSeconds = (endMs - nowMs) / CharacterSkillQueueConfig.MILLIS_PER_SECOND,
                    includeSeconds = false,
                    maxUnit = HomeSkillQueueDurationMaxUnit,
                )
                stringResource(
                    R.string.character_skills_hint_training,
                    remainingEnds.size,
                    durationText,
                )
            }
        }
    }
}
