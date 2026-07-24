package com.marshall.pyerite.sdeModule.room.type

import androidx.room.Dao
import androidx.room.Query

/**
 * Cross-feature type lookups (list skill names, sheet icons/names, etc.).
 * Hierarchy-only type queries stay on [TypeDao].
 */
@Dao
interface SdeTypeDao {
    @Query("SELECT * FROM types WHERE type_id = :typeId")
    suspend fun getTypeById(typeId: Int): TypeEntity?

    @Query(
        """
        SELECT icon_filename
        FROM types
        WHERE type_id = :typeId
        LIMIT 1
        """,
    )
    suspend fun getTypeIconFilename(typeId: Int): String?

    @Query(
        """
        SELECT type_id AS id, name, zh_name AS zhName, en_name AS enName
        FROM types
        WHERE type_id = :typeId
        LIMIT 1
        """,
    )
    suspend fun getTypeDisplayName(typeId: Int): TypeDisplayNameRow?
}
