package com.marshall.pyerite.characterModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.marshall.pyerite.characterModule.CharacterManagementPage

fun NavGraphBuilder.characterNavGraph(
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
