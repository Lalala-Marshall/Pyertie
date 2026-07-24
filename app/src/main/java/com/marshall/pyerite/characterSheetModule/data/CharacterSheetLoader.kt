package com.marshall.pyerite.characterSheetModule.data

import com.marshall.pyerite.characterSheetModule.model.CharacterMedal
import com.marshall.pyerite.characterSheetModule.model.CharacterSheet
import com.marshall.pyerite.characterSheetModule.model.CharacterSheetLocation
import com.marshall.pyerite.characterSheetModule.model.CharacterSheetLocationPresence
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.model.EsiCharacterLocationDto
import com.marshall.pyerite.esiModule.model.EsiOrganization
import com.marshall.pyerite.esiModule.data.EsiPublicDataSource
import com.marshall.pyerite.esiModule.api.EsiUniverseApi
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.esiModule.data.allianceLogoUrl
import com.marshall.pyerite.esiModule.data.corporationLogoUrl
import com.marshall.pyerite.esiModule.model.parseEsiDateMillis
import com.marshall.pyerite.esiModule.data.portraitUrl
import com.marshall.pyerite.sdeModule.room.RoomProvider
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.localization.localizedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Loads [CharacterSheet] fields not required on the management list:
 * birthday, security status, ship, fatigue, medals, online, structure name.
 */
internal class CharacterSheetLoader(
    private val publicEsi: EsiPublicDataSource,
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
    private val universeApi: EsiUniverseApi,
    private val roomProvider: RoomProvider,
    private val localeController: LocaleController,
) {
    suspend fun load(characterId: Long, characterNameFallback: String): CharacterSheet =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val publicDeferred = async {
                    runCatching { publicEsi.fetchCharacter(characterId) }.getOrNull()
                }
                val locationDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            characterApi.fetchLocation(characterId, auth)
                        }
                    }.getOrNull()
                }
                val shipDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            characterApi.fetchShip(characterId, auth)
                        }
                    }.getOrNull()
                }
                val fatigueDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            characterApi.fetchFatigue(characterId, auth)
                        }
                    }.getOrNull()
                }
                val medalsDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            characterApi.fetchMedals(characterId, auth)
                        }
                    }.getOrNull()
                }
                val onlineDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            characterApi.fetchOnline(characterId, auth)
                        }
                    }.getOrNull()
                }

                val public = publicDeferred.await()
                val corporation = public?.corporationId?.let { id ->
                    runCatching { publicEsi.fetchCorporation(id) }.getOrNull()
                }
                val alliance = public?.allianceId?.let { id ->
                    runCatching { publicEsi.fetchAlliance(id) }.getOrNull()
                }
                val locationDto = locationDeferred.await()
                val location = resolveLocation(characterId, locationDto)
                val ship = shipDeferred.await()
                val shipType = ship?.shipTypeId?.let { resolveType(it) }
                val fatigue = fatigueDeferred.await()
                val medals = medalsDeferred.await().orEmpty()
                    .map { dto ->
                        CharacterMedal(
                            medalId = dto.medalId,
                            title = dto.title,
                            description = dto.description.ifBlank { dto.reason },
                            dateEpochMs = parseEsiDateMillis(dto.date),
                        )
                    }
                    .sortedByDescending { it.dateEpochMs ?: Long.MIN_VALUE }

                CharacterSheet(
                    characterId = characterId,
                    name = public?.name
                        ?: characterNameFallback.ifBlank { characterId.toString() },
                    portraitUrl = portraitUrl(characterId),
                    corporationName = formatOrgLabel(corporation),
                    corporationIconUrl = public?.corporationId?.let { corporationLogoUrl(it) },
                    allianceName = formatOrgLabel(alliance),
                    allianceIconUrl = public?.allianceId?.let { allianceLogoUrl(it) },
                    isOnline = onlineDeferred.await()?.online,
                    birthdayEpochMs = parseEsiDateMillis(public?.birthday),
                    securityStatus = public?.securityStatus,
                    location = location,
                    shipTypeId = ship?.shipTypeId,
                    shipDisplayName = shipType?.displayName,
                    shipIconFilename = shipType?.iconFilename,
                    jumpFatigueExpireEpochMs = parseEsiDateMillis(fatigue?.jumpFatigueExpireDate),
                    lastJumpEpochMs = parseEsiDateMillis(fatigue?.lastJumpDate),
                    medals = medals,
                )
            }
        }

    private suspend fun resolveLocation(
        characterId: Long,
        dto: EsiCharacterLocationDto?,
    ): CharacterSheetLocation? {
        dto ?: return null
        val row = runCatching {
            roomProvider.getDatabase().mapDao().getSolarSystemLocation(dto.solarSystemId)
        }.getOrNull()
        val language = localeController.contentLanguage
        val systemName = localizedName(
            zh = row?.systemZhName,
            en = row?.systemEnName,
            fallback = row?.systemName,
            language = language,
        ).ifBlank {
            publicEsi.fetchSolarSystemName(dto.solarSystemId).orEmpty()
        }
        if (systemName.isBlank()) return null
        val regionName = localizedName(
            zh = row?.regionZhName,
            en = row?.regionEnName,
            fallback = row?.regionName,
            language = language,
        )
        val security = row?.securityStatus
            ?: publicEsi.fetchSolarSystemSecurity(dto.solarSystemId)
            ?: return null
        val presence = if (dto.stationId != null || dto.structureId != null) {
            CharacterSheetLocationPresence.IN_STRUCTURE
        } else {
            CharacterSheetLocationPresence.IN_SPACE
        }
        val place = resolvePlace(characterId, dto)
        val placeIconFilename = place.typeId?.let { resolveTypeIconFilename(it) }
        return CharacterSheetLocation(
            systemSecurityStatus = security,
            systemName = systemName,
            regionName = regionName,
            presence = presence,
            placeName = place.name,
            placeTypeId = place.typeId,
            placeIconFilename = placeIconFilename,
        )
    }

    private suspend fun resolvePlace(
        characterId: Long,
        dto: EsiCharacterLocationDto,
    ): ResolvedPlace {
        dto.structureId?.let { structureId ->
            val structure = runCatching {
                tokenManager.executeWithAuthRetry(characterId) { auth ->
                    universeApi.fetchStructure(structureId, auth)
                }
            }.getOrNull()
            if (structure != null) {
                return ResolvedPlace(
                    name = structure.name.takeIf { it.isNotBlank() },
                    typeId = structure.typeId,
                )
            }
        }
        dto.stationId?.let { stationId ->
            val station = runCatching {
                roomProvider.getDatabase().mapDao().getStation(stationId)
            }.getOrNull()
            if (station != null) {
                return ResolvedPlace(
                    name = station.name?.takeIf { it.isNotBlank() },
                    typeId = station.typeId,
                )
            }
            val fromEsi = publicEsi.fetchStation(stationId)
            if (fromEsi != null) {
                return ResolvedPlace(
                    name = fromEsi.name.takeIf { it.isNotBlank() },
                    typeId = fromEsi.typeId,
                )
            }
        }
        if (dto.stationId == null && dto.structureId == null) {
            val starTypeId = publicEsi.fetchSolarSystemStarTypeId(dto.solarSystemId)
                ?: CharacterSheetLocationConfig.FALLBACK_SUN_TYPE_ID
            return ResolvedPlace(name = null, typeId = starTypeId)
        }
        return ResolvedPlace(name = null, typeId = null)
    }

    private suspend fun resolveType(typeId: Int): ResolvedType {
        val dao = runCatching { roomProvider.getDatabase().sdeTypeDao() }.getOrNull()
            ?: return ResolvedType(
                displayName = publicEsi.fetchTypeName(typeId) ?: typeId.toString(),
                iconFilename = null,
            )
        val entity = runCatching { dao.getTypeById(typeId) }.getOrNull()
        val displayName = entity?.displayName(localeController)?.takeIf { it.isNotBlank() }
            ?: runCatching {
                dao.getTypeDisplayName(typeId)?.displayName(localeController)
            }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: publicEsi.fetchTypeName(typeId)
            ?: typeId.toString()
        val iconFilename = resolveTypeIconFilename(typeId, entity?.iconFilename)
        return ResolvedType(
            displayName = displayName,
            iconFilename = iconFilename,
        )
    }

    /**
     * Remote type id → local `types.icon_filename`.
     * Prefer a dedicated column query; fall back to full type row.
     */
    private suspend fun resolveTypeIconFilename(
        typeId: Int,
        knownFilename: String? = null,
    ): String? {
        knownFilename?.takeIf { it.isNotBlank() }?.let { return it }
        val dao = runCatching { roomProvider.getDatabase().sdeTypeDao() }.getOrNull()
            ?: return null
        val fromColumn = runCatching { dao.getTypeIconFilename(typeId) }
            .getOrNull()?.takeIf { it.isNotBlank() }
        if (fromColumn != null) return fromColumn
        return runCatching { dao.getTypeById(typeId)?.iconFilename }
            .getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun formatOrgLabel(org: EsiOrganization?): String? {
        org ?: return null
        val ticker = org.ticker?.let { "[$it] " }.orEmpty()
        return "$ticker${org.name}"
    }

    private data class ResolvedType(
        val displayName: String,
        val iconFilename: String?,
    )

    private data class ResolvedPlace(
        val name: String?,
        val typeId: Int?,
    )
}

/** Location icon resolution defaults (SDE type ids). */
internal object CharacterSheetLocationConfig {
    /** Yellow G5 sun — used when ESI star lookup fails while in space. */
    const val FALLBACK_SUN_TYPE_ID = 6
}
