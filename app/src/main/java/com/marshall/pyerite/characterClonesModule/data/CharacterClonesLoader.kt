package com.marshall.pyerite.characterClonesModule.data

import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import com.marshall.pyerite.characterClonesModule.model.CloneLocationTypeApi
import com.marshall.pyerite.characterClonesModule.model.JumpCloneConfig
import com.marshall.pyerite.characterClonesModule.model.JumpCloneInfo
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
 * Loads jump-clone cooldown, home location, and jump-clone list.
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
            val jumpClones = clones.jumpClones.map { clone ->
                mapJumpClone(characterId, clone)
            }

            CharacterCloneStatus(
                characterId = characterId,
                nextCloneJumpEpochMs = nextCloneJumpEpochMs,
                lastCloneJumpEpochMs = lastCloneJumpEpochMs,
                lastStationChangeEpochMs = lastStationChangeEpochMs,
                homeLocationName = homeLocation?.name,
                homeLocationIconFilename = homeLocation?.iconFilename,
                jumpClones = jumpClones,
            )
        }
    }

    private suspend fun mapJumpClone(
        characterId: Long,
        dto: EsiJumpCloneDto,
    ): JumpCloneInfo = JumpCloneInfo(
        jumpCloneId = dto.jumpCloneId,
        name = dto.name?.takeIf { it.isNotBlank() },
        locationName = resolveLocationName(
            characterId = characterId,
            location = EsiCloneLocationDto(
                locationId = dto.locationId,
                locationType = dto.locationType,
            ),
        ),
        implantCount = dto.implants.size,
    )

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

    private suspend fun resolveLocationName(
        characterId: Long,
        location: EsiCloneLocationDto,
    ): String? = resolvePlace(characterId, location).name

    private suspend fun resolvePlace(
        characterId: Long,
        location: EsiCloneLocationDto,
    ): ResolvedPlace = when (location.locationType) {
        CloneLocationTypeApi.STATION -> resolveStation(location.locationId)
        CloneLocationTypeApi.STRUCTURE -> resolveStructure(characterId, location.locationId)
        CloneLocationTypeApi.SOLAR_SYSTEM -> ResolvedPlace(
            name = resolveSolarSystemName(location.locationId),
            typeId = null,
        )
        else -> ResolvedPlace(name = null, typeId = null)
    }

    private suspend fun resolveStation(stationId: Long): ResolvedPlace {
        val fromSde = runCatching {
            roomProvider.getDatabase().mapDao().getStation(stationId)
        }.getOrNull()
        if (fromSde != null) {
            return ResolvedPlace(
                name = fromSde.name?.takeIf { it.isNotBlank() },
                typeId = fromSde.typeId,
            )
        }
        val fromEsi = publicEsi.fetchStation(stationId)
        return ResolvedPlace(
            name = fromEsi?.name?.takeIf { it.isNotBlank() },
            typeId = fromEsi?.typeId,
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
        )
    }

    private suspend fun resolveSolarSystemName(solarSystemId: Long): String? {
        val row = runCatching {
            roomProvider.getDatabase().mapDao().getSolarSystemLocation(solarSystemId)
        }.getOrNull()
        val fromSde = localizedName(
            zh = row?.systemZhName,
            en = row?.systemEnName,
            fallback = row?.systemName,
            language = localeController.contentLanguage,
        ).takeIf { it.isNotBlank() }
        if (fromSde != null) return fromSde
        return publicEsi.fetchSolarSystemName(solarSystemId)?.takeIf { it.isNotBlank() }
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

    private data class ResolvedPlace(
        val name: String?,
        val typeId: Int?,
    )

    private data class ResolvedHomeLocation(
        val name: String?,
        val iconFilename: String?,
    )
}
