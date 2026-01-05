package com.marshall.pyerite.databaseHierarchyModule.room.entity

data class TypeEntity(
    val id: Int,
    val groupId: Int,
    val name: String?,
    val zhName: String?,
    val iconFilename: String?,
    val publishedInt: Int
) {
    val published: Boolean
        get() = publishedInt != 0
}
