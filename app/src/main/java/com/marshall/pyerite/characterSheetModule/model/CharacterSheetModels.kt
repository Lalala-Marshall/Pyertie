package com.marshall.pyerite.characterSheetModule.model

/**
 * Full character sheet payload for the sheet detail page.
 * Presentation strings (dates, ages) are formatted in the UI layer.
 */
data class CharacterSheet(
    val characterId: Long,
    val name: String,
    val portraitUrl: String?,
    val corporationName: String?,
    val corporationIconUrl: String?,
    val allianceName: String?,
    val allianceIconUrl: String?,
    val isOnline: Boolean?,
    val birthdayEpochMs: Long?,
    val securityStatus: Double?,
    val location: CharacterSheetLocation?,
    val shipTypeId: Int?,
    val shipDisplayName: String?,
    val shipIconFilename: String?,
    val jumpFatigueExpireEpochMs: Long?,
    val lastJumpEpochMs: Long?,
    val medals: List<CharacterMedal>,
) {
    companion object {
        /** Immediate shell while ESI sheet loads. */
        fun seed(characterId: Long, fallbackName: String = ""): CharacterSheet = CharacterSheet(
            characterId = characterId,
            name = fallbackName.ifBlank { characterId.toString() },
            portraitUrl = null,
            corporationName = null,
            corporationIconUrl = null,
            allianceName = null,
            allianceIconUrl = null,
            isOnline = null,
            birthdayEpochMs = null,
            securityStatus = null,
            location = null,
            shipTypeId = null,
            shipDisplayName = null,
            shipIconFilename = null,
            jumpFatigueExpireEpochMs = null,
            lastJumpEpochMs = null,
            medals = emptyList(),
        )
    }
}

data class CharacterMedal(
    val medalId: Int,
    val title: String,
    val description: String,
    val dateEpochMs: Long?,
)

enum class CharacterSheetLocationPresence {
    IN_STRUCTURE,
    IN_SPACE,
}

/** Sheet-only location line (system + optional docked place / sun type icon). */
data class CharacterSheetLocation(
    val systemSecurityStatus: Double,
    val systemName: String,
    val regionName: String,
    val presence: CharacterSheetLocationPresence,
    val placeName: String? = null,
    val placeTypeId: Int? = null,
    val placeIconFilename: String? = null,
)
