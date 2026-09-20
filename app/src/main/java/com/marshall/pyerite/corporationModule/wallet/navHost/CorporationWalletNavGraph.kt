package com.marshall.pyerite.corporationModule.wallet.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.corporationModule.wallet.ui.CorporationWalletDivisionPage
import com.marshall.pyerite.corporationModule.wallet.ui.CorporationWalletJournalDayPage
import com.marshall.pyerite.corporationModule.wallet.ui.CorporationWalletTransactionDayPage
import com.marshall.pyerite.corporationModule.wallet.ui.CorporationWalletsPage
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletDivisionViewModel
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletJournalDayViewModel
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletTransactionDayViewModel
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletsViewModel

fun NavGraphBuilder.corporationWalletNavGraph(
    navController: NavController,
) {
    composable(
        route = CorporationWalletRoute.Wallets.route,
        arguments = listOf(
            navArgument(CorporationWalletsViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CorporationWalletsPage(navController = navController)
    }
    composable(
        route = CorporationWalletRoute.Division.route,
        arguments = listOf(
            navArgument(CorporationWalletDivisionViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
            navArgument(CorporationWalletDivisionViewModel.NAV_ARG_DIVISION) {
                type = NavType.IntType
            },
        ),
    ) {
        CorporationWalletDivisionPage(navController = navController)
    }
    composable(
        route = CorporationWalletRoute.JournalDay.route,
        arguments = listOf(
            navArgument(CorporationWalletJournalDayViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
            navArgument(CorporationWalletJournalDayViewModel.NAV_ARG_DIVISION) {
                type = NavType.IntType
            },
            navArgument(CorporationWalletJournalDayViewModel.NAV_ARG_DAY_KEY) {
                type = NavType.StringType
            },
        ),
    ) {
        CorporationWalletJournalDayPage(navController = navController)
    }
    composable(
        route = CorporationWalletRoute.TransactionDay.route,
        arguments = listOf(
            navArgument(CorporationWalletTransactionDayViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
            navArgument(CorporationWalletTransactionDayViewModel.NAV_ARG_DIVISION) {
                type = NavType.IntType
            },
            navArgument(CorporationWalletTransactionDayViewModel.NAV_ARG_DAY_KEY) {
                type = NavType.StringType
            },
        ),
    ) {
        CorporationWalletTransactionDayPage(navController = navController)
    }
}
