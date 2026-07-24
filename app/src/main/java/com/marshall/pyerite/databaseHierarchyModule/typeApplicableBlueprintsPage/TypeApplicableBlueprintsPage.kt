package com.marshall.pyerite.databaseHierarchyModule.typeApplicableBlueprintsPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.iconModule.manager.IconManager
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyModule.navHost.rememberDatabaseRootBackStackEntry
import com.marshall.pyerite.sdeModule.room.industry.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.search.DatabaseListSearchHost
import com.marshall.pyerite.databaseHierarchyModule.search.SearchNoResultsItem
import com.marshall.pyerite.databaseHierarchyModule.search.matchesSearchQuery
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private fun applicableBlueprintsPageKey(typeId: Int): String = "applicableBlueprints:$typeId"

private sealed class ApplicableBlueprintListEntry(val key: String) {
    data class Item(
        val blueprintTypeId: Int,
        val model: BaseLazyColumnItemModel,
        val showDivider: Boolean,
        val indexInSection: Int,
        val sectionItemCount: Int,
    ) : ApplicableBlueprintListEntry("item:$blueprintTypeId")

    data object BottomPadding : ApplicableBlueprintListEntry("page:bottom_padding")
}

@Composable
fun TypeApplicableBlueprintsPage(
    typeId: Int,
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    iconManager: IconManager = koinInject(),
) {
    val databaseBackStackEntry = rememberDatabaseRootBackStackEntry(navController, backStackEntry)
    val viewModel: DatabaseViewModel = koinViewModel(viewModelStoreOwner = databaseBackStackEntry)
    val pageKey = applicableBlueprintsPageKey(typeId)
    val listState = rememberLazyListState()

    val blueprints by remember(typeId) { viewModel.applicableBlueprints(typeId) }
        .collectAsState(initial = emptyList())
    val searchState by viewModel.searchState(pageKey).collectAsState()

    val entries = remember(blueprints, searchState.query, iconManager, navController) {
        buildApplicableBlueprintEntries(
            blueprints = blueprints,
            searchQuery = searchState.query,
            iconManager = iconManager,
            navController = navController,
        )
    }

    val hasListItems = entries.any { it is ApplicableBlueprintListEntry.Item }
    val pageTitle = stringResource(R.string.type_detail_applicable_to)
    val onBack = navController.rememberNavigateUpAction()

    DatabaseListSearchHost(
        pageKey = pageKey,
        viewModel = viewModel,
        listState = listState,
        navTitle = pageTitle,
        modifier = Modifier.fillMaxSize(),
        onBack = onBack,
        title = {
            PageTitle(text = pageTitle)
        },
    ) { query ->
        if (query.isNotBlank() && !hasListItems) {
            item(key = "search_no_results") {
                SearchNoResultsItem()
            }
        }
        items(
            items = entries,
            key = { entry -> entry.key },
        ) { entry ->
            ApplicableBlueprintListEntryContent(entry = entry)
        }
    }
}

@Composable
private fun ApplicableBlueprintListEntryContent(entry: ApplicableBlueprintListEntry) {
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    when (entry) {
        is ApplicableBlueprintListEntry.Item -> {
            ApplicableBlueprintItemRow(
                model = entry.model,
                showDivider = entry.showDivider,
                indexInSection = entry.indexInSection,
                sectionItemCount = entry.sectionItemCount,
            )
        }

        ApplicableBlueprintListEntry.BottomPadding -> {
            Spacer(Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun ApplicableBlueprintItemRow(
    model: BaseLazyColumnItemModel,
    showDivider: Boolean,
    indexInSection: Int,
    sectionItemCount: Int,
) {
    val cardCornerRadius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = sectionItemShape(indexInSection, sectionItemCount, cardCornerRadius)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
            .clip(shape)
            .background(colorResource(R.color.second_background), shape),
    ) {
        BaseLazyColumnItem(model = model, showDivider = showDivider)
    }
}

private fun sectionItemShape(indexInSection: Int, sectionItemCount: Int, corner: Dp): Shape {
    return when {
        sectionItemCount == 1 -> RoundedCornerShape(corner)
        indexInSection == 0 -> RoundedCornerShape(topStart = corner, topEnd = corner)
        indexInSection == sectionItemCount - 1 -> RoundedCornerShape(bottomStart = corner, bottomEnd = corner)
        else -> RectangleShape
    }
}

private fun buildApplicableBlueprintEntries(
    blueprints: List<TypeBlueprintDetail>,
    searchQuery: String,
    iconManager: IconManager,
    navController: NavController,
): List<ApplicableBlueprintListEntry> = buildList {
    val filtered = blueprints.filter { it.name.matchesSearchQuery(searchQuery) }
    if (filtered.isEmpty()) {
        add(ApplicableBlueprintListEntry.BottomPadding)
        return@buildList
    }

    filtered.forEachIndexed { index, blueprint ->
        add(
            ApplicableBlueprintListEntry.Item(
                blueprintTypeId = blueprint.typeId,
                model = BaseLazyColumnItemModel(
                    iconFile = blueprint.iconFilename?.let { iconManager.getIconFile(it) },
                    itemName = blueprint.name.orEmpty(),
                    onClick = {
                        navController.navigate(DatabaseRoute.TypeDetail.create(blueprint.typeId))
                    },
                ),
                showDivider = index < filtered.lastIndex,
                indexInSection = index,
                sectionItemCount = filtered.size,
            ),
        )
    }

    add(ApplicableBlueprintListEntry.BottomPadding)
}
