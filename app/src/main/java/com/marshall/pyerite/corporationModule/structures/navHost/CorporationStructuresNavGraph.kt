package com.marshall.pyerite.corporationModule.structures.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.corporationModule.structures.ui.CorporationStructuresPage
import com.marshall.pyerite.corporationModule.structures.viewModel.CorporationStructuresViewModel

fun NavGraphBuilder.corporationStructuresNavGraph(
    navController: NavController,
) {
    composable(
        route = CorporationStructuresRoute.Structures.route,
        arguments = listOf(
            navArgument(CorporationStructuresViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CorporationStructuresPage(navController = navController)
    }
}
