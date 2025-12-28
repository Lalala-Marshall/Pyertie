package com.marshall.pyerite.databaseHierarchyPage.navHostDatabasePage

sealed class DatabaseRoute(val route: String) {

    object Root : DatabaseRoute("database")

    object Category : DatabaseRoute("database/category")

    object Group : DatabaseRoute("database/group/{categoryId}") {
        fun create(categoryId: Int) = "database/group/$categoryId"
    }

    object Type : DatabaseRoute("database/type/{groupId}") {
        fun create(groupId: Int) = "database/type/$groupId"
    }

    object TypeDetail : DatabaseRoute("database/typeDetail/{typeId}") {
        fun create(typeId: Int) = "database/typeDetail/$typeId"
    }
}
