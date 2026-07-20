package com.marshall.pyerite.characterModule.auth

import com.marshall.pyerite.characterModule.model.CharacterLocationInfo
import com.marshall.pyerite.characterModule.model.CharacterLocationPresence
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import com.marshall.pyerite.characterModule.model.SkillQueueEntry
import com.marshall.pyerite.characterModule.model.SkillQueueProgress
import com.marshall.pyerite.characterModule.model.SkillQueueTrainingState
import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.localization.localizedName
import com.marshall.pyerite.util.NumberDisplayFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Builds [LoggedInCharacter] from public + authenticated ESI.
 * Isolates enrichment from OAuth flow in [EveSsoAuthRepository].
 */
internal class CharacterProfileLoader(
    private val publicEsi: EsiPublicDataSource,
    private val tokenManager: EveTokenManager,
    private val api: EsiApi,
    private val roomProvider: RoomProvider,
    private val localeController: LocaleController,
) {
    suspend fun load(session: EveStoredSession): LoggedInCharacter = withContext(Dispatchers.IO) {
        coroutineScope {
            val publicDeferred = async {
                runCatching { publicEsi.fetchCharacter(session.characterId) }.getOrNull()
            }
            val walletDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(session.characterId) { auth ->
                        api.fetchWallet(session.characterId, auth).use { body ->
                            body.string().trim().toDouble()
                        }
                    }
                }.getOrNull()
            }
            val skillsDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(session.characterId) { auth ->
                        api.fetchSkills(session.characterId, auth)
                    }
                }.getOrNull()
            }
            val queueDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(session.characterId) { auth ->
                        api.fetchSkillQueue(session.characterId, auth)
                    }
                }.getOrNull()
            }
            val locationDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(session.characterId) { auth ->
                        api.fetchLocation(session.characterId, auth)
                    }
                }.getOrNull()
            }

            val public = publicDeferred.await()
            val skills = skillsDeferred.await()
            val corporation = public?.corporationId?.let { id ->
                runCatching { publicEsi.fetchCorporation(id) }.getOrNull()
            }
            val alliance = public?.allianceId?.let { id ->
                runCatching { publicEsi.fetchAlliance(id) }.getOrNull()
            }
            val location = locationDeferred.await()
            val locationInfo = resolveLocation(location)
            val queue = queueDeferred.await()?.let { entries -> mapSkillQueue(entries) }

            LoggedInCharacter(
                characterId = session.characterId,
                name = public?.name ?: session.characterName.ifBlank { session.characterId.toString() },
                portraitUrl = portraitUrl(session.characterId),
                securityStatus = public?.securityStatus,
                location = locationInfo,
                walletBalance = walletDeferred.await()?.let {
                    NumberDisplayFormatter.format(it, NumberDisplayFormatter.Style.COMPACT)
                },
                totalSkillPoints = skills?.totalSp?.let {
                    NumberDisplayFormatter.format(it, NumberDisplayFormatter.Style.COMPACT)
                },
                unallocatedSkillPoints = skills?.unallocatedSp?.let {
                    NumberDisplayFormatter.format(it, NumberDisplayFormatter.Style.COMPACT)
                },
                corporationName = formatOrgLabel(corporation),
                corporationIconUrl = public?.corporationId?.let { corporationLogoUrl(it) },
                allianceName = formatOrgLabel(alliance),
                allianceIconUrl = public?.allianceId?.let { allianceLogoUrl(it) },
                skillQueue = queue,
                grantedScopes = tokenManager.grantedScopes(session.characterId),
            )
        }
    }

    private suspend fun mapSkillQueue(entries: List<EsiSkillQueueEntryDto>): SkillQueueProgress {
        if (entries.isEmpty()) {
            return SkillQueueProgress(
                state = SkillQueueTrainingState.IDLE,
                entries = emptyList(),
            )
        }
        val mapped = entries
            .sortedBy { it.queuePosition }
            .map { entry ->
                SkillQueueEntry(
                    skillName = resolveSkillName(entry.skillId),
                    level = entry.finishedLevel,
                    startAtEpochMs = entry.startDate?.let { parseEsiDateMillis(it) },
                    finishAtEpochMs = entry.finishDate?.let { parseEsiDateMillis(it) },
                    trainingStartSp = entry.trainingStartSp,
                    levelStartSp = entry.levelStartSp,
                    levelEndSp = entry.levelEndSp,
                )
            }
        val trainingEntries = mapped.filter { it.finishAtEpochMs != null }
        return if (trainingEntries.isNotEmpty()) {
            SkillQueueProgress(
                state = SkillQueueTrainingState.TRAINING,
                entries = trainingEntries,
            )
        } else {
            SkillQueueProgress(
                state = SkillQueueTrainingState.PAUSED,
                entries = mapped,
            )
        }
    }

    private suspend fun resolveLocation(
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
        return CharacterLocationInfo(
            systemSecurityStatus = security,
            systemName = systemName,
            regionName = regionName,
            presence = presence,
        )
    }

    private suspend fun resolveSkillName(typeId: Int): String {
        val fromSde = runCatching {
            roomProvider.getDatabase().typeDao().getTypeDisplayName(typeId)
                ?.displayName(localeController)
        }.getOrNull()?.takeIf { it.isNotBlank() }
        if (fromSde != null) return fromSde

        val fromEsi = runCatching { publicEsi.fetchTypeName(typeId) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
        if (fromEsi != null) return fromEsi

        return typeId.toString()
    }

    private fun parseEsiDateMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val normalized = raw.trim().removeSuffix("Z") + "Z"
        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        )
        for (pattern in formats) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                    isLenient = false
                }.parse(normalized)?.time
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }

    private fun formatOrgLabel(org: EsiOrganization?): String? {
        org ?: return null
        val ticker = org.ticker?.let { "[$it] " }.orEmpty()
        return "$ticker${org.name}"
    }
}
