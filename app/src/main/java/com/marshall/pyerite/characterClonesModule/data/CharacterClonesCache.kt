package com.marshall.pyerite.characterClonesModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.characterClonesModule.model.ActiveImplantInfo
import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import com.marshall.pyerite.characterClonesModule.model.JumpCloneLocationGroup
import com.marshall.pyerite.infra.network.PyeriteJson
import kotlinx.serialization.Serializable

/**
 * Disk cache of non-secret [CharacterCloneStatus] for instant main-page hint / page UI
 * on cold start. Never stores tokens.
 */
internal class CharacterClonesCache(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(characterId: Long): CharacterCloneStatus? {
        val raw = prefs.getString(keyFor(characterId), null) ?: return null
        return runCatching {
            PyeriteJson.decodeFromString<CachedCharacterCloneStatus>(raw).toModel()
        }.getOrNull()
    }

    fun save(status: CharacterCloneStatus) {
        val encoded = PyeriteJson.encodeToString(CachedCharacterCloneStatus.from(status))
        prefs.edit { putString(keyFor(status.characterId), encoded) }
    }

    private fun keyFor(characterId: Long): String = "$KEY_PREFIX$characterId"

    private companion object {
        const val PREFS_NAME = "pyerite_character_clones_cache"
        const val KEY_PREFIX = "clones_"
    }
}

@Serializable
private data class CachedCharacterCloneStatus(
    val characterId: Long,
    val nextCloneJumpEpochMs: Long? = null,
    val lastCloneJumpEpochMs: Long? = null,
    val lastStationChangeEpochMs: Long? = null,
    val homeLocationName: String? = null,
    val homeLocationIconFilename: String? = null,
    val activeImplants: List<CachedActiveImplant> = emptyList(),
    val jumpCloneLocations: List<CachedJumpCloneLocation> = emptyList(),
) {
    fun toModel(): CharacterCloneStatus = CharacterCloneStatus(
        characterId = characterId,
        nextCloneJumpEpochMs = nextCloneJumpEpochMs,
        lastCloneJumpEpochMs = lastCloneJumpEpochMs,
        lastStationChangeEpochMs = lastStationChangeEpochMs,
        homeLocationName = homeLocationName,
        homeLocationIconFilename = homeLocationIconFilename,
        activeImplants = activeImplants.map { it.toModel() },
        jumpCloneLocations = jumpCloneLocations.map { it.toModel() },
    )

    companion object {
        fun from(status: CharacterCloneStatus): CachedCharacterCloneStatus =
            CachedCharacterCloneStatus(
                characterId = status.characterId,
                nextCloneJumpEpochMs = status.nextCloneJumpEpochMs,
                lastCloneJumpEpochMs = status.lastCloneJumpEpochMs,
                lastStationChangeEpochMs = status.lastStationChangeEpochMs,
                homeLocationName = status.homeLocationName,
                homeLocationIconFilename = status.homeLocationIconFilename,
                activeImplants = status.activeImplants.map(CachedActiveImplant::from),
                jumpCloneLocations = status.jumpCloneLocations.map(CachedJumpCloneLocation::from),
            )
    }
}

@Serializable
private data class CachedActiveImplant(
    val typeId: Int,
    val name: String? = null,
    val zhName: String? = null,
    val enName: String? = null,
    val iconFilename: String? = null,
) {
    fun toModel(): ActiveImplantInfo = ActiveImplantInfo(
        typeId = typeId,
        name = name,
        zhName = zhName,
        enName = enName,
        iconFilename = iconFilename,
    )

    companion object {
        fun from(implant: ActiveImplantInfo): CachedActiveImplant = CachedActiveImplant(
            typeId = implant.typeId,
            name = implant.name,
            zhName = implant.zhName,
            enName = implant.enName,
            iconFilename = implant.iconFilename,
        )
    }
}

@Serializable
private data class CachedJumpCloneLocation(
    val locationId: Long,
    val locationType: String,
    val locationName: String? = null,
    val systemSecurityStatus: Double? = null,
    val iconFilename: String? = null,
    val cloneCount: Int = 0,
    val implantCount: Int = 0,
    val jumpCloneIds: List<Int> = emptyList(),
) {
    fun toModel(): JumpCloneLocationGroup = JumpCloneLocationGroup(
        locationId = locationId,
        locationType = locationType,
        locationName = locationName,
        systemSecurityStatus = systemSecurityStatus,
        iconFilename = iconFilename,
        cloneCount = cloneCount,
        implantCount = implantCount,
        jumpCloneIds = jumpCloneIds,
    )

    companion object {
        fun from(group: JumpCloneLocationGroup): CachedJumpCloneLocation = CachedJumpCloneLocation(
            locationId = group.locationId,
            locationType = group.locationType,
            locationName = group.locationName,
            systemSecurityStatus = group.systemSecurityStatus,
            iconFilename = group.iconFilename,
            cloneCount = group.cloneCount,
            implantCount = group.implantCount,
            jumpCloneIds = group.jumpCloneIds,
        )
    }
}
