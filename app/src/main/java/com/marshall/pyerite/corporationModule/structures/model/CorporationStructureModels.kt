package com.marshall.pyerite.corporationModule.structures.model

import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.localizedName

internal class CorporationStructuresAccessException : Exception()

/** How far ahead fuel expiry is highlighted. [NONE] is the default. */
internal enum class CorporationStructureFuelMonitor(val days: Int) {
    NONE(CorporationStructuresConfig.MONITOR_NONE_DAYS),
    ONE_WEEK(CorporationStructuresConfig.MONITOR_ONE_WEEK_DAYS),
    TWO_WEEKS(CorporationStructuresConfig.MONITOR_TWO_WEEKS_DAYS),
    THREE_WEEKS(CorporationStructuresConfig.MONITOR_THREE_WEEKS_DAYS),
    ONE_MONTH(CorporationStructuresConfig.MONITOR_ONE_MONTH_DAYS),
    TWO_MONTHS(CorporationStructuresConfig.MONITOR_TWO_MONTHS_DAYS),
    ;

    fun windowMillis(): Long = days * CorporationStructuresConfig.MILLIS_PER_DAY

    companion object {
        fun fromDays(days: Int): CorporationStructureFuelMonitor =
            entries.firstOrNull { it.days == days } ?: NONE
    }
}

/** ESI `state` values for `/corporations/{id}/structures/`. */
internal enum class CorporationStructureState {
    SHIELD_VULNERABLE,
    ARMOR_VULNERABLE,
    HULL_VULNERABLE,
    ARMOR_REINFORCE,
    HULL_REINFORCE,
    ANCHORING,
    ANCHOR_VULNERABLE,
    DEPLOY_VULNERABLE,
    FITTING_INVULNERABLE,
    ONLINING_VULNERABLE,
    ONLINE_DEPRECATED,
    UNANCHORED,
    UNKNOWN,
    ;

    companion object {
        fun fromApi(raw: String?): CorporationStructureState = when (raw) {
            API_SHIELD_VULNERABLE -> SHIELD_VULNERABLE
            API_ARMOR_VULNERABLE -> ARMOR_VULNERABLE
            API_HULL_VULNERABLE -> HULL_VULNERABLE
            API_ARMOR_REINFORCE -> ARMOR_REINFORCE
            API_HULL_REINFORCE -> HULL_REINFORCE
            API_ANCHORING -> ANCHORING
            API_ANCHOR_VULNERABLE -> ANCHOR_VULNERABLE
            API_DEPLOY_VULNERABLE -> DEPLOY_VULNERABLE
            API_FITTING_INVULNERABLE -> FITTING_INVULNERABLE
            API_ONLINING_VULNERABLE -> ONLINING_VULNERABLE
            API_ONLINE_DEPRECATED -> ONLINE_DEPRECATED
            API_UNANCHORED -> UNANCHORED
            else -> UNKNOWN
        }

        private const val API_SHIELD_VULNERABLE = "shield_vulnerable"
        private const val API_ARMOR_VULNERABLE = "armor_vulnerable"
        private const val API_HULL_VULNERABLE = "hull_vulnerable"
        private const val API_ARMOR_REINFORCE = "armor_reinforce"
        private const val API_HULL_REINFORCE = "hull_reinforce"
        private const val API_ANCHORING = "anchoring"
        private const val API_ANCHOR_VULNERABLE = "anchor_vulnerable"
        private const val API_DEPLOY_VULNERABLE = "deploy_vulnerable"
        private const val API_FITTING_INVULNERABLE = "fitting_invulnerable"
        private const val API_ONLINING_VULNERABLE = "onlining_vulnerable"
        private const val API_ONLINE_DEPRECATED = "online_deprecated"
        private const val API_UNANCHORED = "unanchored"
    }
}

/** ESI service `state`. Anything other than online is shown as not running. */
internal enum class CorporationStructureServiceStatus {
    ONLINE,
    OFFLINE,
    CLEANUP,
    ;

    val isOnline: Boolean
        get() = this == ONLINE

    companion object {
        fun fromApi(raw: String?): CorporationStructureServiceStatus = when (raw) {
            API_ONLINE -> ONLINE
            API_CLEANUP -> CLEANUP
            else -> OFFLINE
        }

        private const val API_ONLINE = "online"
        private const val API_CLEANUP = "cleanup"
    }
}

/**
 * Known ESI service display names. Unlisted names are shown as returned by ESI.
 * These strings are the wire values, not UI copy.
 */
internal enum class CorporationStructureServiceKind(val apiName: String) {
    MANUFACTURING_STANDARD("Manufacturing (Standard)"),
    MANUFACTURING_CAPITALS("Manufacturing (Capitals)"),
    MANUFACTURING_SUPERCAPITALS("Manufacturing (Supercapitals)"),
    RESEARCH("Research"),
    INVENTION("Invention"),
    MATERIAL_EFFICIENCY_RESEARCH("Material Efficiency Research"),
    TIME_EFFICIENCY_RESEARCH("Time Efficiency Research"),
    COPYING("Copying"),
    CLONE_BAY("Clone Bay"),
    MARKET("Market"),
    REPROCESSING("Reprocessing"),
    COMPOSITE_REACTIONS("Composite Reactions"),
    BIOCHEMICAL_REACTIONS("Biochemical Reactions"),
    HYBRID_REACTIONS("Hybrid Reactions"),
    MOON_DRILLING("Moon Drilling"),
    ;

    companion object {
        fun fromApi(name: String): CorporationStructureServiceKind? =
            entries.firstOrNull { it.apiName == name }
    }
}

internal data class CorporationStructureService(
    val name: String,
    val status: CorporationStructureServiceStatus,
) {
    val kind: CorporationStructureServiceKind?
        get() = CorporationStructureServiceKind.fromApi(name)
}

internal data class CorporationStructure(
    val structureId: Long,
    val name: String,
    val iconFilename: String?,
    val state: CorporationStructureState,
    val fuelExpiresAtMillis: Long?,
    val services: List<CorporationStructureService>,
    val systemId: Long,
    val systemZhName: String?,
    val systemEnName: String?,
    val systemName: String?,
    val regionZhName: String?,
    val regionEnName: String?,
    val regionName: String?,
    val systemSecurityStatus: Double?,
)

internal data class CorporationStructuresSnapshot(
    val structures: List<CorporationStructure>,
)

internal data class CorporationStructureSystemSection(
    val systemId: Long,
    val systemZhName: String?,
    val systemEnName: String?,
    val systemName: String?,
    val regionZhName: String?,
    val regionEnName: String?,
    val regionName: String?,
    val systemSecurityStatus: Double?,
    val structures: List<CorporationStructure>,
) {
    fun systemDisplayName(language: ContentLanguage): String =
        localizedName(systemZhName, systemEnName, systemName, language)

    fun regionDisplayName(language: ContentLanguage): String =
        localizedName(regionZhName, regionEnName, regionName, language)
}

internal fun List<CorporationStructure>.systemSections(
    language: ContentLanguage,
): List<CorporationStructureSystemSection> =
    groupBy { it.systemId }
        .map { (systemId, structures) ->
            val sample = structures.first()
            CorporationStructureSystemSection(
                systemId = systemId,
                systemZhName = sample.systemZhName,
                systemEnName = sample.systemEnName,
                systemName = sample.systemName,
                regionZhName = sample.regionZhName,
                regionEnName = sample.regionEnName,
                regionName = sample.regionName,
                systemSecurityStatus = sample.systemSecurityStatus,
                structures = structures.sortedWith(
                    compareBy<CorporationStructure> { it.name.isBlank() }
                        .thenBy { it.name.lowercase() },
                ),
            )
        }
        .sortedWith(
            compareByDescending<CorporationStructureSystemSection> {
                it.systemSecurityStatus ?: Double.NEGATIVE_INFINITY
            }
                .thenBy { it.regionDisplayName(language).lowercase() }
                .thenBy { it.systemDisplayName(language).lowercase() }
                .thenBy { it.systemId },
        )

internal fun List<CorporationStructure>.lowFuelStructures(
    monitor: CorporationStructureFuelMonitor,
    nowMs: Long,
): List<CorporationStructure> {
    if (monitor == CorporationStructureFuelMonitor.NONE) return emptyList()
    val windowMs = monitor.windowMillis()
    return filter { structure ->
        val expiresAt = structure.fuelExpiresAtMillis ?: return@filter false
        val remainingMs = expiresAt - nowMs
        remainingMs > 0L && remainingMs <= windowMs
    }.sortedBy { it.fuelExpiresAtMillis }
}
