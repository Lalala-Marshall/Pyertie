package com.marshall.pyerite.databaseHierarchyModule.room.entity

// 普通 POJO，不用 @Entity，不参与 Room 编译期检查
data class CategoryEntity(
    val id: Int,
    val deName: String? = null,
    val enName: String? = null,
    val esName: String? = null,
    val frName: String? = null,
    val iconID: Int? = null,
    val iconFilename: String? = null,
    val jaName: String? = null,
    val koName: String? = null,
    val name: String? = null,
    val publishedInt: Int = 1,
    val ruName: String? = null,
    val zhName: String? = null
) {
    val published: Boolean
        get() = publishedInt != 0
}