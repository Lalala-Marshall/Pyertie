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

    val (publishedItems, unpublishedItems) = getItems(level, categories, groups, types, iconManager, navController)

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

        if (publishedItems.isNotEmpty()) {
            BaseContainer(
                title = stringResource(R.string.published),
                modifier = Modifier.padding(bottom = 16.dp),
                useSystemBarsPadding = false
            ) {
                BaseColumn(items = publishedItems)
            }
        }

        if (unpublishedItems.isNotEmpty()) {
            BaseContainer(
                title = stringResource(R.string.unpublished),
                useSystemBarsPadding = false
            ) {
                BaseColumn(items = unpublishedItems)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
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
): Pair<List<BaseLazyColumnItemModel>, List<BaseLazyColumnItemModel>> {
    return remember(level, categories, groups, types) {
        when (level) {
            DatabaseLevel.CATEGORY -> {
                val published = categories.filter { it.published == true }.map { category ->
                    createCategoryModel(category, iconManager, navController)
                }
                val unpublished = categories.filter { it.published != true }.map { category ->
                    createCategoryModel(category, iconManager, navController)
                }
                published to unpublished
            }

            DatabaseLevel.GROUP -> {
                val published = groups.filter { it.published == true }.map { group ->
                    createGroupModel(group, iconManager, navController)
                }
                val unpublished = groups.filter { it.published != true }.map { group ->
                    createGroupModel(group, iconManager, navController)
                }
                published to unpublished
            }

            DatabaseLevel.TYPE -> {
                val published = types.filter { it.published == true }.map { type ->
                    createTypeModel(type, iconManager)
                }
                val unpublished = types.filter { it.published != true }.map { type ->
                    createTypeModel(type, iconManager)
                }
                published to unpublished
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
    iconManager: IconManager
) = BaseLazyColumnItemModel(
    iconFile = iconManager.getIconFile(type.iconFilename),
    itemName = type.zhName ?: type.name.orEmpty(),
    onClick = {
        // Type 一般是终点
    }
)
