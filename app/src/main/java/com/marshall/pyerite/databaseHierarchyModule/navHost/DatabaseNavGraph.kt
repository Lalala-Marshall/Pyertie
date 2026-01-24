package com.marshall.pyerite.databaseHierarchyModule.navHost

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.DatabaseHierarchyPage
import com.marshall.pyerite.databaseHierarchyModule.TypeDetailPage

fun NavGraphBuilder.databaseNavGraph(
    navController: NavController
) {
    navigation(
        route = DatabaseRoute.Root.route,
        startDestination = DatabaseRoute.Category.route
    ) {
        composable(DatabaseRoute.Category.route) {
            DatabaseHierarchyPage(
                title = stringResource(R.string.database),
                level = DatabaseLevel.CATEGORY,
                parentId = null,
                navController = navController
            )
        }

        composable(
            route = DatabaseRoute.Group.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStack ->
            DatabaseHierarchyPage(
                title = backStack.arguments?.getString("categoryName") ?: stringResource(R.string.group),
                level = DatabaseLevel.GROUP,
                parentId = backStack.arguments!!.getInt("categoryId"),
                navController = navController
            )
        }

        composable(
            route = DatabaseRoute.Type.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStack ->
            DatabaseHierarchyPage(
                title = backStack.arguments?.getString("groupName") ?: stringResource(R.string.type),
                level = DatabaseLevel.TYPE,
                parentId = backStack.arguments!!.getInt("groupId"),
                navController = navController
            )
        }

        composable(
            route = DatabaseRoute.TypeDetail.route,
            arguments = listOf(
                navArgument("typeId") { type = NavType.IntType }
            )
        ) { backStack ->
            TypeDetailPage(
                typeId = backStack.arguments!!.getInt("typeId")
            )
        }
    }
}
