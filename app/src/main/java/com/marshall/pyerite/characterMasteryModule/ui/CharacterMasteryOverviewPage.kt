package com.marshall.pyerite.characterMasteryModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMasteryModule.model.MasteryFilter
import com.marshall.pyerite.characterMasteryModule.model.MasteryGroup
import com.marshall.pyerite.characterMasteryModule.navHost.CharacterMasteryRoute
import com.marshall.pyerite.characterMasteryModule.viewModel.CharacterMasteryOverviewViewModel
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarMenuItem
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.util.NumberDisplayFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CharacterMasteryOverviewPage(
    navController: NavController,
    viewModel: CharacterMasteryOverviewViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(localeController.contentLanguage) {
        viewModel.reloadForLanguage()
    }
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_mastery)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val filterDescription = stringResource(R.string.character_mastery_filter)
    val filterLabels = MasteryFilter.entries.associateWith { masteryFilterLabel(it) }
    val refreshAction = pyeritePullRefreshTopBarAction(
        isRefreshing = uiState.isLoading,
        refreshFailed = uiState.loadFailed,
        onRefresh = viewModel::refresh,
    )
    val filterAction = PyeriteTopBarActionItem(
        onClick = {},
        icon = if (uiState.filter == MasteryFilter.ALL) {
            Icons.Outlined.FilterList
        } else {
            Icons.Filled.FilterList
        },
        contentDescription = filterDescription,
        menuItems = MasteryFilter.entries.map { filter ->
            PyeriteTopBarMenuItem(
                label = filterLabels.getValue(filter),
                trailingIcon = if (filter == uiState.filter) Icons.Filled.Check else null,
                showDividerBelow = filter.showMenuDividerBelow,
                onClick = { viewModel.setFilter(filter) },
            )
        },
    )
    val endActions = listOfNotNull(refreshAction, filterAction)
    val visibleGroups = remember(
        uiState.snapshot,
        uiState.filter,
        localeController.contentLanguage,
    ) {
        uiState.snapshot?.groups
            .orEmpty()
            .filter { it.matchingCount(uiState.filter) > 0 }
            .sortedBy { it.displayName(localeController) }
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
                PageTitle(text = pageTitle)
                if (uiState.loadFailed) {
                    Spacer(modifier = Modifier.height(sectionGap))
                    MasteryLoadFailedBanner(onRetry = viewModel::refresh)
                }
                if (uiState.detailsReady) {
                    Spacer(modifier = Modifier.height(sectionGap))
                    BaseContainer(
                        title = null,
                        useSystemBarsPadding = false,
                    ) {
                        if (visibleGroups.isEmpty()) {
                            BaseLazyColumnItem(
                                model = BaseLazyColumnItemModel(
                                    showLeadingIcon = false,
                                    itemName = stringResource(R.string.character_mastery_groups_empty),
                                    showChevron = false,
                                    onClick = null,
                                ),
                                showDivider = false,
                            )
                        } else {
                            visibleGroups.forEachIndexed { index, group ->
                                MasteryOverviewGroupRow(
                                    group = group,
                                    filter = uiState.filter,
                                    localeController = localeController,
                                    showDivider = index != visibleGroups.lastIndex,
                                    onClick = {
                                        navController.navigate(
                                            CharacterMasteryRoute.Group.create(
                                                characterId = viewModel.characterId,
                                                groupId = group.groupId,
                                                filter = uiState.filter,
                                            ),
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

@Composable
private fun MasteryOverviewGroupRow(
    group: MasteryGroup,
    filter: MasteryFilter,
    localeController: LocaleController,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_database,
            iconFileName = group.iconFilename,
            itemName = group.displayName(localeController),
            trailingValue = NumberDisplayFormatter.format(
                group.matchingCount(filter).toLong(),
                NumberDisplayFormatter.Style.FULL,
            ),
            onClick = onClick,
        ),
        showDivider = showDivider,
    )
}

@Composable
internal fun MasteryLoadFailedBanner(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.character_sheet_load_failed),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.character_sheet_retry))
        }
    }
}
