package com.marshall.pyerite.databaseHierarchyPage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyPage.navHostDatabasePage.DatabaseLevel
import com.marshall.pyerite.databaseHierarchyPage.navHostDatabasePage.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyPage.room.DatabaseListItem
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumn
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

@Composable
fun DatabaseHierarchyPage(
    title: String,
    level: DatabaseLevel,
    parentId: Int?,
    navController: NavController
) {
    val items = remember(level, parentId) {
        fakeLoadItems(level, parentId)
    }

    BaseContainer(title = title) {
        BaseLazyColumn(
            items = items.map { item ->
                BaseLazyColumnItemModel(
                    iconRes = item.iconRes,
                    itemName = item.title,
                    onClick = {
                        when (level) {
                            DatabaseLevel.CATEGORY ->
                                navController.navigate(
                                    DatabaseRoute.Group.create(item.id)
                                )

                            DatabaseLevel.GROUP ->
                                navController.navigate(
                                    DatabaseRoute.Type.create(item.id)
                                )

                            DatabaseLevel.TYPE -> {
                                // 下一步是 TypeDetail（后面再做）
                            }
                        }
                    }
                )
            }
        )
    }
}

private fun fakeLoadItems(
    level: DatabaseLevel,
    parentId: Int?
): List<DatabaseListItem> {
    return when (level) {
        DatabaseLevel.CATEGORY -> listOf(
            DatabaseListItem(1, "Ships", R.drawable.ic_database),
            DatabaseListItem(2, "Modules", R.drawable.ic_database)
        )

        DatabaseLevel.GROUP -> listOf(
            DatabaseListItem(10, "Frigate", R.drawable.ic_database),
            DatabaseListItem(11, "Cruiser", R.drawable.ic_database)
        )

        DatabaseLevel.TYPE -> listOf(
            DatabaseListItem(100, "Rifter", R.drawable.ic_database),
            DatabaseListItem(101, "Punisher", R.drawable.ic_database)
        )
    }
}
