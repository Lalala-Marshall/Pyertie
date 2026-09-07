package com.marshall.pyerite.personalPropertyModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.personalPropertyModule.ui.PersonalPropertyPage
import com.marshall.pyerite.personalPropertyModule.ui.PersonalPropertyRankingPage
import com.marshall.pyerite.personalPropertyModule.viewModel.PersonalPropertyRankingViewModel
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
    composable(
        route = PersonalPropertyRoute.Ranking.route,
        arguments = listOf(
            navArgument(PersonalPropertyRankingViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
            navArgument(PersonalPropertyRankingViewModel.NAV_ARG_CATEGORY) {
                type = NavType.StringType
            },
        ),
    ) {
        PersonalPropertyRankingPage(navController = navController)
    }
}
