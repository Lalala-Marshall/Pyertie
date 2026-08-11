package com.marshall.pyerite.characterSkillsModule.model

/**
 * SP / time totals for a skill plan against one character's catalog progress.
 */
data class SkillPlanProgressSummary(
    val needLearnSp: Long,
    val needSeconds: Long,
    val totalSp: Long,
) {
    companion object {
        val EMPTY = SkillPlanProgressSummary(
            needLearnSp = 0L,
            needSeconds = 0L,
            totalSp = 0L,
        )
    }
}

/**
 * Resolves plan entry progress using the character's skill catalog + attributes.
 * Entries whose skill is missing from the catalog contribute 0 until catalog loads.
 */
internal object SkillPlanProgressCalculator {

    fun summarize(
        entries: List<SkillPlanEntry>,
        skillsByTypeId: Map<Int, SkillCatalogSkill>,
        attributes: CharacterAttributes,
        attributesReady: Boolean,
    ): SkillPlanProgressSummary {
        if (entries.isEmpty()) return SkillPlanProgressSummary.EMPTY
        var needLearnSp = 0L
        var needSeconds = 0L
        var totalSp = 0L
        for (entry in entries) {
            val skill = skillsByTypeId[entry.skillTypeId] ?: continue
            val targetLevel = entry.targetLevel.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
            val targetSp = SkillCatalogConfig.cumulativeSpToLevel(
                skillTimeConstant = skill.skillTimeConstant,
                level = targetLevel,
            )
            totalSp += targetSp
            val remainingSp = (targetSp - skill.trainedSp).coerceAtLeast(0L)
            needLearnSp += remainingSp
            if (attributesReady && remainingSp > 0L) {
                needSeconds += secondsToReachTarget(
                    skill = skill,
                    targetLevel = targetLevel,
                    attributes = attributes,
                )
            }
        }
        return SkillPlanProgressSummary(
            needLearnSp = needLearnSp,
            needSeconds = needSeconds,
            totalSp = totalSp,
        )
    }

    fun isEntryCompleted(
        entry: SkillPlanEntry,
        skillsByTypeId: Map<Int, SkillCatalogSkill>,
    ): Boolean {
        val skill = skillsByTypeId[entry.skillTypeId] ?: return false
        return skill.trainedLevel >= entry.targetLevel.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
    }

    private fun secondsToReachTarget(
        skill: SkillCatalogSkill,
        targetLevel: Int,
        attributes: CharacterAttributes,
    ): Long {
        if (skill.trainedLevel >= targetLevel) return 0L
        var trainedLevel = skill.trainedLevel
        var trainedSp = skill.trainedSp
        var totalSeconds = 0L
        while (trainedLevel < targetLevel) {
            val seconds = SkillCatalogConfig.secondsToTrainNextLevel(
                skillTimeConstant = skill.skillTimeConstant,
                trainedLevel = trainedLevel,
                trainedSp = trainedSp,
                primaryAttributeTypeId = skill.primaryAttributeTypeId,
                secondaryAttributeTypeId = skill.secondaryAttributeTypeId,
                attributes = attributes,
            ) ?: return totalSeconds
            totalSeconds += seconds
            trainedLevel += 1
            trainedSp = SkillCatalogConfig.cumulativeSpToLevel(
                skillTimeConstant = skill.skillTimeConstant,
                level = trainedLevel,
            )
        }
        return totalSeconds
    }
}
