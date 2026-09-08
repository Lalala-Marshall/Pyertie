package com.marshall.pyerite.loyaltyPointsModule.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferItem
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyCorpOffersViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.util.NumberDisplayFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun LoyaltyCorpOffersPage(
    navController: NavController,
    viewModel: LoyaltyCorpOffersViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(localeController.contentLanguage) {
        viewModel.reloadForLanguage()
    }
    val listState = rememberLazyListState()
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val pageTitle = uiState.category?.displayName(localeController)?.ifBlank { null }
        ?: placeholder
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(listState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val sectionHeaderTextSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val sectionHeaderBottomPadding = dimensionResource(R.dimen.list_section_header_bottom_padding)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refresh,
        ),
    )
    val lpColor = colorResource(R.color.loyalty_points_lp)
    val iskColor = colorResource(R.color.character_status_positive)
    val operatorColor = colorResource(R.color.text_primary)
    val timesSeparator = stringResource(R.string.loyalty_points_times_separator)
    val plusSeparator = stringResource(R.string.loyalty_points_plus_separator)

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
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "title") {
                    PageTitle(text = pageTitle)
                }
                if (uiState.loadFailed) {
                    item(key = "load_failed") {
                        Spacer(modifier = Modifier.height(sectionGap))
                        LoyaltyPointsLoadFailedBanner(onRetry = viewModel::refresh)
                    }
                }
                item(key = "section_header") {
                    Text(
                        text = stringResource(R.string.loyalty_points_offers),
                        fontSize = sectionHeaderTextSize,
                        fontWeight = FontWeight.Black,
                        color = colorResource(R.color.text_primary),
                        modifier = Modifier.padding(
                            start = titleStartPadding,
                            top = sectionGap,
                            bottom = sectionHeaderBottomPadding,
                        ),
                    )
                }
                when {
                    !uiState.detailsReady -> {
                        item(key = "pending") {
                            LoyaltyPointsSectionItemRow(
                                model = BaseLazyColumnItemModel(
                                    showLeadingIcon = false,
                                    itemName = placeholder,
                                    showChevron = false,
                                    onClick = null,
                                ),
                                showDivider = false,
                                indexInSection = 0,
                                sectionItemCount = 1,
                            )
                        }
                    }
                    uiState.offers.isEmpty() -> {
                        item(key = "empty") {
                            LoyaltyPointsSectionItemRow(
                                model = BaseLazyColumnItemModel(
                                    showLeadingIcon = false,
                                    itemName = stringResource(R.string.loyalty_points_offers_empty),
                                    showChevron = false,
                                    onClick = null,
                                ),
                                showDivider = false,
                                indexInSection = 0,
                                sectionItemCount = 1,
                            )
                        }
                    }
                    else -> {
                        itemsIndexed(
                            items = uiState.offers,
                            key = { _, offer -> offer.offerId },
                        ) { index, offer ->
                            LoyaltyPointsSectionItemRow(
                                model = loyaltyOfferItemModel(
                                    offer = offer,
                                    localeController = localeController,
                                    placeholder = placeholder,
                                    lpColor = lpColor,
                                    iskColor = iskColor,
                                    operatorColor = operatorColor,
                                    timesSeparator = timesSeparator,
                                    plusSeparator = plusSeparator,
                                    onClick = {
                                        navController.navigate(
                                            DatabaseRoute.TypeDetail.create(offer.typeId),
                                        )
                                    },
                                ),
                                showDivider = index != uiState.offers.lastIndex,
                                indexInSection = index,
                                sectionItemCount = uiState.offers.size,
                            )
                        }
                    }
                }
                item(key = "bottom") {
                    Spacer(Modifier.height(bottomPadding))
                }
            }
        }
    }
}

@Composable
private fun loyaltyOfferItemModel(
    offer: LoyaltyOfferItem,
    localeController: LocaleController,
    placeholder: String,
    lpColor: Color,
    iskColor: Color,
    operatorColor: Color,
    timesSeparator: String,
    plusSeparator: String,
    onClick: () -> Unit,
): BaseLazyColumnItemModel {
    val typeName = offer.displayName(localeController).ifBlank { placeholder }
    val lpText = stringResource(
        R.string.loyalty_points_lp_value,
        NumberDisplayFormatter.format(offer.lpCost, NumberDisplayFormatter.Style.FULL),
    )
    val iskText = stringResource(
        R.string.loyalty_points_isk_value,
        NumberDisplayFormatter.format(offer.iskCost, NumberDisplayFormatter.Style.COMPACT),
    )
    val operatorStyle = SpanStyle(
        color = operatorColor,
        fontWeight = FontWeight.Normal,
    )
    val titleAnnotated = buildAnnotatedString {
        append(offer.quantity.toString())
        withStyle(operatorStyle) { append(timesSeparator) }
        append(typeName)
    }
    val costHint = buildAnnotatedString {
        withStyle(SpanStyle(color = lpColor)) { append(lpText) }
        withStyle(operatorStyle) { append(plusSeparator) }
        withStyle(SpanStyle(color = iskColor)) { append(iskText) }
    }
    val hints = mutableListOf(
        BaseLazyColumnItemHint(
            annotatedText = costHint,
            emphasized = true,
        ),
    )
    if (offer.requiredItems.isNotEmpty()) {
        val valueText = offer.requiredItemsIskValue?.let { value ->
            NumberDisplayFormatter.format(value, NumberDisplayFormatter.Style.COMPACT)
        } ?: placeholder
        hints += BaseLazyColumnItemHint(
            text = stringResource(R.string.loyalty_points_required_items, valueText),
        )
        offer.requiredItems.forEach { required ->
            val requiredName = required.displayName(localeController).ifBlank { placeholder }
            hints += BaseLazyColumnItemHint(
                iconFileName = required.iconFilename,
                annotatedText = buildAnnotatedString {
                    append(required.quantity.toString())
                    withStyle(operatorStyle) { append(timesSeparator) }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(requiredName)
                    }
                },
            )
        }
    }
    return BaseLazyColumnItemModel(
        iconRes = R.drawable.ic_database,
        iconFileName = offer.iconFilename,
        iconSize = dimensionResource(R.dimen.base_lazy_column_item_icon_size),
        iconOnLightPlate = true,
        itemName = typeName,
        itemNameAnnotated = titleAnnotated,
        itemHints = hints,
        alignHintLeadingColumn = false,
        showChevron = true,
        onClick = onClick,
    )
}
