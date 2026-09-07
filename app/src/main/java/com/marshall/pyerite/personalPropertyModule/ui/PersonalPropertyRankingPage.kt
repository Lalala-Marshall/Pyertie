package com.marshall.pyerite.personalPropertyModule.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyCategory
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyConfig
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyDecoratedItem
import com.marshall.pyerite.personalPropertyModule.viewModel.PersonalPropertyRankingViewModel
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
internal fun PersonalPropertyRankingPage(
    navController: NavController,
    viewModel: PersonalPropertyRankingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val localeController: LocaleController = koinInject()
    val scrollState = rememberScrollState()
    val categoryTitle = stringResource(viewModel.category.titleRes())
    val pageTitle = stringResource(
        R.string.personal_property_ranking_title,
        categoryTitle,
        PersonalPropertyConfig.RANKING_TOP_N,
    )
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val detailsPending = !uiState.detailsReady
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
                    PersonalPropertyRankingLoadFailedBanner(onRetry = viewModel::refresh)
                    Spacer(modifier = Modifier.height(sectionGap))
                }
                RankingItemsContainer(
                    title = stringResource(
                        R.string.personal_property_ranking_priced,
                        PersonalPropertyConfig.RANKING_TOP_N,
                    ),
                    items = uiState.ranking.priced,
                    detailsPending = detailsPending,
                    placeholder = placeholder,
                    localeController = localeController,
                    onItemClick = { typeId ->
                        navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                    },
                )
                Spacer(modifier = Modifier.height(sectionGap))
                RankingItemsContainer(
                    title = stringResource(
                        R.string.personal_property_ranking_unpriced,
                        PersonalPropertyConfig.RANKING_TOP_N,
                    ),
                    items = uiState.ranking.unpriced,
                    detailsPending = detailsPending,
                    placeholder = placeholder,
                    localeController = localeController,
                    onItemClick = { typeId ->
                        navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                    },
                )
            }
        }
    }
}

@Composable
private fun PersonalPropertyRankingLoadFailedBanner(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.personal_property_load_failed),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.character_sheet_retry))
        }
    }
}

@Composable
private fun RankingItemsContainer(
    title: String,
    items: List<PersonalPropertyDecoratedItem>,
    detailsPending: Boolean,
    placeholder: String,
    localeController: LocaleController,
    onItemClick: (Int) -> Unit,
) {
    BaseContainer(
        title = title,
        useSystemBarsPadding = false,
    ) {
        when {
            detailsPending -> {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = placeholder,
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = false,
                )
            }
            items.isEmpty() -> {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.personal_property_ranking_empty),
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = false,
                )
            }
            else -> {
                items.forEachIndexed { index, item ->
                    RankingItemRow(
                        item = item,
                        localeController = localeController,
                        placeholder = placeholder,
                        showDivider = index != items.lastIndex,
                        onClick = { onItemClick(item.typeId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingItemRow(
    item: PersonalPropertyDecoratedItem,
    localeController: LocaleController,
    placeholder: String,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    val name = item.displayName(localeController).ifBlank { placeholder }
    val quantityText = NumberDisplayFormatter.format(
        item.quantity,
        NumberDisplayFormatter.Style.FULL,
    )
    val unitPriceText = formatCompactIskAmount(item.unitPrice)
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconFileName = item.iconFilename?.takeIf { it.isNotBlank() },
            iconOnLightPlate = true,
            itemName = name,
            itemHint = stringResource(
                R.string.personal_property_ranking_qty_price,
                quantityText,
                unitPriceText,
            ),
            trailingValue = formatCompactIsk(
                value = item.totalIsk,
                detailsReady = true,
                placeholder = placeholder,
            ),
            showChevron = true,
            onClick = onClick,
        ),
        showDivider = showDivider,
    )
}

@Composable
private fun formatCompactIskAmount(value: Double?): String {
    if (value == null || value == 0.0) {
        return stringResource(R.string.personal_property_isk_zero)
    }
    return stringResource(
        R.string.personal_property_isk_value,
        NumberDisplayFormatter.format(value, NumberDisplayFormatter.Style.COMPACT),
    )
}

@StringRes
private fun PersonalPropertyCategory.titleRes(): Int = when (this) {
    PersonalPropertyCategory.ASSETS -> R.string.personal_property_assets
    PersonalPropertyCategory.IMPLANTS -> R.string.personal_property_implants
    PersonalPropertyCategory.MARKET_ORDERS -> R.string.personal_property_market_orders
    PersonalPropertyCategory.CONTRACTS -> R.string.personal_property_contracts
}
