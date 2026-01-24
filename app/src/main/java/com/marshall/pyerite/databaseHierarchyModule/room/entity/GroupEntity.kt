package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.*

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "group_id")
    val id: Int,
    @ColumnInfo(name = "categoryID")
    val categoryId: Int? = null,
    val name: String? = null,
    @ColumnInfo(name = "de_name") val deName: String? = null,
    @ColumnInfo(name = "en_name") val enName: String? = null,
    @ColumnInfo(name = "es_name") val esName: String? = null,
    @ColumnInfo(name = "fr_name") val frName: String? = null,
    @ColumnInfo(name = "ja_name") val jaName: String? = null,
    @ColumnInfo(name = "ko_name") val koName: String? = null,
    @ColumnInfo(name = "ru_name") val ruName: String? = null,
    @ColumnInfo(name = "zh_name") val zhName: String? = null,
    val iconID: Int? = null,
    @ColumnInfo(name = "icon_filename") val iconFilename: String? = null,
    val anchorable: Int? = null,
    val anchored: Int? = null,
    val fittableNonSingleton: Int? = null,
    val published: Boolean? = null,
    val useBasePrice: Int? = null
)
