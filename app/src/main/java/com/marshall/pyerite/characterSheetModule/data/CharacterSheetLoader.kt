package com.marshall.pyerite.characterSheetModule.data

import com.marshall.pyerite.characterSheetModule.model.CharacterMedal
import com.marshall.pyerite.characterSheetModule.model.CharacterSheet
import com.marshall.pyerite.charactersListModule.auth.EsiApi
import com.marshall.pyerite.charactersListModule.auth.EsiCharacterLocationDto
import com.marshall.pyerite.charactersListModule.auth.EsiOrganization
import com.marshall.pyerite.charactersListModule.auth.EsiPublicDataSource
import com.marshall.pyerite.charactersListModule.auth.EveTokenManager
import com.marshall.pyerite.charactersListModule.auth.allianceLogoUrl
import com.marshall.pyerite.charactersListModule.auth.corporationLogoUrl
import com.marshall.pyerite.charactersListModule.auth.parseEsiDateMillis
import com.marshall.pyerite.charactersListModule.auth.portraitUrl
import com.marshall.pyerite.charactersListModule.model.CharacterLocationInfo
import com.marshall.pyerite.charactersListModule.model.CharacterLocationPresence
import com.marshall.pyerite.data.db.RoomProvider
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
    private val api: EsiApi,
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
                            api.fetchLocation(characterId, auth)
                        }
                    }.getOrNull()
                }
                val shipDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            api.fetchShip(characterId, auth)
                        }
                    }.getOrNull()
                }
                val fatigueDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            api.fetchFatigue(characterId, auth)
                        }
                    }.getOrNull()
                }
                val medalsDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            api.fetchMedals(characterId, auth)
                        }
                    }.getOrNull()
                }
                val onlineDeferred = async {
                    runCatching {
                        tokenManager.executeWithAuthRetry(characterId) { auth ->
                            api.fetchOnline(characterId, auth)
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
                val shipType = ship?.shipTypeId?.let { resolveShipType(it) }
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
    ): CharacterLocationInfo? {
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
            CharacterLocationPresence.IN_STRUCTURE
        } else {
            CharacterLocationPresence.IN_SPACE
        }
        val placeName = dto.structureId?.let { structureId ->
            runCatching {
                tokenManager.executeWithAuthRetry(characterId) { auth ->
                    api.fetchStructure(structureId, auth).name
                }
            }.getOrNull()?.takeIf { it.isNotBlank() }
        }
        return CharacterLocationInfo(
            systemSecurityStatus = security,
            systemName = systemName,
            regionName = regionName,
            presence = presence,
            placeName = placeName,
        )
    }

    private suspend fun resolveShipType(typeId: Int): ResolvedShipType {
        val entity = runCatching {
            roomProvider.getDatabase().typeDao().getTypeById(typeId)
        }.getOrNull()
        val displayName = entity?.displayName(localeController)?.takeIf { it.isNotBlank() }
            ?: runCatching {
                roomProvider.getDatabase().typeDao().getTypeDisplayName(typeId)
                    ?.displayName(localeController)
            }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: publicEsi.fetchTypeName(typeId)
            ?: typeId.toString()
        return ResolvedShipType(
            displayName = displayName,
            iconFilename = entity?.iconFilename,
        )
    }

    private fun formatOrgLabel(org: EsiOrganization?): String? {
        org ?: return null
        val ticker = org.ticker?.let { "[$it] " }.orEmpty()
        return "$ticker${org.name}"
    }

    private data class ResolvedShipType(
        val displayName: String,
        val iconFilename: String?,
    )
}
