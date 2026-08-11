package com.marshall.pyerite.characterSkillsModule.model

/**
 * Remaining SP to finish the ESI skill queue and injector counts to cover that gap.
 *
 * Matches EVE Nexus: required SP is Σ(level_end_sp − current/training_start_sp) per
 * queue entry (unallocated SP is **not** subtracted). Injector yield tiers use
 * total SP including unallocated.
 */
internal object SkillQueueInjectorNeeds {

    /**
     * Sum of SP still needed on each queue entry.
     *
     * Same rules as EVE Nexus `calculateInjectors`:
     * - Skip entries missing `level_end_sp` / `training_start_sp` (no `0` fallback —
     *   that would treat cumulative `level_end_sp` as the full remaining amount).
     * - Actively training entry (now ∈ start…finish): live [SkillQueueHeadTraining.currentSpAt].
     * - All others: frozen `training_start_sp`.
     */
    fun remainingQueueSp(
        entries: List<SkillQueueHeadTraining>,
        nowMs: Long,
    ): Long {
        if (entries.isEmpty()) return 0L
        return entries.sumOf { entry ->
            val levelEnd = entry.levelEndSp ?: return@sumOf 0L
            val trainingStart = entry.trainingStartSp ?: return@sumOf 0L
            val currentSp = if (entry.isActivelyTrainingAt(nowMs)) {
                entry.currentSpAt(nowMs) ?: trainingStart
            } else {
                trainingStart
            }
            (levelEnd - currentSp).coerceAtLeast(0L)
        }
    }

    /**
     * Large-first mix: take full Large injectors while the gap still fits one,
     * then cover the remainder with Smalls — unless that remainder needs
     * [SkillInjectorConfig.SMALLS_PER_LARGE] or more Smalls, in which case take
     * one more Large instead (e.g. 149_768 SP → 1× Large, not 5× Small).
     *
     * @param totalSp character total SP **including** unallocated (injector tier base).
     */
    fun injectorMixNeeded(
        gapSp: Long,
        totalSp: Long,
    ): SkillInjectorMix {
        if (gapSp <= 0L) return SkillInjectorMix.ZERO
        var remaining = gapSp
        var currentTotal = totalSp.coerceAtLeast(0L)
        var largeCount = 0
        var smallCount = 0
        var steps = 0
        while (
            remaining > 0L &&
            steps < SkillInjectorConfig.MAX_INJECTORS_SIMULATED
        ) {
            steps++
            val largeYield = yieldAt(currentTotal, SkillInjectorSize.LARGE)
            if (largeYield > 0L && remaining >= largeYield) {
                remaining -= largeYield
                currentTotal += largeYield
                largeCount++
                continue
            }
            // Remainder is less than one Large yield at this tier.
            if (largeYield > 0L) {
                val smallYield = yieldAt(currentTotal, SkillInjectorSize.SMALL)
                if (smallYield > 0L) {
                    val smallsForRemainder = ceilDiv(remaining, smallYield)
                    if (smallsForRemainder >= SkillInjectorConfig.SMALLS_PER_LARGE) {
                        largeCount++
                        return SkillInjectorMix(
                            largeCount = largeCount,
                            smallCount = 0,
                        )
                    }
                }
            }
            while (
                remaining > 0L &&
                steps < SkillInjectorConfig.MAX_INJECTORS_SIMULATED
            ) {
                steps++
                val smallYield = yieldAt(currentTotal, SkillInjectorSize.SMALL)
                if (smallYield <= 0L) break
                remaining -= smallYield
                currentTotal += smallYield
                smallCount++
            }
            break
        }
        return SkillInjectorMix(
            largeCount = largeCount,
            smallCount = smallCount,
        )
    }

    private fun ceilDiv(numerator: Long, denominator: Long): Int {
        if (denominator <= 0L) return 0
        return ((numerator + denominator - 1L) / denominator).toInt()
    }

    fun yieldAt(totalSp: Long, size: SkillInjectorSize): Long {
        val sp = totalSp.coerceAtLeast(0L)
        return when (size) {
            SkillInjectorSize.LARGE -> when {
                sp < SkillInjectorConfig.TIER_THRESHOLD_5M ->
                    SkillInjectorConfig.LARGE_YIELD_BELOW_5M
                sp < SkillInjectorConfig.TIER_THRESHOLD_50M ->
                    SkillInjectorConfig.LARGE_YIELD_5M_TO_50M
                sp < SkillInjectorConfig.TIER_THRESHOLD_80M ->
                    SkillInjectorConfig.LARGE_YIELD_50M_TO_80M
                else -> SkillInjectorConfig.LARGE_YIELD_ABOVE_80M
            }
            SkillInjectorSize.SMALL -> when {
                sp < SkillInjectorConfig.TIER_THRESHOLD_5M ->
                    SkillInjectorConfig.SMALL_YIELD_BELOW_5M
                sp < SkillInjectorConfig.TIER_THRESHOLD_50M ->
                    SkillInjectorConfig.SMALL_YIELD_5M_TO_50M
                sp < SkillInjectorConfig.TIER_THRESHOLD_80M ->
                    SkillInjectorConfig.SMALL_YIELD_50M_TO_80M
                else -> SkillInjectorConfig.SMALL_YIELD_ABOVE_80M
            }
        }
    }
}

/** Combined Large + Small counts to cover a queue SP gap (large-first). */
internal data class SkillInjectorMix(
    val largeCount: Int,
    val smallCount: Int,
) {
    companion object {
        val ZERO = SkillInjectorMix(largeCount = 0, smallCount = 0)
    }
}

/** Character totals from ESI `/characters/{id}/skills`. */
data class CharacterSkillPoints(
    val characterId: Long,
    val totalSp: Long = 0L,
    val unallocatedSp: Long = 0L,
) {
    /**
     * Injector yield tiers use allocated + unallocated SP
     * (CCP: “overall skill points” at time of use).
     */
    val totalSpIncludingUnallocated: Long
        get() = totalSp + unallocatedSp.coerceAtLeast(0L)

    companion object {
        fun empty(characterId: Long): CharacterSkillPoints =
            CharacterSkillPoints(characterId = characterId)
    }
}

/** Catalog groups plus skill-point totals from the same ESI `/skills` call. */
data class SkillCatalogLoadResult(
    val groups: List<SkillCatalogGroup>,
    val skillPoints: CharacterSkillPoints,
)
