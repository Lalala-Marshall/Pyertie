package com.marshall.pyerite.characterSkillsModule.model

import androidx.annotation.DrawableRes
import com.marshall.pyerite.R
import com.marshall.pyerite.localization.LocalizableName
import kotlin.math.pow

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
    /**
     * ESI skill queue targets: skill type id → highest `finished_level` in queue.
     * Example: L1 done, queueing L2/L3/L4 → map value is 4.
     */
    val queuedTargetLevelsBySkillId: Map<Int, Int> = emptyMap(),
) {
    companion object {
        fun empty(characterId: Long): CharacterSkillQueueStatus = CharacterSkillQueueStatus(
            characterId = characterId,
            state = CharacterSkillQueueState.IDLE,
        )
    }
}

/**
 * Character neural attributes from ESI `/characters/{id}/attributes`.
 *
 * [nextRemapAvailableEpochMs] comes from `accrued_remap_cooldown_date`
 * (`null` = unknown / never remapped cooldown; `<= now` = remap available).
 */
data class CharacterAttributes(
    val characterId: Long,
    val perception: Int,
    val memory: Int,
    val willpower: Int,
    val intelligence: Int,
    val charisma: Int,
    val bonusRemaps: Int,
    val lastRemapEpochMs: Long? = null,
    val nextRemapAvailableEpochMs: Long? = null,
) {
    companion object {
        fun empty(characterId: Long): CharacterAttributes = CharacterAttributes(
            characterId = characterId,
            perception = 0,
            memory = 0,
            willpower = 0,
            intelligence = 0,
            charisma = 0,
            bonusRemaps = 0,
        )
    }
}

/** Filter chips on the skill catalog details page. */
enum class SkillCatalogFilter {
    ALL,
    COMPLETED,
    UNTRAINED,
    TRAINABLE,
}

/**
 * SDE / ESI constants for the Skills category catalog.
 * EVE SDE `categories.category_id` for Skills is 16.
 */
internal object SkillCatalogConfig {
    const val SKILLS_CATEGORY_ID = 16
    const val MAX_SKILL_LEVEL = 5
    const val SP_BASE = 250.0
    const val SP_LEVEL_BASE = 32.0
    const val PROGRESS_MIN = 0f
    const val PROGRESS_MAX = 1f

    /**
     * Total SP required to reach [level] (EVE formula is already cumulative):
     * `250 × skillTimeConstant × 32^((level - 1) / 2)`.
     */
    fun cumulativeSpToLevel(skillTimeConstant: Double, level: Int): Long {
        if (skillTimeConstant <= 0.0 || level < 1) return 0L
        return (
            SP_BASE * skillTimeConstant *
                SP_LEVEL_BASE.pow((level - 1) / 2.0)
            ).toLong()
    }

    fun cumulativeSpToMax(skillTimeConstant: Double): Long =
        cumulativeSpToLevel(skillTimeConstant, MAX_SKILL_LEVEL)
}

/**
 * Skill-group sidebar icons (same asset names / groupId map as Tritanium
 * `SkillGroupIconManager`). Drawables live under `res/drawable/skill_group_*.png`.
 */
internal object SkillGroupIcons {
    private val byGroupId: Map<Int, Int> = mapOf(
        255 to R.drawable.skill_group_gunnery,
        256 to R.drawable.skill_group_missiles,
        257 to R.drawable.skill_group_spaceshipcmd,
        258 to R.drawable.skill_group_fleetsupport,
        266 to R.drawable.skill_group_corpmgmt,
        268 to R.drawable.skill_group_production,
        269 to R.drawable.skill_group_rigging,
        270 to R.drawable.skill_group_science,
        272 to R.drawable.skill_group_electronicsystems,
        273 to R.drawable.skill_group_drones,
        274 to R.drawable.skill_group_trade,
        275 to R.drawable.skill_group_navigation,
        278 to R.drawable.skill_group_social,
        1209 to R.drawable.skill_group_shields,
        1210 to R.drawable.skill_group_armor,
        1213 to R.drawable.skill_group_targeting,
        1216 to R.drawable.skill_group_engineering,
        1217 to R.drawable.skill_group_scanning,
        1218 to R.drawable.skill_group_resourceprocessing,
        1220 to R.drawable.skill_group_neuralenhancement,
        1240 to R.drawable.skill_group_subsystems,
        1241 to R.drawable.skill_group_planetmgmt,
        1545 to R.drawable.skill_group_structuremgmt,
        4734 to R.drawable.skill_group_skinsequencing,
    )

    @DrawableRes
    fun drawableRes(groupId: Int): Int =
        byGroupId[groupId] ?: R.drawable.ic_character_skills
}

/**
 * One skill type inside a catalog group (per-skill SP / level for filter / hint counts).
 */
data class SkillCatalogSkill(
    val typeId: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val trainedSp: Long,
    val maxSp: Long,
    /** ESI `trained_skill_level` (0 when not present / not trained). */
    val trainedLevel: Int,
    /** Dogma `skillTimeConstant` (rank) for SP-to-level calculations. */
    val skillTimeConstant: Double,
    /**
     * True when this skill appears in ESI `/characters/{id}/skills`
     * (skillbook injected / absorbed onto the character).
     */
    val isInjected: Boolean,
) : LocalizableName {
    val isCompleted: Boolean
        get() = isInjected && trainedLevel >= SkillCatalogConfig.MAX_SKILL_LEVEL

    /** Never injected — 未吸收. */
    val isUninjected: Boolean
        get() = !isInjected
}

/**
 * One skill-group row on the catalog details page.
 * [trainedSp] is the sum of ESI `skillpoints_in_skill` for skills in the group.
 * Filters match when **any** skill in [skills] has that status.
 */
data class SkillCatalogGroup(
    val groupId: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val trainedSp: Long,
    val maxSp: Long,
    val skills: List<SkillCatalogSkill> = emptyList(),
) : LocalizableName {
    val skillCount: Int
        get() = skills.size

    /** Whole group finished (every skill trained to V). */
    val isCompleted: Boolean
        get() = skills.isNotEmpty() && skills.all { it.isCompleted }

    fun matchesFilter(
        filter: SkillCatalogFilter,
        queuedTargetLevelsBySkillId: Map<Int, Int> = emptyMap(),
    ): Boolean =
        when (filter) {
            SkillCatalogFilter.ALL -> true
            else -> skillsMatching(filter, queuedTargetLevelsBySkillId).isNotEmpty()
        }

    /**
     * Skills for the active catalog filter.
     * - 未吸收: never injected (not in ESI skills); queue entries are treated as injected
     * - 可训练: injected (or queued) but not at level V
     */
    fun skillsMatching(
        filter: SkillCatalogFilter,
        queuedTargetLevelsBySkillId: Map<Int, Int> = emptyMap(),
    ): List<SkillCatalogSkill> {
        val queuedIds = queuedTargetLevelsBySkillId.keys
        return when (filter) {
            SkillCatalogFilter.ALL -> skills
            SkillCatalogFilter.COMPLETED -> skills.filter { it.isCompleted }
            SkillCatalogFilter.UNTRAINED -> skills.filter {
                it.isUninjected && it.typeId !in queuedIds
            }
            SkillCatalogFilter.TRAINABLE -> skills.filter {
                !it.isCompleted && (it.isInjected || it.typeId in queuedIds)
            }
        }
    }

    fun matchingSkillCount(
        filter: SkillCatalogFilter,
        queuedTargetLevelsBySkillId: Map<Int, Int> = emptyMap(),
    ): Int = skillsMatching(filter, queuedTargetLevelsBySkillId).size

    fun matchingTrainedSp(
        filter: SkillCatalogFilter,
        queuedTargetLevelsBySkillId: Map<Int, Int> = emptyMap(),
    ): Long = skillsMatching(filter, queuedTargetLevelsBySkillId).sumOf { it.trainedSp }

    /** Distinct matching skills that also appear in the training queue. */
    fun queuedMatchingSkillCount(
        filter: SkillCatalogFilter,
        queuedTargetLevelsBySkillId: Map<Int, Int>,
    ): Int {
        if (filter == SkillCatalogFilter.UNTRAINED || queuedTargetLevelsBySkillId.isEmpty()) {
            return 0
        }
        return skillsMatching(filter, queuedTargetLevelsBySkillId)
            .count { it.typeId in queuedTargetLevelsBySkillId }
    }

    /**
     * SP required to reach each matching queued skill's highest queued level
     * (cumulative 1→N), summed for the sky-blue progress segment.
     */
    fun queuedMatchingRequiredSp(
        filter: SkillCatalogFilter,
        queuedTargetLevelsBySkillId: Map<Int, Int>,
    ): Long {
        if (filter == SkillCatalogFilter.UNTRAINED || queuedTargetLevelsBySkillId.isEmpty()) {
            return 0L
        }
        return skillsMatching(filter, queuedTargetLevelsBySkillId).sumOf { skill ->
            val targetLevel = queuedTargetLevelsBySkillId[skill.typeId] ?: return@sumOf 0L
            SkillCatalogConfig.cumulativeSpToLevel(skill.skillTimeConstant, targetLevel)
        }
    }

    /** Trained SP of skills that are not part of the sky-blue queue segment. */
    fun learnedSpExcludingQueued(
        filter: SkillCatalogFilter,
        queuedTargetLevelsBySkillId: Map<Int, Int>,
    ): Long {
        val skyIds = skillsMatching(filter, queuedTargetLevelsBySkillId)
            .map { it.typeId }
            .filter { it in queuedTargetLevelsBySkillId }
            .toSet()
        return skills.filter { it.typeId !in skyIds }.sumOf { it.trainedSp }
    }
}
