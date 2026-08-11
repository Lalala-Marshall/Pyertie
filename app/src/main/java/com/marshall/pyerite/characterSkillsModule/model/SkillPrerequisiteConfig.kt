package com.marshall.pyerite.characterSkillsModule.model

/**
 * Dogma attribute names / category for skill prerequisite slots (`requiredSkill1`…`6`).
 */
internal object SkillPrerequisiteConfig {
    /** Dogma attribute category used for skill-requirement attributes. */
    const val SKILL_REQUIREMENT_ATTRIBUTE_CATEGORY_ID = 8

    const val REQUIRED_SKILL_SLOT_COUNT = 6

    const val DEFAULT_REQUIRED_LEVEL = 1

    fun requiredSkillAttributeName(slot: Int): String = "requiredSkill$slot"

    fun requiredSkillLevelAttributeName(slot: Int): String = "requiredSkill${slot}Level"
}
