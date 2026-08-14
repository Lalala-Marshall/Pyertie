package com.marshall.pyerite.characterMailModule.navHost

sealed class CharacterMailRoute(val route: String) {

    object Root : CharacterMailRoute("character/mail/{characterId}") {
        fun create(characterId: Long) = "character/mail/$characterId"
    }

    object All : CharacterMailRoute("character/mail/{characterId}/all") {
        fun create(characterId: Long) = "character/mail/$characterId/all"
    }

    companion object {
        const val ARG_CHARACTER_ID = "characterId"
    }
}
