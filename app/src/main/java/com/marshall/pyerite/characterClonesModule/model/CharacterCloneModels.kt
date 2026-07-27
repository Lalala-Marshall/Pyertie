package com.marshall.pyerite.characterClonesModule.model

import com.marshall.pyerite.localization.LocalizableName

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

/** ESI `location_type` wire values for clones / home station. */
internal object CloneLocationTypeApi {
    const val STATION = "station"
    const val STRUCTURE = "structure"
    const val SOLAR_SYSTEM = "solar_system"
}

/**
 * Clone jump availability and home / jump-clone details for a character.
 * [nextCloneJumpEpochMs] `null` = unknown; `<= now` = available now.
 */
data class CharacterCloneStatus(
    val characterId: Long,
    val nextCloneJumpEpochMs: Long?,
    val lastCloneJumpEpochMs: Long? = null,
    val lastStationChangeEpochMs: Long? = null,
    val homeLocationName: String? = null,
    val homeLocationIconFilename: String? = null,
    val activeImplants: List<ActiveImplantInfo> = emptyList(),
    /** Jump clones grouped by station / structure (or solar system). */
    val jumpCloneLocations: List<JumpCloneLocationGroup> = emptyList(),
) {
    companion object {
        fun empty(characterId: Long): CharacterCloneStatus = CharacterCloneStatus(
            characterId = characterId,
            nextCloneJumpEpochMs = null,
        )
    }
}

data class ActiveImplantInfo(
    val typeId: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
) : LocalizableName

/** One row per building (or system) that hosts one or more jump clones. */
data class JumpCloneLocationGroup(
    val locationId: Long,
    val locationType: String,
    val locationName: String?,
    val systemSecurityStatus: Double?,
    val iconFilename: String?,
    val cloneCount: Int,
    val implantCount: Int,
    val jumpCloneIds: List<Int> = emptyList(),
)
