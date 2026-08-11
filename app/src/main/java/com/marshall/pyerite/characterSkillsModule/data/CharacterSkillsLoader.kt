package com.marshall.pyerite.characterSkillsModule.data

import com.marshall.pyerite.characterSkillsModule.model.AttributeRemapConfig
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillPoints
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueState
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.characterSkillsModule.model.ImplantAttributeBonuses
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogLoadResult
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.characterSkillsModule.model.SkillQueueHeadTraining
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.model.EsiCharacterAttributesDto
import com.marshall.pyerite.esiModule.model.EsiSkillQueueEntryDto
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.sdeModule.room.RoomProvider
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Loads skill-queue summary, attributes, and skill-catalog groups.
 */
internal class CharacterSkillsLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
    private val roomProvider: RoomProvider,
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

    /**
     * Active implant bonuses for remap optimization (ESI implants + SDE dogma 175–179).
     * Returns [ImplantAttributeBonuses.ZERO] when the call fails or no implants are fitted.
     */
    suspend fun loadImplantAttributeBonuses(characterId: Long): ImplantAttributeBonuses =
        withContext(Dispatchers.IO) {
            val typeIds = runCatching {
                tokenManager.executeWithAuthRetry(characterId) { auth ->
                    characterApi.fetchImplants(characterId, auth)
                }
            }.getOrNull().orEmpty()
            if (typeIds.isEmpty()) return@withContext ImplantAttributeBonuses.ZERO

            val dogmaDao = roomProvider.getDatabase().dogmaDao()
            val implantAttrIds = AttributeRemapConfig.IMPLANT_ATTRIBUTE_IDS.toSet()
            var perception = 0
            var memory = 0
            var willpower = 0
            var intelligence = 0
            var charisma = 0
            for (typeId in typeIds) {
                val details = runCatching {
                    dogmaDao.getTypeAttributeDetails(typeId)
                }.getOrNull().orEmpty()
                for (detail in details) {
                    if (detail.attributeId !in implantAttrIds) continue
                    val bonus = detail.value?.toInt() ?: continue
                    when (detail.attributeId) {
                        AttributeRemapConfig.IMPLANT_ATTR_PERCEPTION ->
                            perception = max(perception, bonus)
                        AttributeRemapConfig.IMPLANT_ATTR_MEMORY ->
                            memory = max(memory, bonus)
                        AttributeRemapConfig.IMPLANT_ATTR_WILLPOWER ->
                            willpower = max(willpower, bonus)
                        AttributeRemapConfig.IMPLANT_ATTR_INTELLIGENCE ->
                            intelligence = max(intelligence, bonus)
                        AttributeRemapConfig.IMPLANT_ATTR_CHARISMA ->
                            charisma = max(charisma, bonus)
                    }
                }
            }
            ImplantAttributeBonuses(
                perception = perception,
                memory = memory,
                willpower = willpower,
                intelligence = intelligence,
                charisma = charisma,
            )
        }

    suspend fun loadCatalog(characterId: Long): SkillCatalogLoadResult =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val skillsDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            characterApi.fetchSkills(characterId, auth)
                        }
                    }.getOrNull()
                }
                val db = roomProvider.getDatabase()
                val groupsDeferred = async {
                    db.groupDao().getGroupsByCategory(SkillCatalogConfig.SKILLS_CATEGORY_ID)
                }
                val catalogTypesDeferred = async {
                    db.skillDao().getSkillCatalogTypes(SkillCatalogConfig.SKILLS_CATEGORY_ID)
                }

                val skillsDto = skillsDeferred.await()
                val esiSkills = skillsDto?.skills.orEmpty()
                val skillPoints = CharacterSkillPoints(
                    characterId = characterId,
                    totalSp = skillsDto?.totalSp ?: 0L,
                    unallocatedSp = skillsDto?.unallocatedSp ?: 0L,
                )
                val trainedSpByTypeId = esiSkills.associate { it.skillId to it.skillpointsInSkill }
                val trainedLevelByTypeId = esiSkills.associate { it.skillId to it.trainedSkillLevel }
                val injectedSkillIds = esiSkills.map { it.skillId }.toSet()
                val groups = groupsDeferred.await()
                    .filter { it.published != false }
                    .associateBy { it.id }
                val typesByGroup = catalogTypesDeferred.await().groupBy { it.groupId }

                val catalogGroups = typesByGroup.mapNotNull { (groupId, types) ->
                    val group = groups[groupId] ?: return@mapNotNull null
                    val skills = types.map { type ->
                        val maxSp = SkillCatalogConfig.cumulativeSpToMax(type.skillTimeConstant)
                        val injected = type.typeId in injectedSkillIds
                        SkillCatalogSkill(
                            typeId = type.typeId,
                            name = type.name,
                            zhName = type.zhName,
                            enName = type.enName,
                            trainedSp = trainedSpByTypeId[type.typeId] ?: 0L,
                            maxSp = maxSp,
                            trainedLevel = trainedLevelByTypeId[type.typeId] ?: 0,
                            skillTimeConstant = type.skillTimeConstant,
                            primaryAttributeTypeId = type.primaryAttributeId
                                ?.toInt()
                                ?.takeIf { it > 0 },
                            secondaryAttributeTypeId = type.secondaryAttributeId
                                ?.toInt()
                                ?.takeIf { it > 0 },
                            isInjected = injected,
                            iconFilename = type.iconFilename,
                        )
                    }
                    SkillCatalogGroup(
                        groupId = groupId,
                        name = group.name,
                        zhName = group.zhName,
                        enName = group.enName,
                        trainedSp = skills.sumOf { it.trainedSp },
                        maxSp = skills.sumOf { it.maxSp },
                        skills = skills,
                    )
                }.sortedBy { it.groupId }

                SkillCatalogLoadResult(
                    groups = catalogGroups,
                    skillPoints = skillPoints,
                )
            }
        }

    private fun mapQueue(
        characterId: Long,
        entries: List<EsiSkillQueueEntryDto>,
    ): CharacterSkillQueueStatus {
        if (entries.isEmpty()) {
            return CharacterSkillQueueStatus.empty(characterId)
        }
        val ordered = entries.sortedBy { it.queuePosition }
        val queuedTargetLevelsBySkillId = ordered
            .groupBy { it.skillId }
            .mapValues { (_, skillEntries) -> skillEntries.maxOf { it.finishedLevel } }
        val queuedEntries = ordered.map { entry ->
            SkillQueueHeadTraining(
                skillId = entry.skillId,
                finishedLevel = entry.finishedLevel,
                trainingStartSp = entry.trainingStartSp,
                levelStartSp = entry.levelStartSp,
                levelEndSp = entry.levelEndSp,
                startAtEpochMs = entry.startDate?.let { parseEsiDateMillis(it) },
                finishAtEpochMs = entry.finishDate?.let { parseEsiDateMillis(it) },
            )
        }
        val queueHead = queuedEntries.firstOrNull()
        val finishTimes = queuedEntries.mapNotNull { it.finishAtEpochMs }
        return if (finishTimes.isNotEmpty()) {
            CharacterSkillQueueStatus(
                characterId = characterId,
                state = CharacterSkillQueueState.TRAINING,
                trainingFinishAtEpochMs = finishTimes,
                queuedTargetLevelsBySkillId = queuedTargetLevelsBySkillId,
                queuedEntries = queuedEntries,
                queueHead = queueHead,
                activeTrainingSkillId = queueHead?.skillId,
                activeTrainingLevel = queueHead?.finishedLevel,
            )
        } else {
            CharacterSkillQueueStatus(
                characterId = characterId,
                state = CharacterSkillQueueState.PAUSED,
                pausedSkillCount = ordered.size,
                pausedRemainingSeconds = null,
                queuedTargetLevelsBySkillId = queuedTargetLevelsBySkillId,
                queuedEntries = queuedEntries,
                queueHead = queueHead,
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
