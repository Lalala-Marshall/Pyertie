package com.marshall.pyerite.characterClonesModule.data

import com.marshall.pyerite.characterClonesModule.model.ActiveImplantInfo
import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import com.marshall.pyerite.characterClonesModule.model.CloneLocationTypeApi
import com.marshall.pyerite.characterClonesModule.model.JumpCloneConfig
import com.marshall.pyerite.characterClonesModule.model.JumpCloneLocationGroup
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.api.EsiUniverseApi
import com.marshall.pyerite.esiModule.data.EsiPublicDataSource
import com.marshall.pyerite.esiModule.model.EsiCharacterSkillsDto
import com.marshall.pyerite.esiModule.model.EsiCloneLocationDto
import com.marshall.pyerite.esiModule.model.EsiJumpCloneDto
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.localizedName
import com.marshall.pyerite.sdeModule.room.RoomProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Loads jump-clone status, home station, active implants, and jump-clone locations.
 */
internal class CharacterClonesLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
    private val universeApi: EsiUniverseApi,
    private val publicEsi: EsiPublicDataSource,
    private val roomProvider: RoomProvider,
    private val localeController: LocaleController,
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
            val implantsDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(characterId) { auth ->
                        characterApi.fetchImplants(characterId, auth)
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
                ?: error("Failed to load character clones")

            val lastCloneJumpEpochMs = parseEsiDateMillis(clones.lastCloneJumpDate)
            val lastStationChangeEpochMs = parseEsiDateMillis(clones.lastStationChangeDate)
            val nextCloneJumpEpochMs = JumpCloneConfig.nextAvailableEpochMs(
                lastCloneJumpEpochMs = lastCloneJumpEpochMs,
                infomorphSynchronizingLevel = skillsDeferred.await().infomorphSynchronizingLevel(),
            )
            val homeLocation = clones.homeLocation?.let { location ->
                resolveHomeLocation(characterId, location)
            }
            val activeImplants = implantsDeferred.await().orEmpty().map { typeId ->
                resolveActiveImplant(typeId)
            }
            val jumpCloneLocations = groupJumpClonesByLocation(characterId, clones.jumpClones)

            CharacterCloneStatus(
                characterId = characterId,
                nextCloneJumpEpochMs = nextCloneJumpEpochMs,
                lastCloneJumpEpochMs = lastCloneJumpEpochMs,
                lastStationChangeEpochMs = lastStationChangeEpochMs,
                homeLocationName = homeLocation?.name,
                homeLocationIconFilename = homeLocation?.iconFilename,
                activeImplants = activeImplants,
                jumpCloneLocations = jumpCloneLocations,
            )
        }
    }

    private suspend fun resolveActiveImplant(typeId: Int): ActiveImplantInfo {
        val dao = runCatching { roomProvider.getDatabase().sdeTypeDao() }.getOrNull()
        val displayRow = dao?.let { runCatching { it.getTypeDisplayName(typeId) }.getOrNull() }
        val entity = if (displayRow == null) {
            dao?.let { runCatching { it.getTypeById(typeId) }.getOrNull() }
        } else {
            null
        }
        val iconFilename = resolveTypeIconFilename(typeId)
            ?: entity?.iconFilename?.takeIf { it.isNotBlank() }
        if (displayRow != null) {
            return ActiveImplantInfo(
                typeId = typeId,
                name = displayRow.name,
                zhName = displayRow.zhName,
                enName = displayRow.enName,
                iconFilename = iconFilename,
            )
        }
        if (entity != null) {
            return ActiveImplantInfo(
                typeId = typeId,
                name = entity.name,
                zhName = entity.zhName,
                enName = entity.enName,
                iconFilename = iconFilename,
            )
        }
        val fromEsi = publicEsi.fetchTypeName(typeId)?.takeIf { it.isNotBlank() }
        return ActiveImplantInfo(
            typeId = typeId,
            name = fromEsi,
            zhName = null,
            enName = fromEsi,
            iconFilename = iconFilename,
        )
    }

    private suspend fun groupJumpClonesByLocation(
        characterId: Long,
        clones: List<EsiJumpCloneDto>,
    ): List<JumpCloneLocationGroup> {
        if (clones.isEmpty()) return emptyList()
        return clones
            .groupBy { CloneLocationKey(it.locationType, it.locationId) }
            .map { (key, group) ->
                val place = resolvePlace(
                    characterId = characterId,
                    location = EsiCloneLocationDto(
                        locationId = key.locationId,
                        locationType = key.locationType,
                    ),
                )
                JumpCloneLocationGroup(
                    locationId = key.locationId,
                    locationType = key.locationType,
                    locationName = place.name,
                    systemSecurityStatus = place.systemSecurityStatus,
                    iconFilename = place.typeId?.let { resolveTypeIconFilename(it) },
                    cloneCount = group.size,
                    implantCount = group.sumOf { it.implants.size },
                    jumpCloneIds = group.map { it.jumpCloneId },
                )
            }
            .sortedByDescending { it.systemSecurityStatus ?: Double.NEGATIVE_INFINITY }
    }

    private suspend fun resolveHomeLocation(
        characterId: Long,
        location: EsiCloneLocationDto,
    ): ResolvedHomeLocation {
        val place = resolvePlace(characterId, location)
        return ResolvedHomeLocation(
            name = place.name,
            iconFilename = place.typeId?.let { resolveTypeIconFilename(it) },
        )
    }

    private suspend fun resolvePlace(
        characterId: Long,
        location: EsiCloneLocationDto,
    ): ResolvedPlace = when (location.locationType) {
        CloneLocationTypeApi.STATION -> resolveStation(location.locationId)
        CloneLocationTypeApi.STRUCTURE -> resolveStructure(characterId, location.locationId)
        CloneLocationTypeApi.SOLAR_SYSTEM -> resolveSolarSystemPlace(location.locationId)
        else -> ResolvedPlace(name = null, typeId = null, systemSecurityStatus = null)
    }

    private suspend fun resolveStation(stationId: Long): ResolvedPlace {
        val fromSde = runCatching {
            roomProvider.getDatabase().mapDao().getStation(stationId)
        }.getOrNull()
        if (fromSde != null) {
            val solarSystemId = fromSde.solarSystemId?.toLong()
            return ResolvedPlace(
                name = fromSde.name?.takeIf { it.isNotBlank() },
                typeId = fromSde.typeId,
                systemSecurityStatus = resolveSystemSecurity(solarSystemId),
            )
        }
        val fromEsi = publicEsi.fetchStation(stationId)
        return ResolvedPlace(
            name = fromEsi?.name?.takeIf { it.isNotBlank() },
            typeId = fromEsi?.typeId,
            systemSecurityStatus = resolveSystemSecurity(fromEsi?.systemId),
        )
    }

    private suspend fun resolveStructure(characterId: Long, structureId: Long): ResolvedPlace {
        val structure = runCatching {
            tokenManager.executeWithAuthRetry(characterId) { auth ->
                universeApi.fetchStructure(structureId, auth)
            }
        }.getOrNull()
        return ResolvedPlace(
            name = structure?.name?.takeIf { it.isNotBlank() },
            typeId = structure?.typeId,
            systemSecurityStatus = resolveSystemSecurity(structure?.solarSystemId),
        )
    }

    private suspend fun resolveSolarSystemPlace(solarSystemId: Long): ResolvedPlace {
        val row = runCatching {
            roomProvider.getDatabase().mapDao().getSolarSystemLocation(solarSystemId)
        }.getOrNull()
        val name = localizedName(
            zh = row?.systemZhName,
            en = row?.systemEnName,
            fallback = row?.systemName,
            language = localeController.contentLanguage,
        ).takeIf { it.isNotBlank() }
            ?: publicEsi.fetchSolarSystemName(solarSystemId)?.takeIf { it.isNotBlank() }
        val security = row?.securityStatus
            ?: publicEsi.fetchSolarSystemSecurity(solarSystemId)
        return ResolvedPlace(
            name = name,
            typeId = null,
            systemSecurityStatus = security,
        )
    }

    private suspend fun resolveSystemSecurity(solarSystemId: Long?): Double? {
        solarSystemId ?: return null
        val row = runCatching {
            roomProvider.getDatabase().mapDao().getSolarSystemLocation(solarSystemId)
        }.getOrNull()
        return row?.securityStatus
            ?: publicEsi.fetchSolarSystemSecurity(solarSystemId)
    }

    private suspend fun resolveTypeIconFilename(typeId: Int): String? {
        val dao = runCatching { roomProvider.getDatabase().sdeTypeDao() }.getOrNull()
            ?: return null
        val fromColumn = runCatching { dao.getTypeIconFilename(typeId) }
            .getOrNull()?.takeIf { it.isNotBlank() }
        if (fromColumn != null) return fromColumn
        return runCatching { dao.getTypeById(typeId)?.iconFilename }
            .getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun EsiCharacterSkillsDto?.infomorphSynchronizingLevel(): Int {
        this ?: return 0
        return skills
            .firstOrNull { it.skillId == JumpCloneConfig.INFOMORPH_SYNCHRONIZING_TYPE_ID }
            ?.activeSkillLevel
            ?: 0
    }

    private data class CloneLocationKey(
        val locationType: String,
        val locationId: Long,
    )

    private data class ResolvedPlace(
        val name: String?,
        val typeId: Int?,
        val systemSecurityStatus: Double?,
    )

    private data class ResolvedHomeLocation(
        val name: String?,
        val iconFilename: String?,
    )
}
