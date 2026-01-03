package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity

@Dao
interface CategoryDao {
    @RawQuery
    suspend fun getCategoriesRaw(query: SupportSQLiteQuery = SimpleSQLiteQuery("""
        SELECT
            category_id AS id,
            de_name AS deName,
            en_name AS enName,
            es_name AS esName,
            fr_name AS frName,
            iconID,
            icon_filename AS iconFilename,
            ja_name AS jaName,
            ko_name AS koName,
            name,
            CAST(published AS INTEGER) AS publishedInt,
            ru_name AS ruName,
            zh_name AS zhName
        FROM categories
        WHERE published != 0
    """)): List<CategoryEntity>
}