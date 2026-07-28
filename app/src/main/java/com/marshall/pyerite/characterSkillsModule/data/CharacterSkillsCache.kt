package com.marshall.pyerite.characterSkillsModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueState
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.infra.network.PyeriteJson
import kotlinx.serialization.Serializable

/**
 * Disk cache of non-secret [CharacterSkillQueueStatus] for instant main-page hint
 * on cold start. Never stores tokens.
 */
internal class CharacterSkillsCache(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(characterId: Long): CharacterSkillQueueStatus? {
        val raw = prefs.getString(keyFor(characterId), null) ?: return null
        return runCatching {
            PyeriteJson.decodeFromString<CachedCharacterSkillQueueStatus>(raw).toModel()
        }.getOrNull()
    }

    fun save(status: CharacterSkillQueueStatus) {
        val encoded = PyeriteJson.encodeToString(CachedCharacterSkillQueueStatus.from(status))
        prefs.edit { putString(keyFor(status.characterId), encoded) }
    }

    private fun keyFor(characterId: Long): String = "$KEY_PREFIX$characterId"

    private companion object {
        const val PREFS_NAME = "pyerite_character_skills_cache"
        const val KEY_PREFIX = "skills_"
    }
}

@Serializable
private data class CachedCharacterSkillQueueStatus(
    val characterId: Long,
    val state: String,
    val trainingFinishAtEpochMs: List<Long> = emptyList(),
    val pausedSkillCount: Int = 0,
    val pausedRemainingSeconds: Long? = null,
) {
    fun toModel(): CharacterSkillQueueStatus = CharacterSkillQueueStatus(
        characterId = characterId,
        state = runCatching { CharacterSkillQueueState.valueOf(state) }
            .getOrDefault(CharacterSkillQueueState.IDLE),
        trainingFinishAtEpochMs = trainingFinishAtEpochMs,
        pausedSkillCount = pausedSkillCount,
        pausedRemainingSeconds = pausedRemainingSeconds,
    )

    companion object {
        fun from(status: CharacterSkillQueueStatus): CachedCharacterSkillQueueStatus =
            CachedCharacterSkillQueueStatus(
                characterId = status.characterId,
                state = status.state.name,
                trainingFinishAtEpochMs = status.trainingFinishAtEpochMs,
                pausedSkillCount = status.pausedSkillCount,
                pausedRemainingSeconds = status.pausedRemainingSeconds,
            )
    }
}
