package com.marshall.pyerite.characterSkillsModule.model

/**
 * Neural remap pool and implant dogma attribute ids used for optimal
 * attribute allocation (same rules as EVE Nexus / CCP).
 */
internal object AttributeRemapConfig {
    /** Base points per attribute before remap / implants / boosters. */
    const val BASE_POINTS = 17

    /** Points available to distribute on a neural remap. */
    const val REMAP_POOL = 14

    /** Max points that can be added to a single attribute from the remap pool. */
    const val MAX_BONUS_PER_ATTRIBUTE = 10

    /** Five attributes × [BASE_POINTS] (excludes remap pool and implants). */
    const val BASE_TOTAL_POINTS = BASE_POINTS * 5

    /** Dogma typeAttributes ids for implant attribute bonuses. */
    const val IMPLANT_ATTR_CHARISMA = 175
    const val IMPLANT_ATTR_INTELLIGENCE = 176
    const val IMPLANT_ATTR_MEMORY = 177
    const val IMPLANT_ATTR_PERCEPTION = 178
    const val IMPLANT_ATTR_WILLPOWER = 179

    val IMPLANT_ATTRIBUTE_IDS: List<Int> = listOf(
        IMPLANT_ATTR_CHARISMA,
        IMPLANT_ATTR_INTELLIGENCE,
        IMPLANT_ATTR_MEMORY,
        IMPLANT_ATTR_PERCEPTION,
        IMPLANT_ATTR_WILLPOWER,
    )
}
