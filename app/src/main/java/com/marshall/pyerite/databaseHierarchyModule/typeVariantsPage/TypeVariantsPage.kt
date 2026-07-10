package com.marshall.pyerite.databaseHierarchyModule.typeVariantsPage

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
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.databaseHierarchyModule.navHost.rememberDatabaseRootBackStackEntry
import com.marshall.pyerite.databaseHierarchyModule.room.entity.MetaGroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.search.DatabaseListSearchHost
import com.marshall.pyerite.databaseHierarchyModule.search.SearchNoResultsItem
import com.marshall.pyerite.databaseHierarchyModule.search.matchingSearch
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private fun variantPageKey(typeId: Int): String = "typeVariants:$typeId"

private sealed class VariantListEntry(val key: String) {
    data class SectionHeader(
        val metaGroupId: Int?,
        val title: String,
        val addTopGap: Boolean,
    ) : VariantListEntry("header:${metaGroupId ?: "none"}")

    data class Item(
        val metaGroupId: Int?,
        val typeId: Int,
        val model: BaseLazyColumnItemModel,
        val showDivider: Boolean,
        val indexInSection: Int,
        val sectionItemCount: Int,
    ) : VariantListEntry("item:${metaGroupId ?: "none"}:$typeId")

    data object BottomPadding : VariantListEntry("page:bottom_padding")
}

@Composable
fun TypeVariantsPage(
    typeId: Int,
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    iconManager: IconManager = koinInject(),
    localeController: LocaleController = koinInject(),
) {
    val databaseBackStackEntry = rememberDatabaseRootBackStackEntry(navController, backStackEntry)
    val viewModel: DatabaseViewModel = koinViewModel(viewModelStoreOwner = databaseBackStackEntry)
    val pageKey = variantPageKey(typeId)
    val listState = rememberLazyListState()

    val variants by remember(typeId) { viewModel.variants(typeId) }
        .collectAsState(initial = emptyList())
    val metaGroups by viewModel.metaGroups.collectAsState(initial = emptyList())
    val searchState by viewModel.searchState(pageKey).collectAsState()

    val unknownGroupLabel = stringResource(R.string.unknown_group)
    val contentLanguage = localeController.contentLanguage
    val entries = remember(
        variants,
        metaGroups,
        unknownGroupLabel,
        contentLanguage,
        searchState.query,
        iconManager,
        navController,
        localeController,
    ) {
        buildVariantEntries(
            variants = variants,
            metaGroups = metaGroups,
            unknownGroupLabel = unknownGroupLabel,
            searchQuery = searchState.query,
            iconManager = iconManager,
            navController = navController,
            localeController = localeController,
        )
    }

    val hasListItems = entries.any { it is VariantListEntry.Item }
    val pageTitle = stringResource(R.string.type_detail_variants_section)
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
            VariantListEntryContent(entry = entry)
        }
    }
}

@Composable
private fun VariantListEntryContent(entry: VariantListEntry) {
    val sectionHeaderTextSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val sectionHeaderBottomPadding = dimensionResource(R.dimen.list_section_header_bottom_padding)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    when (entry) {
        is VariantListEntry.SectionHeader -> {
            Text(
                text = entry.title,
                fontSize = sectionHeaderTextSize,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(
                    start = titleStartPadding,
                    bottom = sectionHeaderBottomPadding,
                    top = if (entry.addTopGap) sectionGap else 0.dp,
                ),
            )
        }

        is VariantListEntry.Item -> {
            VariantSectionItemRow(
                model = entry.model,
                showDivider = entry.showDivider,
                indexInSection = entry.indexInSection,
                sectionItemCount = entry.sectionItemCount,
            )
        }

        VariantListEntry.BottomPadding -> {
            Spacer(Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun VariantSectionItemRow(
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

private fun buildVariantEntries(
    variants: List<TypeEntity>,
    metaGroups: List<MetaGroupEntity>,
    unknownGroupLabel: String,
    searchQuery: String,
    iconManager: IconManager,
    navController: NavController,
    localeController: LocaleController,
): List<VariantListEntry> = buildList {
    val filteredVariants = variants.matchingSearch(searchQuery, localeController)
    if (filteredVariants.isEmpty()) {
        add(VariantListEntry.BottomPadding)
        return@buildList
    }

    val metaGroupMap = metaGroups.associateBy { it.id }
    val grouped = filteredVariants.groupBy { it.metaGroupID }
    var addTopGap = false

    grouped.keys.sortedBy { it ?: Int.MAX_VALUE }.forEach { metaId ->
        val itemsInMeta = grouped[metaId].orEmpty()
        val sectionTitle = metaGroupMap[metaId]?.name
            ?: metaId?.let { "$unknownGroupLabel ($it)" }
            ?: unknownGroupLabel
        addTopGap = appendVariantSection(
            builder = this,
            metaGroupId = metaId,
            title = sectionTitle,
            addTopGap = addTopGap,
            types = itemsInMeta,
            iconManager = iconManager,
            navController = navController,
            localeController = localeController,
        )
    }

    add(VariantListEntry.BottomPadding)
}

private fun appendVariantSection(
    builder: MutableList<VariantListEntry>,
    metaGroupId: Int?,
    title: String,
    addTopGap: Boolean,
    types: List<TypeEntity>,
    iconManager: IconManager,
    navController: NavController,
    localeController: LocaleController,
): Boolean {
    if (types.isEmpty()) return addTopGap

    builder.add(
        VariantListEntry.SectionHeader(
            metaGroupId = metaGroupId,
            title = title,
            addTopGap = addTopGap,
        ),
    )
    types.forEachIndexed { index, type ->
        builder.add(
            VariantListEntry.Item(
                metaGroupId = metaGroupId,
                typeId = type.id,
                model = BaseLazyColumnItemModel(
                    iconFile = iconManager.getIconFile(type.iconFilename),
                    itemName = type.displayName(localeController),
                    onClick = {
                        navController.navigate(DatabaseRoute.TypeDetail.create(type.id))
                    },
                ),
                showDivider = index < types.lastIndex,
                indexInSection = index,
                sectionItemCount = types.size,
            ),
        )
    }
    return true
}
