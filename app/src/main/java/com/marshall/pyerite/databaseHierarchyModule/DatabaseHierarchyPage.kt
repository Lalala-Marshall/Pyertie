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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseLevel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.MetaGroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private val SectionGap = 16.dp
private val BottomPadding = 24.dp
private val CardCornerRadius = 16.dp

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
    viewModel: DatabaseViewModel = koinViewModel(),
    iconManager: IconManager = koinInject(),
) {
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

    val entries = remember(
        level,
        categories,
        groups,
        types,
        metaGroups,
        publishedTitle,
        unpublishedTitle,
        iconManager,
        navController,
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
        )
    }

    LazyColumn(
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
private fun HierarchyListEntryContent(
    pageTitle: String,
    entry: HierarchyListEntry,
) {
    when (entry) {
        HierarchyListEntry.Title -> {
            Text(
                text = pageTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp),
            )
        }

        is HierarchyListEntry.SectionHeader -> {
            Text(
                text = entry.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(
                    start = 24.dp,
                    bottom = 4.dp,
                    top = if (entry.addTopGap) SectionGap else 0.dp,
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
            Spacer(Modifier.height(BottomPadding))
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
    val shape = sectionItemShape(indexInSection, sectionItemCount)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(colorResource(R.color.second_background), shape),
    ) {
        BaseLazyColumnItem(model = model, showDivider = showDivider)
    }
}

private fun sectionItemShape(indexInSection: Int, sectionItemCount: Int): Shape {
    val corner = CardCornerRadius
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
): List<HierarchyListEntry> = buildList {
    add(HierarchyListEntry.Title)

    when (level) {
        DatabaseLevel.CATEGORY -> {
            appendPublishedUnpublishedSections(
                builder = this,
                publishedTitle = publishedTitle,
                unpublishedTitle = unpublishedTitle,
                published = categories.filter { it.published == true }.map {
                    HierarchyRowKey.Category(it.id) to createCategoryModel(it, iconManager, navController)
                },
                unpublished = categories.filter { it.published != true }.map {
                    HierarchyRowKey.Category(it.id) to createCategoryModel(it, iconManager, navController)
                },
            )
        }

        DatabaseLevel.GROUP -> {
            appendPublishedUnpublishedSections(
                builder = this,
                publishedTitle = publishedTitle,
                unpublishedTitle = unpublishedTitle,
                published = groups.filter { it.published == true }.map {
                    HierarchyRowKey.Group(it.id) to createGroupModel(it, iconManager, navController)
                },
                unpublished = groups.filter { it.published != true }.map {
                    HierarchyRowKey.Group(it.id) to createGroupModel(it, iconManager, navController)
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
                            HierarchyRowKey.Type(it.id) to createTypeModel(it, iconManager, navController)
                        },
                    )
                }
            }

            val unpublished = types.filter { it.published != true }.map {
                HierarchyRowKey.Type(it.id) to createTypeModel(it, iconManager, navController)
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
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(category.iconFilename),
    itemName = category.zhName ?: category.name.orEmpty(),
    onClick = {
        navController.navigate(
            DatabaseRoute.Group.create(category.id, category.zhName ?: category.name.orEmpty()),
        )
    },
)

private fun createGroupModel(
    group: GroupEntity,
    iconManager: IconManager,
    navController: NavController,
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(group.iconFilename),
    itemName = group.zhName ?: group.name.orEmpty(),
    onClick = {
        navController.navigate(
            DatabaseRoute.Type.create(group.id, group.zhName ?: group.name.orEmpty()),
        )
    },
)

private fun createTypeModel(
    type: TypeEntity,
    iconManager: IconManager,
    navController: NavController,
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(type.iconFilename),
    itemName = type.zhName ?: type.name.orEmpty(),
    onClick = {
        navController.navigate(DatabaseRoute.TypeDetail.create(type.id))
    },
)
