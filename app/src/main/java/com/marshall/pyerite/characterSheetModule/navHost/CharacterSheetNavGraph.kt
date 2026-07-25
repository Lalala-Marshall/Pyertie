package com.marshall.pyerite.characterSheetModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.characterSheetModule.ui.CharacterSheetPage
import com.marshall.pyerite.characterSheetModule.viewModel.CharacterSheetViewModel

fun NavGraphBuilder.characterSheetNavGraph(
    navController: NavController,
) {
    composable(
        route = CharacterSheetRoute.Sheet.route,
        arguments = listOf(
            navArgument(CharacterSheetViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterSheetPage(navController = navController)
    }
}
