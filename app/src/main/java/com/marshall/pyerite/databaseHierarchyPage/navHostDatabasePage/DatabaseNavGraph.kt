package com.marshall.pyerite.databaseHierarchyPage.navHostDatabasePage

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.marshall.pyerite.databaseHierarchyPage.DatabaseHierarchyPage

fun NavGraphBuilder.databaseNavGraph(
    navController: NavController
) {
    navigation(
        route = DatabaseRoute.Root.route,
        startDestination = DatabaseRoute.Category.route
    ) {
        composable(DatabaseRoute.Category.route) {
            DatabaseHierarchyPage(
                title = "Category",
                level = DatabaseLevel.CATEGORY,
                parentId = null,
                navController = navController
            )
        }

        composable(
            route = DatabaseRoute.Group.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType }
            )
        ) { backStack ->
            DatabaseHierarchyPage(
                title = "Group",
                level = DatabaseLevel.GROUP,
                parentId = backStack.arguments!!.getInt("categoryId"),
                navController = navController
            )
        }

        composable(
            route = DatabaseRoute.Type.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType }
            )
        ) { backStack ->
            DatabaseHierarchyPage(
                title = "Type",
                level = DatabaseLevel.TYPE,
                parentId = backStack.arguments!!.getInt("groupId"),
                navController = navController
            )
        }
    }
}
