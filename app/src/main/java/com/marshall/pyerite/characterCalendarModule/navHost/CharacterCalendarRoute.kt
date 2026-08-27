package com.marshall.pyerite.characterCalendarModule.navHost

sealed class CharacterCalendarRoute(val route: String) {
    object Root : CharacterCalendarRoute("character/calendar/{characterId}") {
        fun create(characterId: Long) = "character/calendar/$characterId"
    }
}
