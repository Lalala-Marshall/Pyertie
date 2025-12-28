package com.marshall.pyerite.mainPageModule

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyPage.navHostDatabasePage.DatabaseRoute
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumn
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

@Composable
fun MainPage(navController: NavController) {
    BaseContainer(
        title = stringResource(R.string.data),
        content = {
            BaseLazyColumn(
                items = listOf(
                    BaseLazyColumnItemModel(
                        iconRes = R.drawable.ic_database,
                        itemName = stringResource(R.string.database),
                        onClick = {
                            navController.navigate(DatabaseRoute.Root.route)
                        }
                    )
                )
            )
        }
    )
}
