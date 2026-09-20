package com.marshall.pyerite.characterMasteryModule.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.marshall.pyerite.R

/** Hull mastery constants (Tritanium ShipMasteryBrowserView / MasteryEvaluator). */
internal object MasteryConfig {
    const val SHIP_CATEGORY_ID = 6
    const val MIN_LEVEL = 0
    const val MAX_LEVEL = 5
    const val GRADIENT_BASE_END_FRACTION = 0.6f
    const val GRADIENT_TINT_END_FRACTION = 1.0f
    const val TINT_ALPHA_LIGHT = 0.7f
    const val TINT_ALPHA_DARK = 0.4f
    const val ICON_SIZE_DP = 42

    val LEVEL_SYMBOLS = listOf("0", "I", "II", "III", "IV", "V")

    private val TINT_LOCKED = Color(0xFFB0433C)
    private val TINT_LEVEL_0_TO_4 = Color(0xFF4163AC)
    private val TINT_LEVEL_5 = Color(0xFFB48F3F)

    private val LEVEL_ICONS = intArrayOf(
        R.drawable.mastery_level_0,
        R.drawable.mastery_level_1,
        R.drawable.mastery_level_2,
        R.drawable.mastery_level_3,
        R.drawable.mastery_level_4,
        R.drawable.mastery_level_5,
    )

    fun levelSymbol(level: Int): String = LEVEL_SYMBOLS.getOrElse(level) { LEVEL_SYMBOLS.first() }

    fun rowTint(state: MasteryLevelState): Color = when (state) {
        MasteryLevelState.Locked -> TINT_LOCKED
        is MasteryLevelState.Level ->
            if (state.level >= MAX_LEVEL) TINT_LEVEL_5 else TINT_LEVEL_0_TO_4
    }

    @DrawableRes
    fun iconRes(state: MasteryLevelState): Int = when (state) {
        MasteryLevelState.Locked -> R.drawable.mastery_level_locked
        is MasteryLevelState.Level ->
            LEVEL_ICONS.getOrElse(state.level) { R.drawable.mastery_level_0 }
    }
}
