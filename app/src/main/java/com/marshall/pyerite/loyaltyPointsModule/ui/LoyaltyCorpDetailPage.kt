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
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferCategoryItem
import com.marshall.pyerite.loyaltyPointsModule.navHost.LoyaltyPointsRoute
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyCorpDetailViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.util.NumberDisplayFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun LoyaltyCorpDetailPage(
    navController: NavController,
    viewModel: LoyaltyCorpDetailViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(localeController.contentLanguage) {
        viewModel.reloadForLanguage()
    }
    val scrollState = rememberScrollState()
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val pageTitle = uiState.corporation?.displayName(localeController)?.ifBlank { null }
        ?: placeholder
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

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
            Spacer(modifier = Modifier.height(sectionGap))
            BaseContainer(useSystemBarsPadding = false) {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        iconRes = R.drawable.ic_loyalty_station,
                        itemName = stringResource(R.string.loyalty_points_find_station),
                        showChevron = true,
                        onClick = {
                            navController.navigate(
                                LoyaltyPointsRoute.Stations.create(
                                    viewModel.characterId,
                                    viewModel.corporationId,
                                ),
                            )
                        },
                    ),
                    showDivider = false,
                )
            }
            Spacer(modifier = Modifier.height(sectionGap))
            LoyaltyCorpCategoriesSection(
                categories = uiState.categories,
                detailsPending = !uiState.detailsReady,
                placeholder = placeholder,
                localeController = localeController,
                onCategoryClick = { categoryId ->
                    navController.navigate(
                        LoyaltyPointsRoute.Offers.create(
                            viewModel.characterId,
                            viewModel.corporationId,
                            categoryId,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun LoyaltyCorpCategoriesSection(
    categories: List<LoyaltyOfferCategoryItem>,
    detailsPending: Boolean,
    placeholder: String,
    localeController: LocaleController,
    onCategoryClick: (Int) -> Unit,
) {
    BaseContainer(
        title = stringResource(R.string.loyalty_points_offer_categories),
        useSystemBarsPadding = false,
    ) {
        when {
            detailsPending -> LoyaltyPointsPlaceholderRow(placeholder)
            categories.isEmpty() -> LoyaltyPointsPlaceholderRow(
                stringResource(R.string.loyalty_points_categories_empty),
            )
            else -> categories.forEachIndexed { index, category ->
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        iconRes = R.drawable.ic_database,
                        iconFileName = category.iconFilename,
                        iconOnLightPlate = true,
                        itemName = category.displayName(localeController).ifBlank { placeholder },
                        trailingValue = NumberDisplayFormatter.format(
                            category.offerCount.toLong(),
                            NumberDisplayFormatter.Style.FULL,
                        ),
                        showChevron = true,
                        onClick = { onCategoryClick(category.categoryId) },
                    ),
                    showDivider = index != categories.lastIndex,
                )
            }
        }
    }
}
