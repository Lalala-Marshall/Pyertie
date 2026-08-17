package com.marshall.pyerite.characterMailModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.characterMailModule.ui.CharacterMailDetailPage
import com.marshall.pyerite.characterMailModule.ui.CharacterMailListPage
import com.marshall.pyerite.characterMailModule.ui.CharacterMailPage
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailDetailViewModel
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailViewModel

fun NavGraphBuilder.characterMailNavGraph(
    navController: NavController,
) {
    val characterIdArgument = navArgument(CharacterMailViewModel.NAV_ARG_CHARACTER_ID) {
        type = NavType.LongType
    }
    composable(
        route = CharacterMailRoute.Root.route,
        arguments = listOf(characterIdArgument),
    ) {
        CharacterMailPage(
            navController = navController,
        )
    }
    composable(
        route = CharacterMailRoute.List.route,
        arguments = listOf(
            characterIdArgument,
            navArgument(CharacterMailViewModel.NAV_ARG_LABEL_ID) {
                type = NavType.IntType
                defaultValue = CharacterMailViewModel.NAV_LABEL_ID_UNFILTERED
            },
        ),
    ) {
        CharacterMailListPage(navController = navController)
    }
    composable(
        route = CharacterMailRoute.Detail.route,
        arguments = listOf(
            characterIdArgument,
            navArgument(CharacterMailDetailViewModel.NAV_ARG_MAIL_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterMailDetailPage(navController = navController)
    }
}
