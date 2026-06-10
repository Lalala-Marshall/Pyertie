package com.marshall.pyerite.databaseHierarchyModule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseLevel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.MetaGroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.databaseHierarchyModule.viewModel.HierarchyScrollPosition
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private fun hierarchyScrollKey(level: DatabaseLevel, parentId: Int?): String = when (level) {
    DatabaseLevel.CATEGORY -> "hierarchy:category"
    DatabaseLevel.GROUP -> "hierarchy:group:$parentId"
    DatabaseLevel.TYPE -> "hierarchy:type:$parentId"
}

private sealed interface HierarchySectionKey {
    data object Published : HierarchySectionKey

    data object Unpublished : HierarchySectionKey

    data class MetaGroup(val metaGroupId: Int?) : HierarchySectionKey
}

private sealed interface HierarchyRowKey {
    data class Category(val id: Int) : HierarchyRowKey

    data class Group(val id: Int) : HierarchyRowKey

    data class Type(val id: Int) : HierarchyRowKey
}

private sealed class HierarchyListEntry(val key: String) {
    data object Title : HierarchyListEntry("page:title")

    data class SectionHeader(
        val sectionKey: HierarchySectionKey,
        val title: String,
        val addTopGap: Boolean,
    ) : HierarchyListEntry("header:${sectionKey.toLazyListKey()}")

    data class Item(
        val sectionKey: HierarchySectionKey,
        val rowKey: HierarchyRowKey,
        val model: BaseLazyColumnItemModel,
        val showDivider: Boolean,
        val indexInSection: Int,
        val sectionItemCount: Int,
    ) : HierarchyListEntry(
        "item:${sectionKey.toLazyListKey()}:${rowKey.toLazyListKey()}",
    )

    data object BottomPadding : HierarchyListEntry("page:bottom_padding")
}

/** LazyColumn keys on Android must be [android.os.Bundle]-compatible (e.g. [String]). */
private fun HierarchySectionKey.toLazyListKey(): String = when (this) {
    HierarchySectionKey.Published -> "published"
    HierarchySectionKey.Unpublished -> "unpublished"
    is HierarchySectionKey.MetaGroup -> "meta:${metaGroupId ?: "none"}"
}

private fun HierarchyRowKey.toLazyListKey(): String = when (this) {
    is HierarchyRowKey.Category -> "category:$id"
    is HierarchyRowKey.Group -> "group:$id"
    is HierarchyRowKey.Type -> "type:$id"
}

@Composable
fun DatabaseHierarchyPage(
    title: String,
    level: DatabaseLevel,
    parentId: Int?,
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    iconManager: IconManager = koinInject(),
    localeController: LocaleController = koinInject(),
) {
    val databaseBackStackEntry = remember(backStackEntry) {
        navController.getBackStackEntry(DatabaseRoute.Root.route)
    }
    val viewModel: DatabaseViewModel = koinViewModel(viewModelStoreOwner = databaseBackStackEntry)
    val scrollKey = hierarchyScrollKey(level, parentId)
    val listState = rememberHierarchyLazyListState(scrollKey, viewModel)
    val categories by if (level == DatabaseLevel.CATEGORY) {
        viewModel.categories.collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val groups by if (level == DatabaseLevel.GROUP && parentId != null) {
        remember(parentId) { viewModel.groups(parentId) }.collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val types by if (level == DatabaseLevel.TYPE && parentId != null) {
        remember(parentId) { viewModel.types(parentId) }.collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val metaGroups by if (level == DatabaseLevel.TYPE) {
        viewModel.metaGroups.collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val publishedTitle = stringResource(R.string.published)
    val unpublishedTitle = stringResource(R.string.unpublished)

    val contentLanguage = localeController.contentLanguage
    val entries = remember(
        level,
        categories,
        groups,
        types,
        metaGroups,
        publishedTitle,
        unpublishedTitle,
        contentLanguage,
        iconManager,
        navController,
        localeController,
    ) {
        buildHierarchyEntries(
            level = level,
            categories = categories,
            groups = groups,
            types = types,
            metaGroups = metaGroups,
            publishedTitle = publishedTitle,
            unpublishedTitle = unpublishedTitle,
            iconManager = iconManager,
            navController = navController,
            localeController = localeController,
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        items(
            items = entries,
            key = { entry -> entry.key },
        ) { entry ->
            HierarchyListEntryContent(
                pageTitle = title,
                entry = entry,
            )
        }
    }
}

@Composable
private fun rememberHierarchyLazyListState(
    scrollKey: String,
    viewModel: DatabaseViewModel,
): LazyListState {
    val saved: HierarchyScrollPosition = viewModel.hierarchyScrollPosition(scrollKey)
    val listState = remember(scrollKey) {
        LazyListState(
            firstVisibleItemIndex = saved.index,
            firstVisibleItemScrollOffset = saved.offset,
        )
    }

    DisposableEffect(scrollKey, listState) {
        onDispose {
            viewModel.saveHierarchyScrollPosition(
                scrollKey = scrollKey,
                index = listState.firstVisibleItemIndex,
                offset = listState.firstVisibleItemScrollOffset,
            )
        }
    }

    LaunchedEffect(scrollKey) {
        val targetIndex = viewModel.hierarchyScrollPosition(scrollKey).index
        val targetOffset = viewModel.hierarchyScrollPosition(scrollKey).offset
        if (targetIndex <= 0 && targetOffset <= 0) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.totalItemsCount }.collect { count ->
            if (count <= 0) return@collect
            val index = targetIndex.coerceIn(0, count - 1)
            val currentIndex = listState.firstVisibleItemIndex
            val currentOffset = listState.firstVisibleItemScrollOffset
            if (currentIndex < index || (currentIndex == index && currentOffset < targetOffset)) {
                listState.scrollToItem(index, targetOffset)
            }
        }
    }

    return listState
}

@Composable
private fun HierarchyListEntryContent(
    pageTitle: String,
    entry: HierarchyListEntry,
) {
    val pageTitleTextSize = dimensionResource(R.dimen.list_page_title_text_size).value.sp
    val sectionHeaderTextSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val titleVerticalPadding = dimensionResource(R.dimen.type_detail_page_title_vertical_padding)
    val sectionHeaderBottomPadding = dimensionResource(R.dimen.list_section_header_bottom_padding)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    when (entry) {
        HierarchyListEntry.Title -> {
            Text(
                text = pageTitle,
                fontSize = pageTitleTextSize,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(
                    start = titleStartPadding,
                    top = titleVerticalPadding,
                    bottom = titleVerticalPadding,
                ),
            )
        }

        is HierarchyListEntry.SectionHeader -> {
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

        is HierarchyListEntry.Item -> {
            HierarchySectionItemRow(
                model = entry.model,
                showDivider = entry.showDivider,
                indexInSection = entry.indexInSection,
                sectionItemCount = entry.sectionItemCount,
            )
        }

        HierarchyListEntry.BottomPadding -> {
            Spacer(Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun HierarchySectionItemRow(
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

private fun buildHierarchyEntries(
    level: DatabaseLevel,
    categories: List<CategoryEntity>,
    groups: List<GroupEntity>,
    types: List<TypeEntity>,
    metaGroups: List<MetaGroupEntity>,
    publishedTitle: String,
    unpublishedTitle: String,
    iconManager: IconManager,
    navController: NavController,
    localeController: LocaleController,
): List<HierarchyListEntry> = buildList {
    add(HierarchyListEntry.Title)

    when (level) {
        DatabaseLevel.CATEGORY -> {
            appendPublishedUnpublishedSections(
                builder = this,
                publishedTitle = publishedTitle,
                unpublishedTitle = unpublishedTitle,
                published = categories.filter { it.published == true }.map {
                    HierarchyRowKey.Category(it.id) to createCategoryModel(it, iconManager, navController, localeController)
                },
                unpublished = categories.filter { it.published != true }.map {
                    HierarchyRowKey.Category(it.id) to createCategoryModel(it, iconManager, navController, localeController)
                },
            )
        }

        DatabaseLevel.GROUP -> {
            appendPublishedUnpublishedSections(
                builder = this,
                publishedTitle = publishedTitle,
                unpublishedTitle = unpublishedTitle,
                published = groups.filter { it.published == true }.map {
                    HierarchyRowKey.Group(it.id) to createGroupModel(it, iconManager, navController, localeController)
                },
                unpublished = groups.filter { it.published != true }.map {
                    HierarchyRowKey.Group(it.id) to createGroupModel(it, iconManager, navController, localeController)
                },
            )
        }

        DatabaseLevel.TYPE -> {
            var addTopGap = false
            val publishedTypes = types.filter { it.published == true }
            if (publishedTypes.isNotEmpty()) {
                val metaGroupMap = metaGroups.associateBy { it.id }
                val grouped = publishedTypes.groupBy { it.metaGroupID }
                grouped.keys.sortedBy { it ?: Int.MAX_VALUE }.forEach { metaId ->
                    val itemsInMeta = grouped[metaId].orEmpty()
                    val sectionKey = HierarchySectionKey.MetaGroup(metaId)
                    val metaName = if (metaId == null) {
                        publishedTitle
                    } else {
                        metaGroupMap[metaId]?.name ?: "Meta Group $metaId"
                    }
                    addTopGap = appendSection(
                        builder = this,
                        sectionKey = sectionKey,
                        title = metaName,
                        addTopGap = addTopGap,
                        rows = itemsInMeta.map {
                            HierarchyRowKey.Type(it.id) to createTypeModel(it, iconManager, navController, localeController)
                        },
                    )
                }
            }

            val unpublished = types.filter { it.published != true }.map {
                HierarchyRowKey.Type(it.id) to createTypeModel(it, iconManager, navController, localeController)
            }
            appendSection(
                builder = this,
                sectionKey = HierarchySectionKey.Unpublished,
                title = unpublishedTitle,
                addTopGap = addTopGap,
                rows = unpublished,
            )
        }
    }

    add(HierarchyListEntry.BottomPadding)
}

private fun appendPublishedUnpublishedSections(
    builder: MutableList<HierarchyListEntry>,
    publishedTitle: String,
    unpublishedTitle: String,
    published: List<Pair<HierarchyRowKey, BaseLazyColumnItemModel>>,
    unpublished: List<Pair<HierarchyRowKey, BaseLazyColumnItemModel>>,
) {
    val addTopGap: Boolean = appendSection(
        builder = builder,
        sectionKey = HierarchySectionKey.Published,
        title = publishedTitle,
        addTopGap = false,
        rows = published,
    )
    appendSection(
        builder = builder,
        sectionKey = HierarchySectionKey.Unpublished,
        title = unpublishedTitle,
        addTopGap = addTopGap,
        rows = unpublished,
    )
}

/** @return `true` when the next section should use top gap. */
private fun appendSection(
    builder: MutableList<HierarchyListEntry>,
    sectionKey: HierarchySectionKey,
    title: String,
    addTopGap: Boolean,
    rows: List<Pair<HierarchyRowKey, BaseLazyColumnItemModel>>,
): Boolean {
    if (rows.isEmpty()) return addTopGap

    builder.add(
        HierarchyListEntry.SectionHeader(
            sectionKey = sectionKey,
            title = title,
            addTopGap = addTopGap,
        ),
    )
    rows.forEachIndexed { index, (rowKey, model) ->
        builder.add(
            HierarchyListEntry.Item(
                sectionKey = sectionKey,
                rowKey = rowKey,
                model = model,
                showDivider = index < rows.lastIndex,
                indexInSection = index,
                sectionItemCount = rows.size,
            ),
        )
    }
    return true
}

private fun createCategoryModel(
    category: CategoryEntity,
    iconManager: IconManager,
    navController: NavController,
    localeController: LocaleController,
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(category.iconFilename),
    itemName = category.displayName(localeController),
    onClick = {
        navController.navigate(
            DatabaseRoute.Group.create(category.id, category.displayName(localeController)),
        )
    },
)

private fun createGroupModel(
    group: GroupEntity,
    iconManager: IconManager,
    navController: NavController,
    localeController: LocaleController,
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(group.iconFilename),
    itemName = group.displayName(localeController),
    onClick = {
        navController.navigate(
            DatabaseRoute.Type.create(group.id, group.displayName(localeController)),
        )
    },
)

private fun createTypeModel(
    type: TypeEntity,
    iconManager: IconManager,
    navController: NavController,
    localeController: LocaleController,
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(type.iconFilename),
    itemName = type.displayName(localeController),
    onClick = {
        navController.navigate(DatabaseRoute.TypeDetail.create(type.id))
    },
)
