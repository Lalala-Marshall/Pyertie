package com.marshall.pyerite.peoplePlacesModule.navHost

import com.marshall.pyerite.peoplePlacesModule.viewModel.PeoplePlacesNavArgs

sealed class PeoplePlacesRoute(val route: String) {

    object Search : PeoplePlacesRoute(
        "character/people-places/{${PeoplePlacesNavArgs.CHARACTER_ID}}",
    ) {
        fun create(characterId: Long) = "character/people-places/$characterId"
    }
}
