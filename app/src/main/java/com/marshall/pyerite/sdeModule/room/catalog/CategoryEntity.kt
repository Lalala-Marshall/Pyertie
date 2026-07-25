package com.marshall.pyerite.sdeModule.room.catalog

import androidx.room.*
import com.marshall.pyerite.localization.LocalizableName

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "category_id")
    val id: Int,
    override val name: String? = null,
    @ColumnInfo(name = "de_name") val deName: String? = null,
    @ColumnInfo(name = "en_name") override val enName: String? = null,
    @ColumnInfo(name = "es_name") val esName: String? = null,
    @ColumnInfo(name = "fr_name") val frName: String? = null,
    @ColumnInfo(name = "ja_name") val jaName: String? = null,
    @ColumnInfo(name = "ko_name") val koName: String? = null,
    @ColumnInfo(name = "ru_name") val ruName: String? = null,
    @ColumnInfo(name = "zh_name") override val zhName: String? = null,
    @ColumnInfo(name = "icon_filename") val iconFilename: String? = null,
    @ColumnInfo(name = "iconID") val iconId: Int? = null,
    val published: Boolean? = true,
) : LocalizableName