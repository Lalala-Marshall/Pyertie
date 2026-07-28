package com.marshall.pyerite.characterSkillsModule.model

/**
 * Main-page / skills summary for a character's ESI skill queue.
 */
enum class CharacterSkillQueueState {
    /** Empty queue from ESI. */
    IDLE,

    /** Queue present but start/finish dates omitted (training paused). */
    PAUSED,

    /** At least one entry has a projected finish time. */
    TRAINING,
}

internal object CharacterSkillQueueConfig {
    const val UI_TICK_MS = 1_000L
    const val MILLIS_PER_SECOND = 1_000L
}

/**
 * Compact skill-queue status for the home-page row (and future skills page seed).
 *
 * When [state] is [CharacterSkillQueueState.TRAINING], [trainingFinishAtEpochMs] holds
 * each queued skill's finish time (ESI order). The UI filters by `now` for live count
 * and remaining duration. [pausedSkillCount] is used when paused.
 */
data class CharacterSkillQueueStatus(
    val characterId: Long,
    val state: CharacterSkillQueueState,
    val trainingFinishAtEpochMs: List<Long> = emptyList(),
    val pausedSkillCount: Int = 0,
    /** Optional static remaining-seconds estimate while paused (no live countdown). */
    val pausedRemainingSeconds: Long? = null,
) {
    companion object {
        fun empty(characterId: Long): CharacterSkillQueueStatus = CharacterSkillQueueStatus(
            characterId = characterId,
            state = CharacterSkillQueueState.IDLE,
        )
    }
}
