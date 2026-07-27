package com.marshall.pyerite.characterClonesModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.characterClonesModule.ui.CharacterClonesPage
import com.marshall.pyerite.characterClonesModule.viewModel.CharacterClonesViewModel

fun NavGraphBuilder.characterClonesNavGraph(
    navController: NavController,
) {
    composable(
        route = CharacterClonesRoute.Status.route,
        arguments = listOf(
            navArgument(CharacterClonesViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterClonesPage(navController = navController)
    }
}
