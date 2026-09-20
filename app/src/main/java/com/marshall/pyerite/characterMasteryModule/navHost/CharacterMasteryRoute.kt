package com.marshall.pyerite.characterMasteryModule.navHost

import com.marshall.pyerite.characterMasteryModule.model.MasteryFilter
import com.marshall.pyerite.characterMasteryModule.viewModel.CharacterMasteryNavArgs

sealed class CharacterMasteryRoute(val route: String) {

    object Overview : CharacterMasteryRoute(
        "character/mastery/{${CharacterMasteryNavArgs.CHARACTER_ID}}",
    ) {
        fun create(characterId: Long) = "character/mastery/$characterId"
    }

    object Group : CharacterMasteryRoute(
        "character/mastery/{${CharacterMasteryNavArgs.CHARACTER_ID}}/" +
            "group/{${CharacterMasteryNavArgs.GROUP_ID}}/{${CharacterMasteryNavArgs.FILTER}}",
    ) {
        fun create(
            characterId: Long,
            groupId: Int,
            filter: MasteryFilter,
        ) = "character/mastery/$characterId/group/$groupId/${filter.name}"
    }
}
