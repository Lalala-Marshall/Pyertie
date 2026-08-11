package com.marshall.pyerite.characterSkillsModule.model

/**
 * Optimal neural-remap allocation for the current skill queue
 * (EVE Nexus `SkillTrainingCalculator.calculateOptimalAttributes`).
 */
internal object OptimalAttributeAllocator {

    /**
     * @param trainingNeeds queue entries with remaining SP and dogma primary/secondary
     * @param currentAttributes ESI attributes (include implants and any booster)
     * @param implantBonuses max implant bonus per attribute from fitted implants
     */
    fun allocate(
        trainingNeeds: List<QueueSkillTrainingNeed>,
        currentAttributes: CharacterAttributes,
        implantBonuses: ImplantAttributeBonuses,
    ): OptimalAttributeAllocation? {
        if (trainingNeeds.isEmpty()) return null

        val boosterBonus = detectBoosterBonus(currentAttributes, implantBonuses)
        val currentWithoutBooster = currentAttributes.withoutBooster(boosterBonus)
        val currentSeconds = totalTrainingSeconds(trainingNeeds, currentWithoutBooster)
            ?: return null

        val maxBonus = AttributeRemapConfig.MAX_BONUS_PER_ATTRIBUTE
        val pool = AttributeRemapConfig.REMAP_POOL
        val bestBonuses = IntArray(ATTRIBUTE_COUNT)
        var bestSeconds = totalTrainingSecondsForBonuses(
            trainingNeeds = trainingNeeds,
            bonuses = IntArray(ATTRIBUTE_COUNT),
            implantBonuses = implantBonuses,
        ) ?: return null

        val current = IntArray(ATTRIBUTE_COUNT)
        fun enumerate(index: Int, remaining: Int) {
            if (remaining > (ATTRIBUTE_COUNT - index) * maxBonus) return
            if (index == ATTRIBUTE_COUNT - 1) {
                current[index] = remaining
                val seconds = totalTrainingSecondsForBonuses(
                    trainingNeeds = trainingNeeds,
                    bonuses = current,
                    implantBonuses = implantBonuses,
                )
                if (seconds != null && seconds < bestSeconds) {
                    bestSeconds = seconds
                    current.copyInto(bestBonuses)
                }
                current[index] = 0
                return
            }
            val upper = minOf(remaining, maxBonus)
            for (points in 0..upper) {
                current[index] = points
                enumerate(index + 1, remaining - points)
            }
            current[index] = 0
        }
        enumerate(0, pool)

        val base = AttributeRemapConfig.BASE_POINTS
        return OptimalAttributeAllocation(
            perception = base + bestBonuses[INDEX_PERCEPTION],
            memory = base + bestBonuses[INDEX_MEMORY],
            willpower = base + bestBonuses[INDEX_WILLPOWER],
            intelligence = base + bestBonuses[INDEX_INTELLIGENCE],
            charisma = base + bestBonuses[INDEX_CHARISMA],
            currentTrainingSeconds = currentSeconds,
            optimalTrainingSeconds = bestSeconds,
            savedSeconds = (currentSeconds - bestSeconds).coerceAtLeast(0L),
            detectedBoosterBonus = boosterBonus,
        )
    }

    /**
     * Remaining SP for one queue entry (same rules as injector remaining).
     */
    fun remainingSpForEntry(entry: SkillQueueHeadTraining, nowMs: Long): Long? {
        val levelEnd = entry.levelEndSp ?: return null
        val trainingStart = entry.trainingStartSp ?: return null
        val currentSp = if (entry.isActivelyTrainingAt(nowMs)) {
            entry.currentSpAt(nowMs) ?: trainingStart
        } else {
            trainingStart
        }
        return (levelEnd - currentSp).coerceAtLeast(0L)
    }

    fun detectBoosterBonus(
        attributes: CharacterAttributes,
        implantBonuses: ImplantAttributeBonuses,
    ): Int {
        val totalAttributes = attributes.perception + attributes.memory +
            attributes.willpower + attributes.intelligence + attributes.charisma
        val totalImplants = implantBonuses.perception + implantBonuses.memory +
            implantBonuses.willpower + implantBonuses.intelligence + implantBonuses.charisma
        val booster = (
            totalAttributes -
                AttributeRemapConfig.BASE_TOTAL_POINTS -
                AttributeRemapConfig.REMAP_POOL -
                totalImplants
            ) / ATTRIBUTE_COUNT
        return booster.coerceAtLeast(0)
    }

    private fun totalTrainingSecondsForBonuses(
        trainingNeeds: List<QueueSkillTrainingNeed>,
        bonuses: IntArray,
        implantBonuses: ImplantAttributeBonuses,
    ): Long? {
        val base = AttributeRemapConfig.BASE_POINTS
        val attrs = CharacterAttributes(
            characterId = 0L,
            perception = base + bonuses[INDEX_PERCEPTION] + implantBonuses.perception,
            memory = base + bonuses[INDEX_MEMORY] + implantBonuses.memory,
            willpower = base + bonuses[INDEX_WILLPOWER] + implantBonuses.willpower,
            intelligence = base + bonuses[INDEX_INTELLIGENCE] + implantBonuses.intelligence,
            charisma = base + bonuses[INDEX_CHARISMA] + implantBonuses.charisma,
            bonusRemaps = 0,
        )
        return totalTrainingSeconds(trainingNeeds, attrs)
    }

    private fun totalTrainingSeconds(
        trainingNeeds: List<QueueSkillTrainingNeed>,
        attributes: CharacterAttributes,
    ): Long? {
        var totalSeconds = 0.0
        for (need in trainingNeeds) {
            if (need.remainingSp <= 0L) continue
            val ratePerHour = trainingRateSpPerHour(
                primaryAttributeTypeId = need.primaryAttributeTypeId,
                secondaryAttributeTypeId = need.secondaryAttributeTypeId,
                attributes = attributes,
            ) ?: return null
            if (ratePerHour <= 0) return null
            totalSeconds += need.remainingSp.toDouble() / ratePerHour.toDouble() *
                SECONDS_PER_HOUR
        }
        return totalSeconds.toLong().coerceAtLeast(0L)
    }

    /** SP/hour = (primary + secondary/2) × 60 — same as Nexus. */
    fun trainingRateSpPerHour(
        primaryAttributeTypeId: Int,
        secondaryAttributeTypeId: Int,
        attributes: CharacterAttributes,
    ): Int? {
        val primary = attributes.pointsForAttributeTypeId(primaryAttributeTypeId) ?: return null
        val secondary = attributes.pointsForAttributeTypeId(secondaryAttributeTypeId)
            ?: return null
        val pointsPerMinute = primary + secondary / 2.0
        return (pointsPerMinute * MINUTES_PER_HOUR).toInt()
    }

    private fun CharacterAttributes.withoutBooster(boosterBonus: Int): CharacterAttributes {
        if (boosterBonus <= 0) return this
        return copy(
            perception = (perception - boosterBonus).coerceAtLeast(0),
            memory = (memory - boosterBonus).coerceAtLeast(0),
            willpower = (willpower - boosterBonus).coerceAtLeast(0),
            intelligence = (intelligence - boosterBonus).coerceAtLeast(0),
            charisma = (charisma - boosterBonus).coerceAtLeast(0),
        )
    }

    private const val ATTRIBUTE_COUNT = 5
    private const val INDEX_PERCEPTION = 0
    private const val INDEX_MEMORY = 1
    private const val INDEX_WILLPOWER = 2
    private const val INDEX_INTELLIGENCE = 3
    private const val INDEX_CHARISMA = 4
    private const val MINUTES_PER_HOUR = 60
    private const val SECONDS_PER_HOUR = 3_600.0
}

/** One queue entry’s remaining SP and dogma training attributes. */
internal data class QueueSkillTrainingNeed(
    val remainingSp: Long,
    val primaryAttributeTypeId: Int,
    val secondaryAttributeTypeId: Int,
)

/** Max implant bonus per character attribute (dogma 175–179). */
data class ImplantAttributeBonuses(
    val perception: Int = 0,
    val memory: Int = 0,
    val willpower: Int = 0,
    val intelligence: Int = 0,
    val charisma: Int = 0,
) {
    companion object {
        val ZERO = ImplantAttributeBonuses()
    }
}

/** Optimal remap result (base + bonus only; implants not included in displayed points). */
data class OptimalAttributeAllocation(
    val perception: Int,
    val memory: Int,
    val willpower: Int,
    val intelligence: Int,
    val charisma: Int,
    val currentTrainingSeconds: Long,
    val optimalTrainingSeconds: Long,
    val savedSeconds: Long,
    val detectedBoosterBonus: Int,
) {
    fun allocatedBonus(attribute: OptimalAttributeKind): Int {
        val total = when (attribute) {
            OptimalAttributeKind.PERCEPTION -> perception
            OptimalAttributeKind.MEMORY -> memory
            OptimalAttributeKind.WILLPOWER -> willpower
            OptimalAttributeKind.INTELLIGENCE -> intelligence
            OptimalAttributeKind.CHARISMA -> charisma
        }
        return (total - AttributeRemapConfig.BASE_POINTS).coerceAtLeast(0)
    }
}

enum class OptimalAttributeKind {
    PERCEPTION,
    MEMORY,
    WILLPOWER,
    INTELLIGENCE,
    CHARISMA,
}
