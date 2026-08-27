package com.marshall.pyerite.characterCalendarModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.characterCalendarModule.ui.CharacterCalendarPage
import com.marshall.pyerite.characterCalendarModule.viewModel.CharacterCalendarViewModel

fun NavGraphBuilder.characterCalendarNavGraph(
    navController: NavController,
) {
    composable(
        route = CharacterCalendarRoute.Root.route,
        arguments = listOf(
            navArgument(CharacterCalendarViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterCalendarPage(navController = navController)
    }
}
