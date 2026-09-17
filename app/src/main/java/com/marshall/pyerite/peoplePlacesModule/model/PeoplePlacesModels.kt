package com.marshall.pyerite.peoplePlacesModule.model

internal enum class PeoplePlacesCategory {
    CHARACTER,
    CORPORATION,
    ALLIANCE,
    STRUCTURE,
}

internal enum class PeoplePlacesSearchStatus {
    IDLE,
    SEARCHING,
    RESULTS,
    FAILED,
    QUERY_TOO_SHORT,
}

internal enum class PeoplePlacesBuildingType {
    ALL,
    STATION,
    STRUCTURE,
}

internal enum class PeoplePlacesStanding {
    SAME_CORPORATION,
    SAME_ALLIANCE,
    PLUS_10,
    PLUS_5,
    NEUTRAL,
    MINUS_5,
    MINUS_10,
}

internal data class PeoplePlacesViewer(
    val characterId: Long,
    val corporationId: Long?,
    val allianceId: Long?,
)

internal data class PeoplePlacesCharacterFilters(
    val corporationQuery: String = "",
    val allianceQuery: String = "",
    val myCorporationOnly: Boolean = false,
    val myAllianceOnly: Boolean = false,
    val exactMatch: Boolean = false,
)

internal data class PeoplePlacesCorporationFilters(
    val allianceQuery: String = "",
    val myAllianceOnly: Boolean = false,
    val exactMatch: Boolean = false,
)

internal data class PeoplePlacesBuildingFilters(
    val buildingType: PeoplePlacesBuildingType = PeoplePlacesBuildingType.ALL,
    val exactMatch: Boolean = false,
)

internal data class PeoplePlacesSearchRequest(
    val characterId: Long,
    val query: String,
    val category: PeoplePlacesCategory,
    val characterFilters: PeoplePlacesCharacterFilters,
    val corporationFilters: PeoplePlacesCorporationFilters,
    val allianceExactMatch: Boolean,
    val buildingFilters: PeoplePlacesBuildingFilters,
    val viewer: PeoplePlacesViewer?,
)

internal data class PeoplePlacesSearchOutcome(
    val results: List<PeoplePlacesResult>,
    val displayedCount: Int,
    val totalCount: Int,
)

internal sealed class PeoplePlacesResult {
    abstract val id: Long
    abstract val name: String
    abstract val standing: PeoplePlacesStanding?

    data class Character(
        override val id: Long,
        override val name: String,
        val portraitUrl: String,
        val corporationId: Long?,
        val corporationName: String?,
        val corporationIconUrl: String?,
        val allianceId: Long?,
        val allianceName: String?,
        val allianceIconUrl: String?,
        override val standing: PeoplePlacesStanding?,
    ) : PeoplePlacesResult()

    data class Corporation(
        override val id: Long,
        override val name: String,
        val logoUrl: String,
        val allianceId: Long?,
        val allianceName: String?,
        val allianceIconUrl: String?,
        override val standing: PeoplePlacesStanding?,
    ) : PeoplePlacesResult()

    data class Alliance(
        override val id: Long,
        override val name: String,
        val logoUrl: String,
        override val standing: PeoplePlacesStanding?,
    ) : PeoplePlacesResult()

    data class Building(
        override val id: Long,
        override val name: String,
        val iconFileName: String?,
        val securityStatus: Double?,
        val systemName: String?,
        val regionName: String?,
        override val standing: PeoplePlacesStanding? = null,
    ) : PeoplePlacesResult()
}
