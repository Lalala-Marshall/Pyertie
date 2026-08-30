package com.marshall.pyerite.personalPropertyModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.personalPropertyModule.ui.PersonalPropertyPage
import com.marshall.pyerite.personalPropertyModule.viewModel.PersonalPropertyViewModel

fun NavGraphBuilder.personalPropertyNavGraph(
    navController: NavController,
) {
    composable(
        route = PersonalPropertyRoute.Root.route,
        arguments = listOf(
            navArgument(PersonalPropertyViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        PersonalPropertyPage(navController = navController)
    }
}
