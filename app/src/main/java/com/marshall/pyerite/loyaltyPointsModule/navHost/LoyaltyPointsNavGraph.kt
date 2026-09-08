package com.marshall.pyerite.loyaltyPointsModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.loyaltyPointsModule.ui.LoyaltyCorpDetailPage
import com.marshall.pyerite.loyaltyPointsModule.ui.LoyaltyCorpOffersPage
import com.marshall.pyerite.loyaltyPointsModule.ui.LoyaltyCorpStationsPage
import com.marshall.pyerite.loyaltyPointsModule.ui.LoyaltyFactionCorpsPage
import com.marshall.pyerite.loyaltyPointsModule.ui.LoyaltyPointsHubPage
import com.marshall.pyerite.loyaltyPointsModule.ui.LoyaltyStorePage
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyPointsNavArgs

fun NavGraphBuilder.loyaltyPointsNavGraph(
    navController: NavController,
) {
    composable(
        route = LoyaltyPointsRoute.Hub.route,
        arguments = listOf(
            navArgument(LoyaltyPointsNavArgs.CHARACTER_ID) { type = NavType.LongType },
        ),
    ) {
        LoyaltyPointsHubPage(navController = navController)
    }
    composable(
        route = LoyaltyPointsRoute.Store.route,
        arguments = listOf(
            navArgument(LoyaltyPointsNavArgs.CHARACTER_ID) { type = NavType.LongType },
        ),
    ) {
        LoyaltyStorePage(navController = navController)
    }
    composable(
        route = LoyaltyPointsRoute.FactionCorps.route,
        arguments = listOf(
            navArgument(LoyaltyPointsNavArgs.CHARACTER_ID) { type = NavType.LongType },
            navArgument(LoyaltyPointsNavArgs.FACTION_ID) { type = NavType.IntType },
        ),
    ) {
        LoyaltyFactionCorpsPage(navController = navController)
    }
    composable(
        route = LoyaltyPointsRoute.CorpDetail.route,
        arguments = listOf(
            navArgument(LoyaltyPointsNavArgs.CHARACTER_ID) { type = NavType.LongType },
            navArgument(LoyaltyPointsNavArgs.CORPORATION_ID) { type = NavType.LongType },
        ),
    ) {
        LoyaltyCorpDetailPage(navController = navController)
    }
    composable(
        route = LoyaltyPointsRoute.Stations.route,
        arguments = listOf(
            navArgument(LoyaltyPointsNavArgs.CHARACTER_ID) { type = NavType.LongType },
            navArgument(LoyaltyPointsNavArgs.CORPORATION_ID) { type = NavType.LongType },
        ),
    ) {
        LoyaltyCorpStationsPage(navController = navController)
    }
    composable(
        route = LoyaltyPointsRoute.Offers.route,
        arguments = listOf(
            navArgument(LoyaltyPointsNavArgs.CHARACTER_ID) { type = NavType.LongType },
            navArgument(LoyaltyPointsNavArgs.CORPORATION_ID) { type = NavType.LongType },
            navArgument(LoyaltyPointsNavArgs.CATEGORY_ID) { type = NavType.IntType },
        ),
    ) {
        LoyaltyCorpOffersPage(navController = navController)
    }
}
