package com.marshall.pyerite.personalPropertyModule.navHost

import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyCategory

sealed class PersonalPropertyRoute(val route: String) {
    object Root : PersonalPropertyRoute("character/property/{characterId}") {
        fun create(characterId: Long) = "character/property/$characterId"
    }

    object Ranking : PersonalPropertyRoute(
        "character/property/{characterId}/ranking/{category}",
    ) {
        internal fun create(characterId: Long, category: PersonalPropertyCategory) =
            "character/property/$characterId/ranking/${category.routeValue}"
    }
}
