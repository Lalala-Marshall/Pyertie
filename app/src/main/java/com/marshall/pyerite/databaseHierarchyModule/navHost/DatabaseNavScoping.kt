package com.marshall.pyerite.databaseHierarchyModule.navHost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/** Root graph entry for the shared database ViewModel across sub-pages. */
@Composable
fun rememberDatabaseRootBackStackEntry(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
): NavBackStackEntry = remember(backStackEntry) {
    navController.getBackStackEntry(DatabaseRoute.Root.route)
}
