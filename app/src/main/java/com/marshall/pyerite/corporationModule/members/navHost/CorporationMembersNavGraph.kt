package com.marshall.pyerite.corporationModule.members.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.corporationModule.members.ui.CorporationMemberWatchPage
import com.marshall.pyerite.corporationModule.members.ui.CorporationMembersPage
import com.marshall.pyerite.corporationModule.members.viewModel.CorporationMembersViewModel

fun NavGraphBuilder.corporationMembersNavGraph(
    navController: NavController,
) {
    composable(
        route = CorporationMembersRoute.Members.route,
        arguments = listOf(
            navArgument(CorporationMembersViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CorporationMembersPage(navController = navController)
    }
    composable(
        route = CorporationMembersRoute.Watched.route,
        arguments = listOf(
            navArgument(CorporationMembersViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CorporationMemberWatchPage(navController = navController)
    }
}
