package com.marshall.pyerite.characterModule.auth

import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import com.marshall.pyerite.characterModule.model.SkillQueueProgress
import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

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
            val onlineDeferred = async {
                runCatching {
                    tokenManager.executeWithAuthRetry(session.characterId) { auth ->
                        api.fetchOnline(session.characterId, auth)
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
            val systemName = location?.solarSystemId?.let { publicEsi.fetchSolarSystemName(it) }
            val queue = queueDeferred.await()?.let { entries -> mapSkillQueue(entries) }

            LoggedInCharacter(
                characterId = session.characterId,
                name = public?.name ?: session.characterName.ifBlank { session.characterId.toString() },
                portraitUrl = portraitUrl(session.characterId),
                securityStatus = public?.securityStatus,
                location = systemName,
                isOnline = onlineDeferred.await()?.online,
                walletBalance = walletDeferred.await()?.let { formatIsk(it) },
                totalSkillPoints = skills?.totalSp?.let { formatSp(it) },
                unallocatedSkillPoints = skills?.unallocatedSp?.let { formatSp(it) },
                corporationName = formatOrgLabel(corporation),
                corporationIconUrl = public?.corporationId?.let { corporationLogoUrl(it) },
                allianceName = formatOrgLabel(alliance),
                allianceIconUrl = public?.allianceId?.let { allianceLogoUrl(it) },
                skillQueue = queue,
            )
        }
    }

    private suspend fun mapSkillQueue(entries: List<EsiSkillQueueEntryDto>): SkillQueueProgress? {
        val active = entries
            .filter { !it.finishDate.isNullOrBlank() }
            .minByOrNull { it.queuePosition }
            ?: return null
        val finishMs = parseEsiDateMillis(active.finishDate) ?: return null
        val startMs = active.startDate?.let { parseEsiDateMillis(it) }
        val nowMs = System.currentTimeMillis()
        val remainingSeconds = max(0L, (finishMs - nowMs) / EveSsoConfig.MILLIS_PER_SECOND)
        val progress = if (startMs != null && finishMs > startMs) {
            val total = (finishMs - startMs).toFloat()
            val done = (nowMs - startMs).toFloat()
            if (total > 0f) (done / total).coerceIn(0f, 1f) else 0f
        } else {
            0f
        }
        val skillName = resolveSkillName(active.skillId)
        return SkillQueueProgress(
            skillName = skillName,
            level = active.finishedLevel,
            progress = progress,
            remainingSeconds = remainingSeconds,
        )
    }

    private suspend fun resolveSkillName(typeId: Int): String {
        val entity = runCatching {
            roomProvider.getDatabase().typeDao().getTypeById(typeId)
        }.getOrNull()
        return entity?.displayName(localeController)?.takeIf { it.isNotBlank() }
            ?: typeId.toString()
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

    private fun formatIsk(value: Double): String =
        String.format(Locale.US, "%,.2f", value)

    private fun formatSp(value: Long): String =
        String.format(Locale.US, "%,d", value)
}
