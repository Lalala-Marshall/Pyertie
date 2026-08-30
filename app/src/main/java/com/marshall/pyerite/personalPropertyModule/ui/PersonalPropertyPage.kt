package com.marshall.pyerite.personalPropertyModule.ui

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
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyBucket
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertySummary
import com.marshall.pyerite.personalPropertyModule.viewModel.PersonalPropertyViewModel
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
internal fun PersonalPropertyPage(
    navController: NavController,
    viewModel: PersonalPropertyViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.personal_property)
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
                    PersonalPropertyLoadFailedBanner(onRetry = viewModel::refresh)
                    Spacer(modifier = Modifier.height(sectionGap))
                }
                PersonalPropertyTotalSection(
                    summary = uiState.summary,
                    detailsPending = detailsPending,
                    placeholder = placeholder,
                )
                Spacer(modifier = Modifier.height(sectionGap))
                PersonalPropertyBreakdownSection(
                    summary = uiState.summary,
                    detailsPending = detailsPending,
                    placeholder = placeholder,
                )
                Spacer(modifier = Modifier.height(sectionGap))
                PersonalPropertyDistributionSection(
                    summary = uiState.summary,
                    detailsPending = detailsPending,
                    placeholder = placeholder,
                )
            }
        }
    }
}

@Composable
private fun PersonalPropertyLoadFailedBanner(onRetry: () -> Unit) {
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
private fun PersonalPropertyTotalSection(
    summary: PersonalPropertySummary,
    detailsPending: Boolean,
    placeholder: String,
) {
    BaseContainer(useSystemBarsPadding = false) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_personal_property,
                itemName = stringResource(R.string.personal_property_total),
                itemHint = stringResource(R.string.personal_property_total_hint),
                trailingValue = formatPropertyIsk(
                    value = summary.totalIsk,
                    detailsReady = !detailsPending,
                    placeholder = placeholder,
                ),
                showChevron = false,
                onClick = null,
            ),
            showDivider = false,
        )
    }
}

@Composable
private fun PersonalPropertyBreakdownSection(
    summary: PersonalPropertySummary,
    detailsPending: Boolean,
    placeholder: String,
) {
    val detailsReady = !detailsPending
    BaseContainer(useSystemBarsPadding = false) {
        PropertyBreakdownRow(
            iconRes = R.drawable.ic_personal_property_wallet,
            name = stringResource(R.string.personal_property_wallet),
            hint = stringResource(R.string.personal_property_wallet_hint),
            trailingValue = formatPropertyIsk(summary.walletIsk, detailsReady, placeholder),
            clickable = false,
            showDivider = true,
        )
        PropertyBreakdownRow(
            iconRes = R.drawable.ic_personal_property_assets,
            name = stringResource(R.string.personal_property_assets),
            hint = bucketCountHint(
                bucket = summary.assets,
                detailsReady = detailsReady,
                placeholder = placeholder,
                templateRes = R.string.personal_property_assets_hint,
            ),
            trailingValue = formatPropertyIsk(summary.assets.isk, detailsReady, placeholder),
            clickable = true,
            showDivider = true,
        )
        PropertyBreakdownRow(
            iconRes = R.drawable.ic_personal_property_implants,
            name = stringResource(R.string.personal_property_implants),
            hint = bucketCountHint(
                bucket = summary.implants,
                detailsReady = detailsReady,
                placeholder = placeholder,
                templateRes = R.string.personal_property_implants_hint,
            ),
            trailingValue = formatPropertyIsk(summary.implants.isk, detailsReady, placeholder),
            clickable = true,
            showDivider = true,
        )
        PropertyBreakdownRow(
            iconRes = R.drawable.ic_personal_property_market,
            name = stringResource(R.string.personal_property_market_orders),
            hint = bucketCountHint(
                bucket = summary.marketOrders,
                detailsReady = detailsReady,
                placeholder = placeholder,
                templateRes = R.string.personal_property_market_orders_hint,
            ),
            trailingValue = formatPropertyIsk(summary.marketOrders.isk, detailsReady, placeholder),
            clickable = true,
            showDivider = true,
        )
        PropertyBreakdownRow(
            iconRes = R.drawable.ic_personal_property_contract,
            name = stringResource(R.string.personal_property_contracts),
            hint = bucketCountHint(
                bucket = summary.contracts,
                detailsReady = detailsReady,
                placeholder = placeholder,
                templateRes = R.string.personal_property_contracts_hint,
            ),
            trailingValue = formatPropertyIsk(summary.contracts.isk, detailsReady, placeholder),
            clickable = true,
            showDivider = false,
        )
    }
}

@Composable
private fun PersonalPropertyDistributionSection(
    summary: PersonalPropertySummary,
    detailsPending: Boolean,
    placeholder: String,
) {
    val detailsReady = !detailsPending
    BaseContainer(
        title = stringResource(R.string.personal_property_distribution),
        useSystemBarsPadding = false,
    ) {
        PersonalPropertyDistributionChart(
            slices = listOf(
                PersonalPropertySliceUi(
                    label = stringResource(R.string.personal_property_wallet),
                    isk = summary.walletIsk,
                    color = colorResource(R.color.personal_property_slice_wallet),
                ),
                PersonalPropertySliceUi(
                    label = stringResource(R.string.personal_property_assets),
                    isk = summary.assets.isk,
                    color = colorResource(R.color.personal_property_slice_assets),
                ),
                PersonalPropertySliceUi(
                    label = stringResource(R.string.personal_property_implants),
                    isk = summary.implants.isk,
                    color = colorResource(R.color.personal_property_slice_implants),
                ),
                PersonalPropertySliceUi(
                    label = stringResource(R.string.personal_property_market_orders),
                    isk = summary.marketOrders.isk,
                    color = colorResource(R.color.personal_property_slice_market),
                ),
                PersonalPropertySliceUi(
                    label = stringResource(R.string.personal_property_contracts),
                    isk = summary.contracts.isk,
                    color = colorResource(R.color.personal_property_slice_contracts),
                ),
            ),
            detailsReady = detailsReady,
            placeholder = placeholder,
        )
    }
}

@Composable
private fun PropertyBreakdownRow(
    iconRes: Int,
    name: String,
    hint: String,
    trailingValue: String,
    clickable: Boolean,
    showDivider: Boolean,
) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = iconRes,
            itemName = name,
            itemHint = hint,
            trailingValue = trailingValue,
            showChevron = clickable,
            onClick = if (clickable) {
                {}
            } else {
                null
            },
        ),
        showDivider = showDivider,
    )
}

@Composable
private fun bucketCountHint(
    bucket: PersonalPropertyBucket,
    detailsReady: Boolean,
    placeholder: String,
    templateRes: Int,
): String {
    val count = bucket.count
    if (!detailsReady || count == null) return placeholder
    return stringResource(templateRes, count)
}
