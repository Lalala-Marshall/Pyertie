package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dogmaAttributes")
data class DogmaAttributeEntity(
    @PrimaryKey
    @ColumnInfo(name = "attribute_id")
    val id: Int,
    @ColumnInfo(name = "categoryID")
    val categoryId: Int?,
    val name: String?,
    @ColumnInfo(name = "display_name")
    val displayName: String?,
    val tooltipDescription: String?,
    @ColumnInfo(name = "iconID")
    val iconId: Int?,
    @ColumnInfo(name = "icon_filename")
    val iconFilename: String?,
    @ColumnInfo(name = "unitID")
    val unitId: Int?,
    @ColumnInfo(name = "unitName")
    val unitName: String?,
    @ColumnInfo(name = "defaultValue")
    val defaultValue: Double?,
    @ColumnInfo(name = "highIsGood")
    val highIsGood: Int?,
    @ColumnInfo(name = "stackable")
    val stackable: Int?
)
