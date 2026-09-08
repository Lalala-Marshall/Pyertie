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
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationBalance
import com.marshall.pyerite.loyaltyPointsModule.navHost.LoyaltyPointsRoute
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyPointsHubViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.util.NumberDisplayFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun LoyaltyPointsHubPage(
    navController: NavController,
    viewModel: LoyaltyPointsHubViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(localeController.contentLanguage) {
        viewModel.reloadForLanguage()
    }
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.loyalty_points)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
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
                if (uiState.loadFailed) {
                    Spacer(modifier = Modifier.height(sectionGap))
                    LoyaltyPointsLoadFailedBanner(onRetry = viewModel::refresh)
                }
                Spacer(modifier = Modifier.height(sectionGap))
                LoyaltyPointsBalancesSection(
                    balances = uiState.balances,
                    detailsPending = !uiState.detailsReady,
                    placeholder = placeholder,
                    localeController = localeController,
                    onCorporationClick = { corporationId ->
                        navController.navigate(
                            LoyaltyPointsRoute.CorpDetail.create(
                                viewModel.characterId,
                                corporationId,
                            ),
                        )
                    },
                )
                Spacer(modifier = Modifier.height(sectionGap))
                LoyaltyPointsStoreEntrySection(
                    onClick = {
                        navController.navigate(
                            LoyaltyPointsRoute.Store.create(viewModel.characterId),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun LoyaltyPointsBalancesSection(
    balances: List<LoyaltyCorporationBalance>,
    detailsPending: Boolean,
    placeholder: String,
    localeController: LocaleController,
    onCorporationClick: (Long) -> Unit,
) {
    BaseContainer(
        title = stringResource(R.string.loyalty_points_basic_info),
        useSystemBarsPadding = false,
    ) {
        when {
            detailsPending -> LoyaltyPointsPlaceholderRow(placeholder)
            balances.isEmpty() -> LoyaltyPointsPlaceholderRow(
                stringResource(R.string.loyalty_points_corps_empty),
            )
            else -> balances.forEachIndexed { index, balance ->
                val lpHint = stringResource(
                    R.string.loyalty_points_hint,
                    NumberDisplayFormatter.format(
                        balance.loyaltyPoints,
                        NumberDisplayFormatter.Style.COMPACT,
                    ),
                )
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        iconRes = R.drawable.ic_loyalty_store,
                        iconFileName = balance.iconFilename,
                        iconUrl = balance.iconUrl,
                        itemName = balance.displayName(localeController).ifBlank { placeholder },
                        itemHint = lpHint,
                        showChevron = true,
                        onClick = { onCorporationClick(balance.corporationId) },
                    ),
                    showDivider = index != balances.lastIndex,
                    trailingContent = if (balance.isMilitia) {
                        { LoyaltyMilitiaTag() }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun LoyaltyPointsStoreEntrySection(onClick: () -> Unit) {
    BaseContainer(
        title = stringResource(R.string.loyalty_points_store),
        useSystemBarsPadding = false,
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_loyalty_store,
                itemName = stringResource(R.string.loyalty_points_store),
                showChevron = true,
                onClick = onClick,
            ),
            showDivider = false,
        )
    }
}
