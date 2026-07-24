package com.marshall.pyerite.charactersListModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.marshall.pyerite.charactersListModule.ui.CharacterManagementPage

fun NavGraphBuilder.charactersListNavGraph(
    navController: NavController,
) {
    navigation(
        route = CharacterRoute.Root.route,
        startDestination = CharacterRoute.Management.route,
    ) {
        composable(CharacterRoute.Management.route) {
            CharacterManagementPage(navController = navController)
        }
    }
}
