package com.marshall.pyerite.corporationModule.wallet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.corporationModule.wallet.navHost.CorporationWalletRoute
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletsViewModel
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

@Composable
internal fun CorporationWalletsPage(
    navController: NavController,
    viewModel: CorporationWalletsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.corporation_wallet)
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
                if (uiState.permissionDenied || uiState.loadFailed) {
                    CorporationWalletStatusBanner(
                        permissionDenied = uiState.permissionDenied,
                        loadFailed = uiState.loadFailed,
                        onRetry = viewModel::refresh,
                    )
                    Spacer(modifier = Modifier.height(sectionGap))
                }
                when {
                    uiState.permissionDenied -> Unit
                    uiState.divisions.isEmpty() && !uiState.isLoading -> {
                        Text(
                            text = stringResource(R.string.corporation_wallet_empty),
                            color = colorResource(R.color.hint_text),
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding),
                            ),
                        )
                    }
                    uiState.divisions.isNotEmpty() -> {
                        BaseContainer(useSystemBarsPadding = false) {
                            uiState.divisions.forEachIndexed { index, division ->
                                BaseLazyColumnItem(
                                    model = BaseLazyColumnItemModel(
                                        iconRes = R.drawable.ic_corporation_wallet,
                                        itemName = corporationWalletDivisionTitle(
                                            division = division.division,
                                            name = division.name,
                                        ),
                                        itemHint = formatWalletIsk(
                                            value = division.balance,
                                            style = NumberDisplayFormatter.Style.FULL,
                                        ),
                                        onClick = {
                                            navController.navigate(
                                                CorporationWalletRoute.Division.create(
                                                    characterId = viewModel.characterId,
                                                    division = division.division,
                                                ),
                                            )
                                        },
                                    ),
                                    showDivider = index < uiState.divisions.lastIndex,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
