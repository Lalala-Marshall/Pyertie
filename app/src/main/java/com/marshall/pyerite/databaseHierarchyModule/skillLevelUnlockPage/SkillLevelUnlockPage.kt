package com.marshall.pyerite.databaseHierarchyModule.skillLevelUnlockPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
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
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillUnlockTypeRow
import com.marshall.pyerite.databaseHierarchyModule.search.DatabaseListSearchHost
import com.marshall.pyerite.databaseHierarchyModule.search.SearchNoResultsItem
import com.marshall.pyerite.databaseHierarchyModule.search.matchingSearch
import com.marshall.pyerite.databaseHierarchyModule.util.certificateLevelDrawable
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private fun skillUnlockPageKey(skillTypeId: Int, level: Int): String =
    "skillUnlock:$skillTypeId:$level"

private sealed class UnlockListEntry(val key: String) {
    data class SectionHeader(
        val categoryKey: String,
        val title: String,
        val addTopGap: Boolean,
    ) : UnlockListEntry("header:$categoryKey")

    data class Item(
        val categoryKey: String,
        val typeId: Int,
        val model: BaseLazyColumnItemModel,
        val showDivider: Boolean,
        val indexInSection: Int,
        val sectionItemCount: Int,
    ) : UnlockListEntry("item:$categoryKey:$typeId")

    data object BottomPadding : UnlockListEntry("page:bottom_padding")
}

@Composable
fun SkillLevelUnlockPage(
    skillTypeId: Int,
    level: Int,
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    iconManager: IconManager = koinInject(),
    localeController: LocaleController = koinInject(),
) {
    val databaseBackStackEntry = rememberDatabaseRootBackStackEntry(navController, backStackEntry)
    val viewModel: DatabaseViewModel = koinViewModel(viewModelStoreOwner = databaseBackStackEntry)
    val pageKey = skillUnlockPageKey(skillTypeId, level)
    val listState = rememberLazyListState()

    val unlockTypes by remember(skillTypeId, level) {
        viewModel.typesUnlockedBySkillAtLevel(skillTypeId, level)
    }.collectAsState(initial = emptyList())
    val searchState by viewModel.searchState(pageKey).collectAsState()

    val uncategorizedLabel = stringResource(R.string.category_uncategorized)
    val contentLanguage = localeController.contentLanguage
    val entries = remember(
        unlockTypes,
        uncategorizedLabel,
        contentLanguage,
        searchState.query,
        iconManager,
        navController,
        localeController,
    ) {
        buildUnlockEntries(
            unlockTypes = unlockTypes,
            uncategorizedLabel = uncategorizedLabel,
            searchQuery = searchState.query,
            iconManager = iconManager,
            navController = navController,
            localeController = localeController,
        )
    }

    val hasListItems = entries.any { it is UnlockListEntry.Item }
    val pageTitle = stringResource(R.string.skill_level, level)
    val onBack = navController.rememberNavigateUpAction()

    DatabaseListSearchHost(
        pageKey = pageKey,
        viewModel = viewModel,
        listState = listState,
        navTitle = pageTitle,
        modifier = Modifier.fillMaxSize(),
        onBack = onBack,
        title = {
            SkillUnlockPageTitle(level = level)
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
            UnlockListEntryContent(entry = entry)
        }
    }
}

@Composable
private fun SkillUnlockPageTitle(level: Int) {
    val titleIconSize = dimensionResource(R.dimen.skill_unlock_title_icon_size)
    val titleIconGap = dimensionResource(R.dimen.skill_unlock_title_icon_gap)

    PageTitle(
        text = stringResource(R.string.skill_level, level),
        leadingContent = {
            Icon(
                modifier = Modifier.size(titleIconSize),
                painter = painterResource(certificateLevelDrawable(level)),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(titleIconGap))
        },
    )
}

@Composable
private fun UnlockListEntryContent(entry: UnlockListEntry) {
    val sectionSubheaderTextSize = dimensionResource(R.dimen.list_section_subheader_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val sectionHeaderBottomPadding = dimensionResource(R.dimen.list_section_header_bottom_padding)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    when (entry) {
        is UnlockListEntry.SectionHeader -> {
            Text(
                text = entry.title,
                fontSize = sectionSubheaderTextSize,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.hint_text),
                modifier = Modifier.padding(
                    start = titleStartPadding,
                    bottom = sectionHeaderBottomPadding,
                    top = if (entry.addTopGap) sectionGap else 0.dp,
                ),
            )
        }

        is UnlockListEntry.Item -> {
            UnlockSectionItemRow(
                model = entry.model,
                showDivider = entry.showDivider,
                indexInSection = entry.indexInSection,
                sectionItemCount = entry.sectionItemCount,
            )
        }

        UnlockListEntry.BottomPadding -> {
            Spacer(Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun UnlockSectionItemRow(
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

private fun buildUnlockEntries(
    unlockTypes: List<SkillUnlockTypeRow>,
    uncategorizedLabel: String,
    searchQuery: String,
    iconManager: IconManager,
    navController: NavController,
    localeController: LocaleController,
): List<UnlockListEntry> = buildList {
    val filteredTypes = unlockTypes.matchingSearch(searchQuery, localeController)
    if (filteredTypes.isEmpty()) {
        add(UnlockListEntry.BottomPadding)
        return@buildList
    }

    val grouped = filteredTypes.groupBy { row ->
        row.categoryId?.toString() ?: "none"
    }
    var addTopGap = false

    grouped.keys.sortedWith(compareBy { key ->
        val first = grouped[key]?.firstOrNull()
        first?.categoryId ?: Int.MAX_VALUE
    }).forEach { categoryKey ->
        val itemsInCategory = grouped[categoryKey].orEmpty()
        val sectionTitle = itemsInCategory.firstOrNull()?.categoryName?.takeIf { it.isNotBlank() }
            ?: uncategorizedLabel
        addTopGap = appendUnlockSection(
            builder = this,
            categoryKey = categoryKey,
            title = sectionTitle,
            addTopGap = addTopGap,
            types = itemsInCategory,
            iconManager = iconManager,
            navController = navController,
            localeController = localeController,
        )
    }

    add(UnlockListEntry.BottomPadding)
}

private fun appendUnlockSection(
    builder: MutableList<UnlockListEntry>,
    categoryKey: String,
    title: String,
    addTopGap: Boolean,
    types: List<SkillUnlockTypeRow>,
    iconManager: IconManager,
    navController: NavController,
    localeController: LocaleController,
): Boolean {
    if (types.isEmpty()) return addTopGap

    builder.add(
        UnlockListEntry.SectionHeader(
            categoryKey = categoryKey,
            title = title,
            addTopGap = addTopGap,
        ),
    )
    types.forEachIndexed { index, type ->
        builder.add(
            UnlockListEntry.Item(
                categoryKey = categoryKey,
                typeId = type.typeId,
                model = BaseLazyColumnItemModel(
                    iconFile = iconManager.getIconFile(type.iconFilename),
                    itemName = type.displayName(localeController),
                    onClick = {
                        navController.navigate(DatabaseRoute.TypeDetail.create(type.typeId))
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
