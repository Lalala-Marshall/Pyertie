package com.marshall.pyerite.characterMasteryModule.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMasteryModule.model.MasteryConfig
import com.marshall.pyerite.characterMasteryModule.model.MasteryFilter
import com.marshall.pyerite.characterMasteryModule.model.MasteryLevelState

@Composable
internal fun masteryFilterLabel(filter: MasteryFilter): String {
    val level = filter.masteryLevel
    return when {
        filter == MasteryFilter.ALL -> stringResource(R.string.character_mastery_filter_all)
        filter == MasteryFilter.LOCKED -> stringResource(R.string.character_mastery_filter_locked)
        filter == MasteryFilter.QUALIFIED -> stringResource(R.string.character_mastery_filter_qualified)
        level != null -> stringResource(
            R.string.character_mastery_filter_level,
            MasteryConfig.levelSymbol(level),
        )
        else -> stringResource(R.string.character_mastery_filter_all)
    }
}

@Composable
internal fun masteryRowBackgroundBrush(state: MasteryLevelState): Brush {
    val base = colorResource(R.color.second_background)
    val alpha = if (isSystemInDarkTheme()) {
        MasteryConfig.TINT_ALPHA_DARK
    } else {
        MasteryConfig.TINT_ALPHA_LIGHT
    }
    val tint = MasteryConfig.rowTint(state).copy(alpha = alpha)
    return Brush.horizontalGradient(
        colorStops = arrayOf(
            0f to base,
            MasteryConfig.GRADIENT_BASE_END_FRACTION to base,
            MasteryConfig.GRADIENT_TINT_END_FRACTION to tint,
        ),
    )
}
