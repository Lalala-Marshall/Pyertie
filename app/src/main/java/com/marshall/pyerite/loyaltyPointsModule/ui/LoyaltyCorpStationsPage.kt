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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyPointsConfig
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyStationItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyStationRegionGroup
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyCorpStationsViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@Composable
internal fun LoyaltyCorpStationsPage(
    navController: NavController,
    viewModel: LoyaltyCorpStationsViewModel = koinViewModel(),
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
            when {
                !uiState.detailsReady -> {
                    BaseContainer(useSystemBarsPadding = false) {
                        LoyaltyPointsPlaceholderRow(placeholder)
                    }
                }
                uiState.regions.isEmpty() -> {
                    BaseContainer(useSystemBarsPadding = false) {
                        LoyaltyPointsPlaceholderRow(
                            stringResource(R.string.loyalty_points_stations_empty),
                        )
                    }
                }
                else -> uiState.regions.forEachIndexed { index, region ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(sectionGap))
                    }
                    LoyaltyStationRegionSection(
                        region = region,
                        localeController = localeController,
                        placeholder = placeholder,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoyaltyStationRegionSection(
    region: LoyaltyStationRegionGroup,
    localeController: LocaleController,
    placeholder: String,
) {
    val regionTitle = stringResource(
        R.string.loyalty_points_station_region_title,
        region.displayName(localeController).ifBlank { placeholder },
        region.stations.size,
    )
    BaseContainer(
        title = regionTitle,
        useSystemBarsPadding = false,
    ) {
        region.stations.forEachIndexed { index, station ->
            LoyaltyStationRow(
                station = station,
                placeholder = placeholder,
                showDivider = index != region.stations.lastIndex,
            )
        }
    }
}

@Composable
private fun LoyaltyStationRow(
    station: LoyaltyStationItem,
    placeholder: String,
    showDivider: Boolean,
) {
    val placeName = station.stationName?.takeIf { it.isNotBlank() } ?: placeholder
    val security = station.security
    val titleAnnotated = if (security != null) {
        val securityColor = loyaltyPointsSystemSecurityColor(security)
        val primaryColor = colorResource(R.color.text_primary)
        buildAnnotatedString {
            withStyle(SpanStyle(color = securityColor)) {
                append(
                    String.format(
                        Locale.US,
                        LoyaltyPointsConfig.SYSTEM_SECURITY_FORMAT,
                        security,
                    ),
                )
            }
            append(LoyaltyPointsConfig.LOCATION_SEGMENT_GAP)
            withStyle(SpanStyle(color = primaryColor)) {
                append(placeName)
            }
        }
    } else {
        null
    }
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_loyalty_station,
            iconFileName = station.iconFilename,
            iconOnLightPlate = true,
            itemName = placeName,
            itemNameAnnotated = titleAnnotated,
            showChevron = false,
            onClick = null,
        ),
        showDivider = showDivider,
    )
}
