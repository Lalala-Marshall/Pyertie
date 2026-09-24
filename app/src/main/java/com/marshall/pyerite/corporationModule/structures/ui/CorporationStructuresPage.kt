package com.marshall.pyerite.corporationModule.structures.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureSystemSection
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructuresConfig
import com.marshall.pyerite.corporationModule.structures.model.lowFuelStructures
import com.marshall.pyerite.corporationModule.structures.model.systemSections
import com.marshall.pyerite.corporationModule.structures.viewModel.CorporationStructuresViewModel
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@Composable
internal fun CorporationStructuresPage(
    navController: NavController,
    viewModel: CorporationStructuresViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val language = localeController.contentLanguage
    val pageTitle = stringResource(R.string.corporation_structures_page_title)
    val listState = rememberLazyListState()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(listState)
    val onBack = navController.rememberNavigateUpAction()
    val nowMs = System.currentTimeMillis()
    val sections = uiState.structures.systemSections(language)
    val lowFuel = uiState.structures.lowFuelStructures(uiState.fuelMonitor, nowMs)
    val rosterReady = !uiState.isLoading && !uiState.permissionDenied && !uiState.loadFailed
    val showEmpty = rosterReady && uiState.structures.isEmpty()
    val showStructures = !uiState.permissionDenied && uiState.structures.isNotEmpty()
    var showSettings by remember { mutableStateOf(false) }
    var sheetPage by remember { mutableStateOf(CorporationStructuresSheetPage.SETTINGS) }
    val refreshAction = pyeritePullRefreshTopBarAction(
        isRefreshing = uiState.isLoading,
        refreshFailed = uiState.loadFailed,
        onRefresh = viewModel::refresh,
    )
    val settingsAction = PyeriteTopBarActionItem(
        onClick = {
            sheetPage = CorporationStructuresSheetPage.SETTINGS
            showSettings = true
        },
        icon = Icons.Filled.Settings,
        contentDescription = stringResource(R.string.corporation_structures_settings),
    )

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = listOfNotNull(refreshAction, settingsAction),
    ) { topBarPadding ->
        PyeritePullToRefreshBox(
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "page_title") {
                    PageTitle(text = pageTitle)
                }
                if (uiState.permissionDenied || uiState.loadFailed) {
                    item(key = "status") {
                        CorporationStructuresStatusBanner(
                            permissionDenied = uiState.permissionDenied,
                            loadFailed = uiState.loadFailed,
                            onRetry = viewModel::refresh,
                        )
                    }
                }
                if (showEmpty) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.corporation_structures_empty),
                            color = colorResource(R.color.text_primary),
                            fontSize = dimensionResource(R.dimen.type_detail_body_text_size).value.sp,
                            modifier = Modifier.padding(
                                start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
                                end = dimensionResource(R.dimen.detail_card_horizontal_padding),
                                top = dimensionResource(R.dimen.type_detail_section_gap),
                            ),
                        )
                    }
                }
                if (showStructures && lowFuel.isNotEmpty()) {
                    item(key = "low_fuel_header") {
                        Text(
                            text = stringResource(
                                R.string.corporation_structures_low_fuel,
                                uiState.fuelMonitor.days,
                            ),
                            fontSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp,
                            fontWeight = FontWeight.Black,
                            color = colorResource(R.color.corporation_structure_state_hull),
                            modifier = Modifier.sectionTitlePadding(),
                        )
                    }
                    itemsIndexed(
                        items = lowFuel,
                        key = { _, structure -> "low-${structure.structureId}" },
                    ) { index, structure ->
                        StructureCard(isFirst = index == 0, isLast = index == lowFuel.lastIndex) {
                            CorporationStructureRow(
                                structure = structure,
                                language = language,
                                nowMs = nowMs,
                                emphasizeLowFuel = true,
                                showDivider = index < lowFuel.lastIndex,
                            )
                        }
                    }
                }
                if (showStructures) {
                    sections.forEach { section ->
                        item(key = "system-header-${section.systemId}") {
                            CorporationStructuresSystemTitle(
                            section = section,
                            language = language,
                        )
                        }
                        itemsIndexed(
                            items = section.structures,
                            key = { _, structure -> "system-${section.systemId}-${structure.structureId}" },
                        ) { index, structure ->
                            StructureCard(
                                isFirst = index == 0,
                                isLast = index == section.structures.lastIndex,
                            ) {
                                CorporationStructureRow(
                                    structure = structure,
                                    language = language,
                                    nowMs = nowMs,
                                    emphasizeLowFuel = false,
                                    showDivider = index < section.structures.lastIndex,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        CorporationStructuresSettingsSheet(
            page = sheetPage,
            fuelMonitor = uiState.fuelMonitor,
            onOpenFuelMonitor = { sheetPage = CorporationStructuresSheetPage.FUEL_MONITOR },
            onBack = { sheetPage = CorporationStructuresSheetPage.SETTINGS },
            onSelectFuelMonitor = { monitor ->
                viewModel.setFuelMonitor(monitor)
                sheetPage = CorporationStructuresSheetPage.SETTINGS
            },
            onDismiss = {
                sheetPage = CorporationStructuresSheetPage.SETTINGS
                showSettings = false
            },
        )
    }
}

@Composable
private fun CorporationStructuresSystemTitle(
    section: CorporationStructureSystemSection,
    language: ContentLanguage,
) {
    val systemName = section.systemDisplayName(language).ifBlank {
        stringResource(R.string.corporation_structures_unknown_system)
    }
    val regionName = section.regionDisplayName(language)
    val placeName = listOf(regionName, systemName)
        .filter { it.isNotBlank() }
        .joinToString(CorporationStructuresConfig.SECURITY_STATUS_NAME_GAP)
    val textColor = colorResource(R.color.text_primary)
    val security = section.systemSecurityStatus
    val securityColor = security?.let { systemSecurityColor(it) }
    val title = buildAnnotatedString {
        if (security != null && securityColor != null) {
            withStyle(SpanStyle(color = securityColor, fontWeight = FontWeight.Black)) {
                append(
                    String.format(
                        Locale.US,
                        CorporationStructuresConfig.SYSTEM_SECURITY_FORMAT,
                        security,
                    ),
                )
            }
            withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.Black)) {
                append(CorporationStructuresConfig.SECURITY_STATUS_NAME_GAP)
                append(placeName)
            }
        } else {
            withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.Black)) {
                append(placeName)
            }
        }
    }
    Text(
        text = title,
        fontSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.sectionTitlePadding(),
    )
}

@Composable
private fun Modifier.sectionTitlePadding(): Modifier =
    padding(
        start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
        end = dimensionResource(R.dimen.detail_card_horizontal_padding),
        top = dimensionResource(R.dimen.type_detail_section_gap),
        bottom = dimensionResource(R.dimen.list_section_header_bottom_padding),
    )

@Composable
private fun StructureCard(
    isFirst: Boolean,
    isLast: Boolean,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.structureCard(isFirst = isFirst, isLast = isLast)) {
        content()
    }
}

@Composable
private fun Modifier.structureCard(isFirst: Boolean, isLast: Boolean): Modifier {
    val radius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = when {
        isFirst && isLast -> RoundedCornerShape(radius)
        isFirst -> RoundedCornerShape(topStart = radius, topEnd = radius)
        isLast -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
    return this
        .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
        .clip(shape)
        .background(colorResource(R.color.second_background))
}

@Composable
private fun systemSecurityColor(security: Double): Color = when {
    security <= CorporationStructuresConfig.SECURITY_NEGATIVE_MAX ->
        colorResource(R.color.character_security_negative)
    security < CorporationStructuresConfig.SECURITY_LOW_MAX ->
        colorResource(R.color.character_security_low)
    else -> colorResource(R.color.character_security_high)
}

@Composable
private fun CorporationStructuresStatusBanner(
    permissionDenied: Boolean,
    loadFailed: Boolean,
    onRetry: () -> Unit,
) {
    val messageRes = when {
        permissionDenied -> R.string.corporation_structures_permission_denied
        loadFailed -> R.string.corporation_structures_load_failed
        else -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(messageRes),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f),
        )
        if (loadFailed && !permissionDenied) {
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.character_sheet_retry))
            }
        }
    }
}
