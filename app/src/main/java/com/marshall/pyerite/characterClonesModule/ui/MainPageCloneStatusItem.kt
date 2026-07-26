package com.marshall.pyerite.characterClonesModule.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.characterClonesModule.model.JumpCloneConfig
import com.marshall.pyerite.eveAuthModule.sso.EveSsoConfig
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.util.formatDurationDisplay
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Home-page clone status row (pair with Character Sheet inside the Character section card). */
@Composable
fun MainPageCloneStatusItem(
    nextCloneJumpEpochMs: Long?,
    onClick: () -> Unit = {},
    showDivider: Boolean = false,
) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_character_clones,
            itemName = stringResource(R.string.character_clone_status),
            itemHint = cloneJumpHint(nextCloneJumpEpochMs),
            onClick = onClick,
        ),
        showDivider = showDivider,
    )
}

@Composable
private fun cloneJumpHint(nextCloneJumpEpochMs: Long?): String {
    val readyNow = stringResource(R.string.character_clone_jump_now)
    val nowMs by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = nextCloneJumpEpochMs,
    ) {
        val targetMs = nextCloneJumpEpochMs
        if (targetMs == null) {
            value = System.currentTimeMillis()
            return@produceState
        }
        while (isActive) {
            value = System.currentTimeMillis()
            if (value >= targetMs) break
            delay(JumpCloneConfig.UI_TICK_MS.milliseconds)
        }
    }
    val targetMs = nextCloneJumpEpochMs ?: return ""
    val whenText = if (nowMs >= targetMs) {
        readyNow
    } else {
        formatDurationDisplay(
            totalSeconds = (targetMs - nowMs) / EveSsoConfig.MILLIS_PER_SECOND,
            includeSeconds = false,
        )
    }
    return stringResource(R.string.character_clone_jump_hint, whenText)
}
