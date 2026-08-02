package com.marshall.pyerite.characterSkillsModule.model

/**
 * EVE Large / Small Skill Injector type ids and SP yield tiers
 * (total SP including unallocated at time of use).
 */
internal object SkillInjectorConfig {
    const val LARGE_TYPE_ID = 40_520
    const val SMALL_TYPE_ID = 45_635

    /** Safety cap when simulating sequential injections. */
    const val MAX_INJECTORS_SIMULATED = 100_000

    /** Five Small injectors convert to one Large (same SP band ratio). */
    const val SMALLS_PER_LARGE = 5

    const val TIER_THRESHOLD_5M = 5_000_000L
    const val TIER_THRESHOLD_50M = 50_000_000L
    const val TIER_THRESHOLD_80M = 80_000_000L

    const val LARGE_YIELD_BELOW_5M = 500_000L
    const val LARGE_YIELD_5M_TO_50M = 400_000L
    const val LARGE_YIELD_50M_TO_80M = 300_000L
    const val LARGE_YIELD_ABOVE_80M = 150_000L

    const val SMALL_YIELD_BELOW_5M = 100_000L
    const val SMALL_YIELD_5M_TO_50M = 80_000L
    const val SMALL_YIELD_50M_TO_80M = 60_000L
    const val SMALL_YIELD_ABOVE_80M = 30_000L
}

internal enum class SkillInjectorSize {
    LARGE,
    SMALL,
}
