package com.marshall.pyerite.characterMasteryModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMasteryModule.model.MasteryConfig
import com.marshall.pyerite.characterMasteryModule.model.MasteryFilter
import com.marshall.pyerite.characterMasteryModule.model.MasteryMetaGroup
import com.marshall.pyerite.characterMasteryModule.model.MasteryShip
import com.marshall.pyerite.characterMasteryModule.viewModel.CharacterMasteryGroupViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
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
import org.koin.compose.koinInject

@Composable
internal fun CharacterMasteryGroupPage(
    navController: NavController,
    viewModel: CharacterMasteryGroupViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(localeController.contentLanguage) {
        viewModel.reloadForLanguage()
    }
    val scrollState = rememberScrollState()
    val group = remember(uiState.snapshot, viewModel.groupId) {
        uiState.snapshot?.groups?.find { it.groupId == viewModel.groupId }
    }
    val pageTitle = group?.displayName(localeController).orEmpty()
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val publishedTitle = stringResource(R.string.published)
    val unpublishedTitle = stringResource(R.string.unpublished)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refresh,
        ),
    )
    val sections = remember(
        group,
        uiState.filter,
        uiState.snapshot?.metaGroups,
        localeController.contentLanguage,
        publishedTitle,
        unpublishedTitle,
    ) {
        buildMasteryGroupSections(
            ships = group?.ships.orEmpty(),
            filter = uiState.filter,
            metaGroups = uiState.snapshot?.metaGroups.orEmpty(),
            localeController = localeController,
            publishedTitle = publishedTitle,
            unpublishedTitle = unpublishedTitle,
        )
    }

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
                if (pageTitle.isNotEmpty()) {
                    PageTitle(text = pageTitle)
                }
                if (uiState.loadFailed) {
                    Spacer(modifier = Modifier.height(sectionGap))
                    MasteryLoadFailedBanner(onRetry = viewModel::refresh)
                }
                when {
                    !uiState.detailsReady -> Unit
                    sections.isEmpty() -> {
                        Spacer(modifier = Modifier.height(sectionGap))
                        Text(
                            text = if (uiState.filter != MasteryFilter.ALL) {
                                stringResource(
                                    R.string.character_mastery_filter_empty,
                                    masteryFilterLabel(uiState.filter),
                                )
                            } else {
                                stringResource(R.string.character_mastery_groups_empty)
                            },
                            color = colorResource(R.color.hint_text),
                            modifier = Modifier.padding(
                                start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
                                end = dimensionResource(R.dimen.detail_card_horizontal_padding),
                            ),
                        )
                    }
                    else -> sections.forEach { section ->
                        Spacer(modifier = Modifier.height(sectionGap))
                        BaseContainer(
                            title = section.title,
                            useSystemBarsPadding = false,
                        ) {
                            section.ships.forEachIndexed { index, ship ->
                                MasteryShipRow(
                                    ship = ship,
                                    localeController = localeController,
                                    showDivider = index != section.ships.lastIndex,
                                    onClick = {
                                        navController.navigate(
                                            DatabaseRoute.TypeDetail.create(ship.typeId),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class MasteryGroupSection(
    val title: String,
    val ships: List<MasteryShip>,
)

private fun buildMasteryGroupSections(
    ships: List<MasteryShip>,
    filter: MasteryFilter,
    metaGroups: List<MasteryMetaGroup>,
    localeController: LocaleController,
    publishedTitle: String,
    unpublishedTitle: String,
): List<MasteryGroupSection> {
    val matching = ships
        .filter { filter.matches(it.state) }
        .sortedBy { it.displayName(localeController) }
    val published = matching.filter { it.published }
    val unpublished = matching.filter { !it.published }
    val metaNameById = metaGroups.associate { it.id to it.name }
    val sections = mutableListOf<MasteryGroupSection>()
    val grouped = published.groupBy { it.metaGroupId }
    grouped.keys.sortedBy { it ?: Int.MAX_VALUE }.forEach { metaId ->
        val items = grouped[metaId].orEmpty()
        if (items.isEmpty()) return@forEach
        val title = if (metaId == null) {
            publishedTitle
        } else {
            metaNameById[metaId]?.takeIf { it.isNotBlank() } ?: metaId.toString()
        }
        sections += MasteryGroupSection(title = title, ships = items)
    }
    if (unpublished.isNotEmpty()) {
        sections += MasteryGroupSection(title = unpublishedTitle, ships = unpublished)
    }
    return sections
}

@Composable
private fun MasteryShipRow(
    ship: MasteryShip,
    localeController: LocaleController,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    val iconSize = MasteryConfig.ICON_SIZE_DP.dp
    val iconDescription = stringResource(R.string.character_mastery_icon)
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_database,
            iconFileName = ship.iconFilename,
            itemName = ship.displayName(localeController),
            onClick = onClick,
        ),
        showDivider = showDivider,
        backgroundBrush = masteryRowBackgroundBrush(ship.state),
        trailingContent = {
            Icon(
                painter = painterResource(MasteryConfig.iconRes(ship.state)),
                contentDescription = iconDescription,
                tint = Color.Unspecified,
                modifier = Modifier.size(iconSize),
            )
        },
    )
}
