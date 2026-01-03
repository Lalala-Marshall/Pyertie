package com.marshall.pyerite.databaseHierarchyModule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseLevel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
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

    val items = remember(level, categories) {
        if (level == DatabaseLevel.CATEGORY) {
            categories.map { category ->
                BaseLazyColumnItemModel(
                    iconFile = iconManager.getIconFile(category.iconFilename),
                    itemName = category.zhName ?: category.name.orEmpty(),
                    onClick = {
                        navController.navigate(DatabaseRoute.Group.create(category.id))
                    }
                )
            }
        } else emptyList()
    }

    BaseContainer(title = title) {
        BaseLazyColumn(items = items)
    }
}
