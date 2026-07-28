package com.marshall.pyerite.characterSkillsModule.data

import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueState
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.model.EsiSkillQueueEntryDto
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads skill-queue summary for the home-page skills row.
 */
internal class CharacterSkillsLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
) {
    suspend fun load(characterId: Long): CharacterSkillQueueStatus = withContext(Dispatchers.IO) {
        val entries = tokenManager.executeWithAuthRetry(characterId) { auth ->
            characterApi.fetchSkillQueue(characterId, auth)
        }
        mapQueue(characterId, entries)
    }

    private fun mapQueue(
        characterId: Long,
        entries: List<EsiSkillQueueEntryDto>,
    ): CharacterSkillQueueStatus {
        if (entries.isEmpty()) {
            return CharacterSkillQueueStatus.empty(characterId)
        }
        val ordered = entries.sortedBy { it.queuePosition }
        val finishTimes = ordered.mapNotNull { entry ->
            entry.finishDate?.let { parseEsiDateMillis(it) }
        }
        return if (finishTimes.isNotEmpty()) {
            CharacterSkillQueueStatus(
                characterId = characterId,
                state = CharacterSkillQueueState.TRAINING,
                trainingFinishAtEpochMs = finishTimes,
            )
        } else {
            CharacterSkillQueueStatus(
                characterId = characterId,
                state = CharacterSkillQueueState.PAUSED,
                pausedSkillCount = ordered.size,
                // ESI omits finish dates while paused; wall-clock remaining is unknown.
                pausedRemainingSeconds = null,
            )
        }
    }
}
