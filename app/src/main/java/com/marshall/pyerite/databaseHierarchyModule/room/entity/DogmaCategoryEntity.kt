package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dogmaAttributeCategories")
data class DogmaCategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "attribute_category_id")
    val id: Int,
    val name: String?,
    val description: String?
)
