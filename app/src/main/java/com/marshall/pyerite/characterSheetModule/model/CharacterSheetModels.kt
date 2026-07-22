package com.marshall.pyerite.characterSheetModule.model

import com.marshall.pyerite.charactersListModule.model.CharacterLocationInfo
import com.marshall.pyerite.charactersListModule.model.LoggedInCharacter

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
    val location: CharacterLocationInfo?,
    val shipTypeId: Int?,
    val shipDisplayName: String?,
    val shipIconFilename: String?,
    val jumpFatigueExpireEpochMs: Long?,
    val lastJumpEpochMs: Long?,
    val medals: List<CharacterMedal>,
) {
    companion object {
        /** Immediate shell from list-cache identity fields while ESI sheet loads. */
        fun seed(
            characterId: Long,
            cached: LoggedInCharacter?,
        ): CharacterSheet = CharacterSheet(
            characterId = characterId,
            name = cached?.name?.takeIf { it.isNotBlank() } ?: characterId.toString(),
            portraitUrl = cached?.portraitUrl,
            corporationName = cached?.corporationName,
            corporationIconUrl = cached?.corporationIconUrl,
            allianceName = cached?.allianceName,
            allianceIconUrl = cached?.allianceIconUrl,
            isOnline = null,
            birthdayEpochMs = null,
            securityStatus = null,
            location = cached?.location,
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
