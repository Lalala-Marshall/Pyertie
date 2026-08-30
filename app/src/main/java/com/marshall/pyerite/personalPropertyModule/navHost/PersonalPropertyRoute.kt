package com.marshall.pyerite.personalPropertyModule.navHost

sealed class PersonalPropertyRoute(val route: String) {
    object Root : PersonalPropertyRoute("character/property/{characterId}") {
        fun create(characterId: Long) = "character/property/$characterId"
    }
}
