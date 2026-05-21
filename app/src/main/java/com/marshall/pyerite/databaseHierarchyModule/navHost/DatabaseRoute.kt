package com.marshall.pyerite.databaseHierarchyModule.navHost

import android.net.Uri

sealed class DatabaseRoute(val route: String) {

    object Root : DatabaseRoute("database")

    object Category : DatabaseRoute("database/category")

    object Group : DatabaseRoute("database/group/{categoryId}/{categoryName}") {
        fun create(categoryId: Int, name: String) = "database/group/$categoryId/${Uri.encode(name)}"
    }

    object Type : DatabaseRoute("database/type/{groupId}/{groupName}") {
        fun create(groupId: Int, name: String) = "database/type/$groupId/${Uri.encode(name)}"
    }

    object TypeDetail : DatabaseRoute("database/typeDetail/{typeId}") {
        fun create(typeId: Int) = "database/typeDetail/$typeId"
    }

    object TypeVariants : DatabaseRoute("database/typeVariants/{typeId}") {
        fun create(typeId: Int) = "database/typeVariants/$typeId"
    }
}
