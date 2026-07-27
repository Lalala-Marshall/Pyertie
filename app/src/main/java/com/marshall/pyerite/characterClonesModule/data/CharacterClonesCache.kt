package com.marshall.pyerite.characterClonesModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.characterClonesModule.model.ActiveImplantInfo
import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import com.marshall.pyerite.characterClonesModule.model.JumpCloneInfo
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
    val jumpClones: List<CachedJumpClone> = emptyList(),
) {
    fun toModel(): CharacterCloneStatus = CharacterCloneStatus(
        characterId = characterId,
        nextCloneJumpEpochMs = nextCloneJumpEpochMs,
        lastCloneJumpEpochMs = lastCloneJumpEpochMs,
        lastStationChangeEpochMs = lastStationChangeEpochMs,
        homeLocationName = homeLocationName,
        homeLocationIconFilename = homeLocationIconFilename,
        activeImplants = activeImplants.map { it.toModel() },
        jumpClones = jumpClones.map { it.toModel() },
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
                jumpClones = status.jumpClones.map(CachedJumpClone::from),
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
private data class CachedJumpClone(
    val jumpCloneId: Int,
    val name: String? = null,
    val locationName: String? = null,
    val implantCount: Int = 0,
) {
    fun toModel(): JumpCloneInfo = JumpCloneInfo(
        jumpCloneId = jumpCloneId,
        name = name,
        locationName = locationName,
        implantCount = implantCount,
    )

    companion object {
        fun from(clone: JumpCloneInfo): CachedJumpClone = CachedJumpClone(
            jumpCloneId = clone.jumpCloneId,
            name = clone.name,
            locationName = clone.locationName,
            implantCount = clone.implantCount,
        )
    }
}
