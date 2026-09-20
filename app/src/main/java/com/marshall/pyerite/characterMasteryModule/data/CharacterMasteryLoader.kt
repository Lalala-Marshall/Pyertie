package com.marshall.pyerite.characterMasteryModule.data

import com.marshall.pyerite.characterMasteryModule.model.CharacterMasterySnapshot
import com.marshall.pyerite.characterMasteryModule.model.MasteryConfig
import com.marshall.pyerite.characterMasteryModule.model.MasteryGroup
import com.marshall.pyerite.characterMasteryModule.model.MasteryLevelState
import com.marshall.pyerite.characterMasteryModule.model.MasteryMetaGroup
import com.marshall.pyerite.characterMasteryModule.model.MasteryShip
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.sdeModule.room.RoomProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

internal class CharacterMasteryLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
    private val roomProvider: RoomProvider,
) {
    suspend fun loadSkills(characterId: Long): Map<Int, Int> = withContext(Dispatchers.IO) {
        val dto = tokenManager.executeWithAuthRetry(characterId) { auth ->
            characterApi.fetchSkills(characterId, auth)
        }
        dto.skills.associate { it.skillId to it.trainedSkillLevel }
    }

    suspend fun loadCatalog(trainedLevels: Map<Int, Int>): CharacterMasterySnapshot =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val db = roomProvider.getDatabase()
                val masteryDao = db.masteryDao()
                val masteriesDeferred = async { masteryDao.getAllMasteries() }
                val certSkillsDeferred = async { masteryDao.getAllCertificateSkills() }
                val shipTypesDeferred = async {
                    masteryDao.getMasteryShipTypes(MasteryConfig.SHIP_CATEGORY_ID)
                }
                val groupsDeferred = async {
                    masteryDao.getMasteryShipGroups(MasteryConfig.SHIP_CATEGORY_ID)
                }
                val requirementsDeferred = async { masteryDao.getMasteryTypeSkillRequirements() }
                val metaGroupsDeferred = async { db.metaGroupDao().getAllMetaGroups() }

                val requirementsByCert = certSkillsDeferred.await()
                    .groupBy { it.certificateId }
                    .mapValues { (_, rows) ->
                        rows.map { row ->
                            CertificateSkillRequirement(
                                skillId = row.skillId,
                                tierLevels = listOf(
                                    row.basic,
                                    row.standard,
                                    row.improved,
                                    row.advanced,
                                    row.elite,
                                ),
                            )
                        }
                    }
                val certsByTypeAndLevel = HashMap<Int, MutableMap<Int, MutableList<Int>>>()
                for (row in masteriesDeferred.await()) {
                    val byLevel = certsByTypeAndLevel.getOrPut(row.typeId) { HashMap() }
                    byLevel.getOrPut(row.masteryLevel) { mutableListOf() }.add(row.certificateId)
                }
                val flyRequirements = requirementsDeferred.await()
                    .groupBy { it.typeId }
                    .mapValues { (_, rows) ->
                        rows.map { it.requiredSkillId to (it.requiredSkillLevel ?: 0) }
                    }
                val certLevels = MasteryEvaluator.certificateLevels(trainedLevels, requirementsByCert)

                val shipsByGroup = shipTypesDeferred.await().mapNotNull { type ->
                    val groupId = type.groupId ?: return@mapNotNull null
                    if (type.typeId !in certsByTypeAndLevel) return@mapNotNull null
                    val canFly = flyRequirements[type.typeId].orEmpty().all { (skillId, level) ->
                        (trainedLevels[skillId] ?: 0) >= level
                    }
                    val state = if (!canFly) {
                        MasteryLevelState.Locked
                    } else {
                        val level = MasteryEvaluator.masteryLevel(
                            typeId = type.typeId,
                            certLevels = certLevels,
                            certsByTypeAndLevel = certsByTypeAndLevel,
                        ) ?: MasteryConfig.MIN_LEVEL
                        MasteryLevelState.Level(level)
                    }
                    MasteryShip(
                        typeId = type.typeId,
                        groupId = groupId,
                        name = type.name,
                        zhName = type.zhName,
                        enName = type.enName,
                        iconFilename = type.iconFilename,
                        metaGroupId = type.metaGroupId,
                        published = type.published == true,
                        state = state,
                    )
                }.groupBy { it.groupId }

                val groups = groupsDeferred.await().mapNotNull { group ->
                    val ships = shipsByGroup[group.id].orEmpty()
                    if (ships.isEmpty()) return@mapNotNull null
                    MasteryGroup(
                        groupId = group.id,
                        name = group.name,
                        zhName = group.zhName,
                        enName = group.enName,
                        iconFilename = group.iconFilename,
                        ships = ships,
                    )
                }
                CharacterMasterySnapshot(
                    groups = groups,
                    metaGroups = metaGroupsDeferred.await().map {
                        MasteryMetaGroup(id = it.id, name = it.name)
                    },
                )
            }
        }
}
