package com.marshall.pyerite.databaseHierarchyModule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.marshall.pyerite.ui.golbalComponents.BaseColumn
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun DatabaseHierarchyPage(
    title: String,
    level: DatabaseLevel,
    parentId: Int?,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
    iconManager: IconManager = koinInject()
) {
    val categories by if (level == DatabaseLevel.CATEGORY) {
        viewModel.categories.collectAsState()
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val groups by if (level == DatabaseLevel.GROUP && parentId != null) {
        remember(parentId) { viewModel.groups(parentId) }.collectAsState()
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val types by if (level == DatabaseLevel.TYPE && parentId != null) {
        remember(parentId) { viewModel.types(parentId) }.collectAsState()
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val metaGroups by if (level == DatabaseLevel.TYPE) {
        viewModel.metaGroups.collectAsState()
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val sections = getSections(level, categories, groups, types, metaGroups, iconManager, navController)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp, top = 12.dp)
        )

        sections.forEachIndexed { index, section ->
            BaseContainer(
                title = section.title,
                modifier = Modifier.padding(bottom = if (index == sections.lastIndex) 0.dp else 16.dp),
                useSystemBarsPadding = false
            ) {
                BaseColumn(items = section.items)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

data class SectionModel(
    val title: String,
    val items: List<BaseLazyColumnItemModel>
)

@Composable
private fun getSections(
    level: DatabaseLevel,
    categories: List<CategoryEntity>,
    groups: List<GroupEntity>,
    types: List<TypeEntity>,
    metaGroups: List<MetaGroupEntity>,
    iconManager: IconManager,
    navController: NavController
): List<SectionModel> {
    val publishedTitle = stringResource(R.string.published)
    val unpublishedTitle = stringResource(R.string.unpublished)

    return remember(level, categories, groups, types, metaGroups, publishedTitle, unpublishedTitle) {
        when (level) {
            DatabaseLevel.CATEGORY -> {
                buildList {
                    val published = categories.filter { it.published == true }.map { category ->
                        createCategoryModel(category, iconManager, navController)
                    }
                    if (published.isNotEmpty()) add(SectionModel(publishedTitle, published))

                    val unpublished = categories.filter { it.published != true }.map { category ->
                        createCategoryModel(category, iconManager, navController)
                    }
                    if (unpublished.isNotEmpty()) add(SectionModel(unpublishedTitle, unpublished))
                }
            }

            DatabaseLevel.GROUP -> {
                buildList {
                    val published = groups.filter { it.published == true }.map { group ->
                        createGroupModel(group, iconManager, navController)
                    }
                    if (published.isNotEmpty()) add(SectionModel(publishedTitle, published))

                    val unpublished = groups.filter { it.published != true }.map { group ->
                        createGroupModel(group, iconManager, navController)
                    }
                    if (unpublished.isNotEmpty()) add(SectionModel(unpublishedTitle, unpublished))
                }
            }

            DatabaseLevel.TYPE -> {
                buildList {
                    // Split published items by metaGroupID
                    val publishedTypes = types.filter { it.published == true }
                    
                    if (publishedTypes.isNotEmpty()) {
                        val metaGroupMap = metaGroups.associateBy { it.id }
                        
                        // Group by metaGroupID
                        val grouped = publishedTypes.groupBy { it.metaGroupID }
                        
                        // Sort by metaGroupID or metaGroupName? 
                        // Let's sort by metaGroupID or keep them in the order they appear but usually there's a standard order.
                        // We'll sort by metaGroupID for stability.
                        grouped.keys.sortedBy { it ?: Int.MAX_VALUE }.forEach { metaId ->
                            val itemsInMeta = grouped[metaId] ?: emptyList()
                            val metaName = if (metaId == null) {
                                publishedTitle // fallback if null
                            } else {
                                metaGroupMap[metaId]?.name ?: "Meta Group $metaId"
                            }
                            
                            add(SectionModel(
                                metaName, 
                                itemsInMeta.map { createTypeModel(it, iconManager, navController) }
                            ))
                        }
                    }

                    val unpublished = types.filter { it.published != true }.map { type ->
                        createTypeModel(type, iconManager, navController)
                    }
                    if (unpublished.isNotEmpty()) add(SectionModel(unpublishedTitle, unpublished))
                }
            }
        }
    }
}

private fun createCategoryModel(
    category: CategoryEntity,
    iconManager: IconManager,
    navController: NavController
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(category.iconFilename),
    itemName = category.zhName ?: category.name.orEmpty(),
    onClick = {
        navController.navigate(
            DatabaseRoute.Group.create(category.id, category.zhName ?: category.name.orEmpty())
        )
    }
)

private fun createGroupModel(
    group: GroupEntity,
    iconManager: IconManager,
    navController: NavController
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(group.iconFilename),
    itemName = group.zhName ?: group.name.orEmpty(),
    onClick = {
        navController.navigate(
            DatabaseRoute.Type.create(group.id, group.zhName ?: group.name.orEmpty())
        )
    }
)

private fun createTypeModel(
    type: TypeEntity,
    iconManager: IconManager,
    navController: NavController
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(type.iconFilename),
    itemName = type.zhName ?: type.name.orEmpty(),
    onClick = {
        navController.navigate(DatabaseRoute.TypeDetail.create(type.id))
    }
)
