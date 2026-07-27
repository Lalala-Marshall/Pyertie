package com.marshall.pyerite.characterClonesModule.navHost

sealed class CharacterClonesRoute(val route: String) {

    object Status : CharacterClonesRoute("character/clones/{characterId}") {
        fun create(characterId: Long) = "character/clones/$characterId"
    }
}
