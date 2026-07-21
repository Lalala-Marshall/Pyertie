package com.marshall.pyerite.characterModule.navHost

sealed class CharacterRoute(val route: String) {

    object Root : CharacterRoute("character")

    object Management : CharacterRoute("character/management")
}
