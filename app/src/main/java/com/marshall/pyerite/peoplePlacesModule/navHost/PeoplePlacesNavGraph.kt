package com.marshall.pyerite.peoplePlacesModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.peoplePlacesModule.ui.PeoplePlacesSearchPage
import com.marshall.pyerite.peoplePlacesModule.viewModel.PeoplePlacesNavArgs

fun NavGraphBuilder.peoplePlacesNavGraph(navController: NavController) {
    composable(
        route = PeoplePlacesRoute.Search.route,
        arguments = listOf(
            navArgument(PeoplePlacesNavArgs.CHARACTER_ID) { type = NavType.LongType },
        ),
    ) {
        PeoplePlacesSearchPage(navController = navController)
    }
}
