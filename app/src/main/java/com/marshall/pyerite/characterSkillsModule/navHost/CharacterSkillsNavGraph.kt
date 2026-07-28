package com.marshall.pyerite.characterSkillsModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.characterSkillsModule.ui.CharacterSkillsPage
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel

fun NavGraphBuilder.characterSkillsNavGraph(
    navController: NavController,
) {
    composable(
        route = CharacterSkillsRoute.Queue.route,
        arguments = listOf(
            navArgument(CharacterSkillsViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterSkillsPage(navController = navController)
    }
}
