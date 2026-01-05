package com.marshall.pyerite.databaseHierarchyModule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseLevel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumn
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
    val categories by viewModel.categories.collectAsState()

    val groups by remember(parentId) {
        parentId?.let { viewModel.groups(it) }
    }?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }

    val types by remember(parentId) {
        parentId?.let { viewModel.types(it) }
    }?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }

    val items = getItems(level, categories, groups, types, iconManager, navController)

    BaseContainer(title = title) {
        BaseLazyColumn(items = items)
    }
}

@Composable
private fun getItems(
    level: DatabaseLevel,
    categories: List<CategoryEntity>,
    groups: List<GroupEntity>,
    types: List<TypeEntity>,
    iconManager: IconManager,
    navController: NavController
): List<BaseLazyColumnItemModel> {
    return remember(level, categories, groups, types) {
        when (level) {
            DatabaseLevel.CATEGORY -> {
                categories.map { category ->
                    BaseLazyColumnItemModel(
                        iconFile = iconManager.getIconFile(category.iconFilename),
                        itemName = category.name.orEmpty(),
                        onClick = {
                            navController.navigate(
                                DatabaseRoute.Group.create(category.id, category.name.orEmpty())
                            )
                        }
                    )
                }
            }

            DatabaseLevel.GROUP -> {
                groups.map { group ->
                    BaseLazyColumnItemModel(
                        iconFile = iconManager.getIconFile(group.iconFilename),
                        itemName = group.name.orEmpty(),
                        onClick = {
                            navController.navigate(
                                DatabaseRoute.Type.create(group.id, group.name.orEmpty())
                            )
                        }
                    )
                }
            }

            DatabaseLevel.TYPE -> {
                types.map { type ->
                    BaseLazyColumnItemModel(
                        iconFile = iconManager.getIconFile(type.iconFilename),
                        itemName = type.name.orEmpty(),
                        onClick = {
                            // Type 一般是终点
                        }
                    )
                }
            }
        }
    }
}
