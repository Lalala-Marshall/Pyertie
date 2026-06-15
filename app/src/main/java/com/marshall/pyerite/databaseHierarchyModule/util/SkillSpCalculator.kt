package com.marshall.pyerite.databaseHierarchyModule.util

import kotlin.math.pow

/**
 * EVE skill SP: per-level `250 × skillTimeConstant × 32^((level - 1) / 2)`.
 * Cumulative from level 0 is the sum of per-level SP for levels 1 through the target level.
 */
internal object SkillSpCalculator {
    private const val BASE_SP = 250.0
    private const val LEVEL_BASE = 32.0
    const val DEFAULT_MAX_TRAINABLE_LEVEL = 5

    fun spForLevel(skillTimeConstant: Double, level: Int): Long {
        require(level >= 1)
        return (BASE_SP * skillTimeConstant * LEVEL_BASE.pow((level - 1) / 2.0)).toLong()
    }

    /**
     * Total SP required to train from level 0 to [targetLevel] (inclusive).
     */
    fun cumulativeSpFromZero(skillTimeConstant: Double, targetLevel: Int): Long {
        require(targetLevel >= 1)
        var total = 0L
        for (level in 1..targetLevel) {
            total += spForLevel(skillTimeConstant, level)
        }
        return total
    }

    fun resolveMaxTrainableLevel(skillLevelAttribute: Double?): Int {
        val raw = skillLevelAttribute?.toInt() ?: 0
        return when {
            raw <= 0 -> DEFAULT_MAX_TRAINABLE_LEVEL
            else -> raw.coerceIn(1, DEFAULT_MAX_TRAINABLE_LEVEL)
        }
    }
}
