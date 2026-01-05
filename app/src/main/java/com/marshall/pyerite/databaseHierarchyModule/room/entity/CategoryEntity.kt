package com.marshall.pyerite.databaseHierarchyModule.room.entity

// 普通 POJO，不用 @Entity，不参与 Room 编译期检查
data class CategoryEntity(
    val id: Int,
    val iconFilename: String? = null,
    val name: String? = null,
    val publishedInt: Int = 1,
) {
    val published: Boolean
        get() = publishedInt != 0
}