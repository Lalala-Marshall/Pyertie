package com.marshall.pyerite.characterClonesModule.model

/**
 * Jump-clone cooldown: base 24h, −1h per active level of Infomorph Synchronizing.
 */
internal object JumpCloneConfig {
    /** SDE / ESI type id for Infomorph Synchronizing. */
    const val INFOMORPH_SYNCHRONIZING_TYPE_ID = 33399

    const val BASE_COOLDOWN_HOURS = 24
    const val COOLDOWN_REDUCTION_HOURS_PER_LEVEL = 1
    const val MIN_COOLDOWN_HOURS = 0
    const val UI_TICK_MS = 1_000L

    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_HOUR = 3_600L
    private const val MILLIS_PER_HOUR = SECONDS_PER_HOUR * MILLIS_PER_SECOND

    fun cooldownMillis(infomorphSynchronizingLevel: Int): Long {
        val hours = (
            BASE_COOLDOWN_HOURS -
                COOLDOWN_REDUCTION_HOURS_PER_LEVEL * infomorphSynchronizingLevel.coerceAtLeast(0)
            ).coerceAtLeast(MIN_COOLDOWN_HOURS)
        return hours * MILLIS_PER_HOUR
    }

    /**
     * Epoch ms when the next jump clone is allowed.
     * `0` means available immediately when [lastCloneJumpEpochMs] is absent.
     */
    fun nextAvailableEpochMs(
        lastCloneJumpEpochMs: Long?,
        infomorphSynchronizingLevel: Int,
    ): Long {
        val lastJump = lastCloneJumpEpochMs ?: return 0L
        return lastJump + cooldownMillis(infomorphSynchronizingLevel)
    }
}

/**
 * Clone jump availability for the selected character.
 * [nextCloneJumpEpochMs] `null` = unknown; `<= now` = available now.
 */
data class CharacterCloneStatus(
    val characterId: Long,
    val nextCloneJumpEpochMs: Long?,
)
