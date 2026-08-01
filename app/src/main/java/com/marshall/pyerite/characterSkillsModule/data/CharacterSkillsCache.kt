package com.marshall.pyerite.characterSkillsModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueState
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.characterSkillsModule.model.SkillQueueHeadTraining
import com.marshall.pyerite.infra.network.PyeriteJson
import kotlinx.serialization.Serializable

/**
 * Disk cache of non-secret skill-queue / attributes payloads for instant UI paint.
 * Never stores tokens.
 */
internal class CharacterSkillsCache(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(characterId: Long): CharacterSkillQueueStatus? {
        val raw = prefs.getString(queueKeyFor(characterId), null) ?: return null
        return runCatching {
            PyeriteJson.decodeFromString<CachedCharacterSkillQueueStatus>(raw).toModel()
        }.getOrNull()
    }

    fun save(status: CharacterSkillQueueStatus) {
        val encoded = PyeriteJson.encodeToString(CachedCharacterSkillQueueStatus.from(status))
        prefs.edit { putString(queueKeyFor(status.characterId), encoded) }
    }

    fun getAttributes(characterId: Long): CharacterAttributes? {
        val raw = prefs.getString(attributesKeyFor(characterId), null) ?: return null
        return runCatching {
            PyeriteJson.decodeFromString<CachedCharacterAttributes>(raw).toModel()
        }.getOrNull()
    }

    fun saveAttributes(attributes: CharacterAttributes) {
        val encoded = PyeriteJson.encodeToString(CachedCharacterAttributes.from(attributes))
        prefs.edit { putString(attributesKeyFor(attributes.characterId), encoded) }
    }

    private fun queueKeyFor(characterId: Long): String = "$QUEUE_KEY_PREFIX$characterId"

    private fun attributesKeyFor(characterId: Long): String = "$ATTRIBUTES_KEY_PREFIX$characterId"

    private companion object {
        const val PREFS_NAME = "pyerite_character_skills_cache"
        const val QUEUE_KEY_PREFIX = "skills_"
        const val ATTRIBUTES_KEY_PREFIX = "attributes_"
    }
}

@Serializable
private data class CachedQueuedSkillTarget(
    val skillId: Int,
    val targetLevel: Int,
)

@Serializable
private data class CachedQueueHeadTraining(
    val skillId: Int,
    val finishedLevel: Int,
    val trainingStartSp: Long? = null,
    val levelStartSp: Long? = null,
    val levelEndSp: Long? = null,
    val startAtEpochMs: Long? = null,
    val finishAtEpochMs: Long? = null,
) {
    fun toModel(): SkillQueueHeadTraining = SkillQueueHeadTraining(
        skillId = skillId,
        finishedLevel = finishedLevel,
        trainingStartSp = trainingStartSp,
        levelStartSp = levelStartSp,
        levelEndSp = levelEndSp,
        startAtEpochMs = startAtEpochMs,
        finishAtEpochMs = finishAtEpochMs,
    )

    companion object {
        fun from(head: SkillQueueHeadTraining): CachedQueueHeadTraining =
            CachedQueueHeadTraining(
                skillId = head.skillId,
                finishedLevel = head.finishedLevel,
                trainingStartSp = head.trainingStartSp,
                levelStartSp = head.levelStartSp,
                levelEndSp = head.levelEndSp,
                startAtEpochMs = head.startAtEpochMs,
                finishAtEpochMs = head.finishAtEpochMs,
            )
    }
}

@Serializable
private data class CachedCharacterSkillQueueStatus(
    val characterId: Long,
    val state: String,
    val trainingFinishAtEpochMs: List<Long> = emptyList(),
    val pausedSkillCount: Int = 0,
    val pausedRemainingSeconds: Long? = null,
    val queuedSkillTargets: List<CachedQueuedSkillTarget> = emptyList(),
    val queueHead: CachedQueueHeadTraining? = null,
    val activeTrainingSkillId: Int? = null,
    val activeTrainingLevel: Int? = null,
) {
    fun toModel(): CharacterSkillQueueStatus = CharacterSkillQueueStatus(
        characterId = characterId,
        state = runCatching { CharacterSkillQueueState.valueOf(state) }
            .getOrDefault(CharacterSkillQueueState.IDLE),
        trainingFinishAtEpochMs = trainingFinishAtEpochMs,
        pausedSkillCount = pausedSkillCount,
        pausedRemainingSeconds = pausedRemainingSeconds,
        queuedTargetLevelsBySkillId = queuedSkillTargets.associate { it.skillId to it.targetLevel },
        queuedSkillIdsInOrder = queuedSkillTargets.map { it.skillId },
        queueHead = queueHead?.toModel(),
        activeTrainingSkillId = activeTrainingSkillId,
        activeTrainingLevel = activeTrainingLevel,
    )

    companion object {
        fun from(status: CharacterSkillQueueStatus): CachedCharacterSkillQueueStatus =
            CachedCharacterSkillQueueStatus(
                characterId = status.characterId,
                state = status.state.name,
                trainingFinishAtEpochMs = status.trainingFinishAtEpochMs,
                pausedSkillCount = status.pausedSkillCount,
                pausedRemainingSeconds = status.pausedRemainingSeconds,
                queuedSkillTargets = status.queuedSkillIdsInOrder.mapNotNull { skillId ->
                    val level = status.queuedTargetLevelsBySkillId[skillId] ?: return@mapNotNull null
                    CachedQueuedSkillTarget(skillId = skillId, targetLevel = level)
                }.ifEmpty {
                    status.queuedTargetLevelsBySkillId.map { (skillId, level) ->
                        CachedQueuedSkillTarget(skillId = skillId, targetLevel = level)
                    }
                },
                queueHead = status.queueHead?.let(CachedQueueHeadTraining::from),
                activeTrainingSkillId = status.activeTrainingSkillId,
                activeTrainingLevel = status.activeTrainingLevel,
            )
    }
}

@Serializable
private data class CachedCharacterAttributes(
    val characterId: Long,
    val perception: Int = 0,
    val memory: Int = 0,
    val willpower: Int = 0,
    val intelligence: Int = 0,
    val charisma: Int = 0,
    val bonusRemaps: Int = 0,
    val lastRemapEpochMs: Long? = null,
    val nextRemapAvailableEpochMs: Long? = null,
) {
    fun toModel(): CharacterAttributes = CharacterAttributes(
        characterId = characterId,
        perception = perception,
        memory = memory,
        willpower = willpower,
        intelligence = intelligence,
        charisma = charisma,
        bonusRemaps = bonusRemaps,
        lastRemapEpochMs = lastRemapEpochMs,
        nextRemapAvailableEpochMs = nextRemapAvailableEpochMs,
    )

    companion object {
        fun from(attributes: CharacterAttributes): CachedCharacterAttributes =
            CachedCharacterAttributes(
                characterId = attributes.characterId,
                perception = attributes.perception,
                memory = attributes.memory,
                willpower = attributes.willpower,
                intelligence = attributes.intelligence,
                charisma = attributes.charisma,
                bonusRemaps = attributes.bonusRemaps,
                lastRemapEpochMs = attributes.lastRemapEpochMs,
                nextRemapAvailableEpochMs = attributes.nextRemapAvailableEpochMs,
            )
    }
}
