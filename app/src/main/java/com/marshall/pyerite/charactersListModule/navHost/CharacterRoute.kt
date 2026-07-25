package com.marshall.pyerite.charactersListModule.navHost

sealed class CharacterRoute(val route: String) {

    object Root : CharacterRoute("character")

    object Management : CharacterRoute("character/management")
}
