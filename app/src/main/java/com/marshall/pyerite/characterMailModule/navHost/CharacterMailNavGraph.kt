package com.marshall.pyerite.characterMailModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.characterMailModule.ui.CharacterMailAllPage
import com.marshall.pyerite.characterMailModule.ui.CharacterMailPage

fun NavGraphBuilder.characterMailNavGraph(
    navController: NavController,
) {
    val characterIdArgument = navArgument(CharacterMailRoute.ARG_CHARACTER_ID) {
        type = NavType.LongType
    }
    composable(
        route = CharacterMailRoute.Root.route,
        arguments = listOf(characterIdArgument),
    ) { entry ->
        val characterId = checkNotNull(
            entry.arguments?.getLong(CharacterMailRoute.ARG_CHARACTER_ID),
        ) {
            "Missing ${CharacterMailRoute.ARG_CHARACTER_ID}"
        }
        CharacterMailPage(
            navController = navController,
            characterId = characterId,
        )
    }
    composable(
        route = CharacterMailRoute.All.route,
        arguments = listOf(characterIdArgument),
    ) {
        CharacterMailAllPage(navController = navController)
    }
}
