package com.marshall.pyerite.characterMasteryModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.characterMasteryModule.ui.CharacterMasteryGroupPage
import com.marshall.pyerite.characterMasteryModule.ui.CharacterMasteryOverviewPage
import com.marshall.pyerite.characterMasteryModule.viewModel.CharacterMasteryNavArgs

fun NavGraphBuilder.characterMasteryNavGraph(
    navController: NavController,
) {
    composable(
        route = CharacterMasteryRoute.Overview.route,
        arguments = listOf(
            navArgument(CharacterMasteryNavArgs.CHARACTER_ID) { type = NavType.LongType },
        ),
    ) {
        CharacterMasteryOverviewPage(navController = navController)
    }
    composable(
        route = CharacterMasteryRoute.Group.route,
        arguments = listOf(
            navArgument(CharacterMasteryNavArgs.CHARACTER_ID) { type = NavType.LongType },
            navArgument(CharacterMasteryNavArgs.GROUP_ID) { type = NavType.IntType },
            navArgument(CharacterMasteryNavArgs.FILTER) { type = NavType.StringType },
        ),
    ) {
        CharacterMasteryGroupPage(navController = navController)
    }
}
