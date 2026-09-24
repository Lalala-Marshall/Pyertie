package com.marshall.pyerite.corporationModule.structures.navHost

sealed class CorporationStructuresRoute(val route: String) {
    object Structures : CorporationStructuresRoute("corporation/structures/{characterId}") {
        fun create(characterId: Long) = "corporation/structures/$characterId"
    }
}
