package com.marshall.pyerite.databaseHierarchyModule.room.entity

data class GroupEntity(
    val id: Int,
    val categoryId: Int,
    val name: String?,
    val zhName: String?,
    val iconID: Int?,
    val iconFilename: String?,
    val publishedInt: Int
) {
    val published: Boolean
        get() = publishedInt != 0
}
