package com.marshall.pyerite.appNavHost

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.marshall.pyerite.characterModule.navHost.characterNavGraph
import com.marshall.pyerite.databaseHierarchyModule.navHost.databaseNavGraph
import com.marshall.pyerite.mainPageModule.navHost.MainRoute
import com.marshall.pyerite.mainPageModule.navHost.mainNavGraph

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoute.Root.route
    ) {
        mainNavGraph(navController)
        databaseNavGraph(navController)
        characterNavGraph(navController)
    }
}