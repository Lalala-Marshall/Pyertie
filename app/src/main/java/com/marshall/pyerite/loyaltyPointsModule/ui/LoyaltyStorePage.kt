package com.marshall.pyerite.loyaltyPointsModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyFactionItem
import com.marshall.pyerite.loyaltyPointsModule.navHost.LoyaltyPointsRoute
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyStoreViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun LoyaltyStorePage(
    navController: NavController,
    viewModel: LoyaltyStoreViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(localeController.contentLanguage) {
        viewModel.reloadForLanguage()
    }
    val characterId = viewModel.characterId
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.loyalty_points_store)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
    ) { topBarPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding)
                .verticalScroll(scrollState)
                .padding(bottom = bottomPadding),
        ) {
            PageTitle(text = pageTitle)
            if (uiState.loadFailed) {
                Spacer(modifier = Modifier.height(sectionGap))
                LoyaltyPointsLoadFailedBanner(onRetry = viewModel::refresh)
            }
            Spacer(modifier = Modifier.height(sectionGap))
            LoyaltyStoreFactionsSection(
                factions = uiState.factions,
                detailsPending = !uiState.detailsReady,
                placeholder = placeholder,
                localeController = localeController,
                onFactionClick = { factionId ->
                    navController.navigate(
                        LoyaltyPointsRoute.FactionCorps.create(characterId, factionId),
                    )
                },
            )
        }
    }
}

@Composable
private fun LoyaltyStoreFactionsSection(
    factions: List<LoyaltyFactionItem>,
    detailsPending: Boolean,
    placeholder: String,
    localeController: LocaleController,
    onFactionClick: (Int) -> Unit,
) {
    BaseContainer(
        title = stringResource(R.string.loyalty_points_factions),
        useSystemBarsPadding = false,
    ) {
        when {
            detailsPending -> LoyaltyPointsPlaceholderRow(placeholder)
            factions.isEmpty() -> LoyaltyPointsPlaceholderRow(
                stringResource(R.string.loyalty_points_factions_empty),
            )
            else -> factions.forEachIndexed { index, faction ->
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        iconRes = R.drawable.ic_loyalty_store,
                        iconFileName = faction.iconFilename,
                        itemName = faction.displayName(localeController).ifBlank { placeholder },
                        showChevron = true,
                        onClick = { onFactionClick(faction.factionId) },
                    ),
                    showDivider = index != factions.lastIndex,
                )
            }
        }
    }
}
