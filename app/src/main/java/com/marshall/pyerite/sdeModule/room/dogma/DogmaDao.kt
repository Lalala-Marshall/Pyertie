package com.marshall.pyerite.sdeModule.room.dogma

import androidx.room.Dao
import androidx.room.Query

@Dao
interface DogmaDao {
    @Query("""
        SELECT 
            ta.attribute_id as attributeId,
            ta.value as value,
            da.name as name,
            da.display_name as displayName,
            da.unitName as unitName,
            da.icon_filename as iconFilename,
            da.categoryID as categoryId,
            dac.name as categoryName
        FROM typeAttributes ta
        JOIN dogmaAttributes da ON ta.attribute_id = da.attribute_id
        JOIN dogmaAttributeCategories dac ON da.categoryID = dac.attribute_category_id
        WHERE ta.type_id = :typeId
        ORDER BY da.categoryID, ta.attribute_id
    """)
    suspend fun getTypeAttributeDetails(typeId: Int): List<TypeAttributeDetail>

    @Query("SELECT * FROM dogmaAttributes WHERE name IN (:names)")
    suspend fun getDogmaAttributesByNames(names: List<String>): List<DogmaAttributeEntity>

    @Query("SELECT * FROM dogmaAttributes WHERE attribute_id IN (:attributeIds)")
    suspend fun getDogmaAttributesByIds(attributeIds: List<Int>): List<DogmaAttributeEntity>

    /**
     * Skill metadata dogma attributes that co-occur on types with [anchorName]
     * (EVE SDE marker: `skillTimeConstant`).
     */
    @Query(
        """
        SELECT DISTINCT da.*
        FROM dogmaAttributes da
        INNER JOIN typeAttributes ta ON da.attribute_id = ta.attribute_id
        WHERE ta.type_id IN (
            SELECT ta_anchor.type_id
            FROM typeAttributes ta_anchor
            INNER JOIN dogmaAttributes da_anchor ON ta_anchor.attribute_id = da_anchor.attribute_id
            WHERE da_anchor.name = :anchorName
        )
        AND (
            da.name = :anchorName
            OR da.name = 'skillLevel'
            OR (
                da.name LIKE '%Attribute'
                AND da.name NOT LIKE 'required%'
                AND da.name NOT LIKE '%Bonus%'
            )
        )
        ORDER BY da.attribute_id
        """,
    )
    suspend fun getSkillMiscDogmaDefinitions(anchorName: String): List<DogmaAttributeEntity>

    /**
     * Per-skill effect attributes (damage bonus, consumption bonus, ally cost modifier, etc.)
     * discovered on types that have [anchorName], excluding metadata and prerequisites.
     */
    @Query(
        """
        SELECT DISTINCT da.*
        FROM dogmaAttributes da
        INNER JOIN typeAttributes ta ON da.attribute_id = ta.attribute_id
        WHERE ta.type_id IN (
            SELECT ta_anchor.type_id
            FROM typeAttributes ta_anchor
            INNER JOIN dogmaAttributes da_anchor ON ta_anchor.attribute_id = da_anchor.attribute_id
            WHERE da_anchor.name = :anchorName
        )
        AND da.name != :anchorName
        AND da.name != 'skillLevel'
        AND NOT (
            da.name LIKE '%Attribute'
            AND da.name NOT LIKE 'required%'
            AND da.name NOT LIKE '%Bonus%'
        )
        AND da.name NOT LIKE 'requiredSkill%'
        AND da.name NOT IN ('canNotBeTrainedOnTrial', 'isSkillIObsolete', 'skillPoints')
        AND (
            da.name LIKE '%Bonus'
            OR da.name LIKE '%bonus'
            OR da.name LIKE '%Mutator'
            OR da.name LIKE '%Modifier'
            OR da.name LIKE '%modifier'
            OR da.name LIKE '%PerLevel'
        )
        ORDER BY da.attribute_id
        """,
    )
    suspend fun getSkillBonusDogmaDefinitions(anchorName: String): List<DogmaAttributeEntity>
}
