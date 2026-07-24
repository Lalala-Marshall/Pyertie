package com.marshall.pyerite.sdeModule.room.catalog

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY category_id")
    suspend fun getCategories(): List<CategoryEntity>
}