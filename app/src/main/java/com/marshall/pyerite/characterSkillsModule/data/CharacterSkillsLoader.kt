package com.marshall.pyerite.characterSkillsModule.data

import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueState
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.model.EsiCharacterAttributesDto
import com.marshall.pyerite.esiModule.model.EsiSkillQueueEntryDto
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads skill-queue summary and character attributes for the skills feature.
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

    suspend fun loadAttributes(characterId: Long): CharacterAttributes =
        withContext(Dispatchers.IO) {
            val dto = tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchAttributes(characterId, auth)
            }
            mapAttributes(characterId, dto)
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

    private fun mapAttributes(
        characterId: Long,
        dto: EsiCharacterAttributesDto,
    ): CharacterAttributes = CharacterAttributes(
        characterId = characterId,
        perception = dto.perception,
        memory = dto.memory,
        willpower = dto.willpower,
        intelligence = dto.intelligence,
        charisma = dto.charisma,
        bonusRemaps = dto.bonusRemaps,
        lastRemapEpochMs = parseEsiDateMillis(dto.lastRemapDate),
        nextRemapAvailableEpochMs = parseEsiDateMillis(dto.accruedRemapCooldownDate),
    )
}
