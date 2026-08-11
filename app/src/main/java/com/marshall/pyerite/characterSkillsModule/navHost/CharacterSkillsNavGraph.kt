package com.marshall.pyerite.characterSkillsModule.navHost

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marshall.pyerite.characterSkillsModule.ui.CharacterAttributesPage
import com.marshall.pyerite.characterSkillsModule.ui.CharacterSkillsCatalogDetailsPage
import com.marshall.pyerite.characterSkillsModule.ui.CharacterSkillsCatalogGroupPage
import com.marshall.pyerite.characterSkillsModule.ui.CharacterSkillsPage
import com.marshall.pyerite.characterSkillsModule.ui.CharacterSkillsSkillPlanDetailPage
import com.marshall.pyerite.characterSkillsModule.ui.CharacterSkillsSkillPlanPage
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.characterSkillsModule.viewModel.SkillPlanViewModel

fun NavGraphBuilder.characterSkillsNavGraph(
    navController: NavController,
) {
    composable(
        route = CharacterSkillsRoute.Queue.route,
        arguments = listOf(
            navArgument(CharacterSkillsViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterSkillsPage(navController = navController)
    }
    composable(
        route = CharacterSkillsRoute.Attributes.route,
        arguments = listOf(
            navArgument(CharacterSkillsViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterAttributesPage(navController = navController)
    }
    composable(
        route = CharacterSkillsRoute.CatalogGroup.route,
        arguments = listOf(
            navArgument(CharacterSkillsViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
            navArgument(CharacterSkillsViewModel.NAV_ARG_GROUP_ID) {
                type = NavType.IntType
            },
            navArgument(CharacterSkillsViewModel.NAV_ARG_CATALOG_FILTER) {
                type = NavType.StringType
            },
        ),
    ) {
        CharacterSkillsCatalogGroupPage(navController = navController)
    }
    composable(
        route = CharacterSkillsRoute.CatalogDetails.route,
        arguments = listOf(
            navArgument(CharacterSkillsViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterSkillsCatalogDetailsPage(navController = navController)
    }
    composable(
        route = CharacterSkillsRoute.SkillPlan.route,
        arguments = listOf(
            navArgument(CharacterSkillsViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterSkillsSkillPlanPage(navController = navController)
    }
    composable(
        route = CharacterSkillsRoute.SkillPlanDetail.route,
        arguments = listOf(
            navArgument(CharacterSkillsViewModel.NAV_ARG_CHARACTER_ID) {
                type = NavType.LongType
            },
            navArgument(SkillPlanViewModel.NAV_ARG_PLAN_ID) {
                type = NavType.LongType
            },
        ),
    ) {
        CharacterSkillsSkillPlanDetailPage(navController = navController)
    }
}
