package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.navHost.CharacterSkillsRoute
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CharacterSkillsPage(
    navController: NavController,
    viewModel: CharacterSkillsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_skills)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refresh,
        ),
    )

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = endActions,
    ) { topBarPadding ->
        PyeritePullToRefreshBox(
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = bottomPadding),
            ) {
                PageTitle(text = pageTitle)
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterSkillsCatalogSection(
                    onAttributesClick = {
                        navController.navigate(
                            CharacterSkillsRoute.Attributes.create(uiState.status.characterId),
                        )
                    },
                    onCatalogDetailsClick = {
                        navController.navigate(
                            CharacterSkillsRoute.CatalogDetails.create(uiState.status.characterId),
                        )
                    },
                    onSkillPlanClick = {
                        navController.navigate(
                            CharacterSkillsRoute.SkillPlan.create(uiState.status.characterId),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun CharacterSkillsCatalogSection(
    onAttributesClick: () -> Unit,
    onCatalogDetailsClick: () -> Unit,
    onSkillPlanClick: () -> Unit,
) {
    BaseContainer(
        title = stringResource(R.string.character_skills_catalog_section),
        useSystemBarsPadding = false,
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_attributes,
                itemName = stringResource(R.string.character_skills_attributes),
                onClick = onAttributesClick,
            ),
            showDivider = true,
        )
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_skills,
                itemName = stringResource(R.string.character_skills_catalog_details),
                onClick = onCatalogDetailsClick,
            ),
            showDivider = true,
        )
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_skill_plan,
                itemName = stringResource(R.string.character_skills_skill_plan),
                onClick = onSkillPlanClick,
            ),
            showDivider = false,
        )
    }
}
