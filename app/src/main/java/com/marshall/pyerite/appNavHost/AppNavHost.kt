package com.marshall.pyerite.appNavHost

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.marshall.pyerite.databaseHierarchyPage.navHostDatabasePage.databaseNavGraph
import com.marshall.pyerite.mainPageModule.navHostMainPage.MainRoute
import com.marshall.pyerite.mainPageModule.navHostMainPage.mainNavGraph

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoute.Root.route
    ) {
        mainNavGraph(navController)
        databaseNavGraph(navController)
    }
}