package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeTraitDetail

@Dao
interface TraitDao {
    @Query(
        """
        SELECT t.content AS content,
               t.skill AS skill,
               t.importance AS importance,
               t.bonus_type AS bonusType,
               st.zh_name AS zhName,
               st.en_name AS enName
        FROM traits t
        LEFT JOIN types st ON st.type_id = t.skill AND t.skill != -1
        WHERE t.typeid = :typeId
        ORDER BY (t.importance IS NULL), t.importance ASC, t.skill ASC
        """
    )
    suspend fun getTraitsForType(typeId: Int): List<TypeTraitDetail>
}
