package com.marshall.pyerite.characterClonesModule.data

import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import com.marshall.pyerite.characterClonesModule.model.JumpCloneConfig
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.model.EsiCharacterSkillsDto
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Loads jump-clone cooldown for the main-page clone status row.
 */
internal class CharacterClonesLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
) {
    suspend fun load(characterId: Long): CharacterCloneStatus = withContext(Dispatchers.IO) {
        coroutineScope {
            val clonesDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(characterId) { auth ->
                        characterApi.fetchClones(characterId, auth)
                    }
                }.getOrNull()
            }
            val skillsDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(characterId) { auth ->
                        characterApi.fetchSkills(characterId, auth)
                    }
                }.getOrNull()
            }

            val clones = clonesDeferred.await()
            val nextCloneJumpEpochMs = clones?.let { dto ->
                JumpCloneConfig.nextAvailableEpochMs(
                    lastCloneJumpEpochMs = parseEsiDateMillis(dto.lastCloneJumpDate),
                    infomorphSynchronizingLevel = skillsDeferred.await().infomorphSynchronizingLevel(),
                )
            }

            CharacterCloneStatus(
                characterId = characterId,
                nextCloneJumpEpochMs = nextCloneJumpEpochMs,
            )
        }
    }

    private fun EsiCharacterSkillsDto?.infomorphSynchronizingLevel(): Int {
        this ?: return 0
        return skills
            .firstOrNull { it.skillId == JumpCloneConfig.INFOMORPH_SYNCHRONIZING_TYPE_ID }
            ?.activeSkillLevel
            ?: 0
    }
}
