package com.marshall.pyerite.corporationModule.members.model

import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.localizedName

internal class CorporationMembersAccessException : Exception()

internal enum class CorporationMemberSort {
    NAME,
    SHIP,
}

internal data class CorporationMember(
    val characterId: Long,
    val name: String,
    val portraitUrl: String,
    val shipZhName: String?,
    val shipEnName: String?,
    val shipName: String?,
    val shipIconFilename: String?,
    val systemZhName: String?,
    val systemEnName: String?,
    val systemName: String?,
    val systemSecurityStatus: Double?,
)

internal data class CorporationMembersSnapshot(
    val members: List<CorporationMember>,
)

internal fun CorporationMember.shipDisplayName(language: ContentLanguage): String =
    localizedName(shipZhName, shipEnName, shipName, language)

internal fun CorporationMember.systemDisplayName(language: ContentLanguage): String =
    localizedName(systemZhName, systemEnName, systemName, language)

internal fun CorporationMember.matchesQuery(query: String, language: ContentLanguage): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    if (name.contains(needle, ignoreCase = true)) return true
    val ship = shipDisplayName(language)
    return ship.isNotBlank() && ship.contains(needle, ignoreCase = true)
}

internal fun List<CorporationMember>.sortedFor(
    sort: CorporationMemberSort,
    language: ContentLanguage,
): List<CorporationMember> = when (sort) {
    CorporationMemberSort.NAME -> sortedWith(
        compareBy<CorporationMember, String>(String.CASE_INSENSITIVE_ORDER) { it.name }
            .thenBy { it.characterId },
    )
    CorporationMemberSort.SHIP -> sortedWith(
        Comparator { left, right ->
            val leftShip = left.shipDisplayName(language)
            val rightShip = right.shipDisplayName(language)
            val leftBlank = leftShip.isBlank()
            val rightBlank = rightShip.isBlank()
            when {
                leftBlank && rightBlank -> left.characterId.compareTo(right.characterId)
                leftBlank -> 1
                rightBlank -> -1
                else -> {
                    val byShip = String.CASE_INSENSITIVE_ORDER.compare(leftShip, rightShip)
                    if (byShip != 0) byShip else left.characterId.compareTo(right.characterId)
                }
            }
        },
    )
}
