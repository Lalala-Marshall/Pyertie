package com.marshall.pyerite.characterMasteryModule.data

import com.marshall.pyerite.characterMasteryModule.model.MasteryConfig

/**
 * Two-step mastery calculator matching Tritanium [MasteryEvaluator].
 * Certificate matrix is computed once per skill set; per-hull lookup is dictionary-only.
 */
internal object MasteryEvaluator {

    /**
     * certificateID → highest satisfied cert tier 0–5.
     * Tier N uses `tierLevels[N-1]`; 0 means that skill is not required at that tier.
     */
    fun certificateLevels(
        characterSkills: Map<Int, Int>,
        requirementsByCert: Map<Int, List<CertificateSkillRequirement>>,
    ): Map<Int, Int> {
        val levels = HashMap<Int, Int>(requirementsByCert.size)
        for ((certificateId, requirements) in requirementsByCert) {
            var highest = 0
            tierLoop@ for (tier in MasteryConfig.MAX_LEVEL downTo 1) {
                for (requirement in requirements) {
                    val requiredLevel = requirement.tierLevels[tier - 1]
                    if (requiredLevel == 0) continue
                    val trainedLevel = characterSkills[requirement.skillId] ?: 0
                    if (trainedLevel < requiredLevel) continue@tierLoop
                }
                highest = tier
                break
            }
            levels[certificateId] = highest
        }
        return levels
    }

    /**
     * Highest mastery 0–5 for [typeId], or null when the hull has no mastery rows.
     * Level N requires every certificate listed at N to be at least N.
     */
    fun masteryLevel(
        typeId: Int,
        certLevels: Map<Int, Int>,
        certsByTypeAndLevel: Map<Int, Map<Int, List<Int>>>,
    ): Int? {
        val levelCerts = certsByTypeAndLevel[typeId] ?: return null
        for (level in MasteryConfig.MAX_LEVEL downTo 1) {
            val certs = levelCerts[level].orEmpty()
            if (certs.isEmpty()) continue
            if (certs.all { (certLevels[it] ?: 0) >= level }) return level
        }
        return MasteryConfig.MIN_LEVEL
    }
}

internal data class CertificateSkillRequirement(
    val skillId: Int,
    val tierLevels: List<Int>,
)
