package com.marshall.pyerite.characterSheetModule.navHost

sealed class CharacterSheetRoute(val route: String) {

    object Sheet : CharacterSheetRoute("character/sheet/{characterId}") {
        fun create(characterId: Long) = "character/sheet/$characterId"
    }
}
