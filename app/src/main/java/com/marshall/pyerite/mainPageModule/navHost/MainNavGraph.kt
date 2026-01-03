package com.marshall.pyerite.mainPageModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.marshall.pyerite.mainPageModule.MainPage

fun NavGraphBuilder.mainNavGraph(
    navController: NavController
) {
    composable(MainRoute.Root.route) {
        MainPage(navController)
    }
}
