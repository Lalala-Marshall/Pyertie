package com.marshall.pyerite.characterClonesModule.navHost

sealed class CharacterClonesRoute(val route: String) {

    object Status : CharacterClonesRoute("character/clones/{characterId}") {
        fun create(characterId: Long) = "character/clones/$characterId"
    }

    object Location : CharacterClonesRoute(
        "character/clones/{characterId}/location/{locationType}/{locationId}",
    ) {
        fun create(
            characterId: Long,
            locationType: String,
            locationId: Long,
        ) = "character/clones/$characterId/location/$locationType/$locationId"
    }
}
