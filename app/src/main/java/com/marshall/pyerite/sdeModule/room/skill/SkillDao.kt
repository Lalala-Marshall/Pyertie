package com.marshall.pyerite.sdeModule.room.skill

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity
import com.marshall.pyerite.sdeModule.room.catalog.GroupEntity
import com.marshall.pyerite.sdeModule.room.type.TypeEntity

@Dao
interface SkillDao {
    /**
     * Distinct skill levels at which at least one published type lists [skillTypeId]
     * in a `requiredSkill1`–`requiredSkill6` slot.
     */
    @Query(
        """
        SELECT DISTINCT req_level FROM (
            SELECT CAST(ta_l.value AS INTEGER) AS req_level
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill1'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill1Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId AND t.published = 1
            UNION
            SELECT CAST(ta_l.value AS INTEGER)
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill2'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill2Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId AND t.published = 1
            UNION
            SELECT CAST(ta_l.value AS INTEGER)
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill3'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill3Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId AND t.published = 1
            UNION
            SELECT CAST(ta_l.value AS INTEGER)
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill4'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill4Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId AND t.published = 1
            UNION
            SELECT CAST(ta_l.value AS INTEGER)
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill5'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill5Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId AND t.published = 1
            UNION
            SELECT CAST(ta_l.value AS INTEGER)
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill6'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill6Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId AND t.published = 1
        )
        WHERE req_level BETWEEN 1 AND :maxLevel
        ORDER BY req_level
        """,
    )
    suspend fun getSkillUnlockLevels(skillTypeId: Int, maxLevel: Int): List<Int>

    @Query(
        """
        SELECT DISTINCT
            typeId,
            zhName,
            enName,
            name,
            iconFilename,
            categoryId,
            categoryName
        FROM (
            SELECT
                t.type_id AS typeId,
                t.zh_name AS zhName,
                t.en_name AS enName,
                t.name AS name,
                t.icon_filename AS iconFilename,
                t.categoryID AS categoryId,
                t.category_name AS categoryName
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill1'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill1Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.en_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill2'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill2Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.en_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill3'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill3Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.en_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill4'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill4Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.en_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill5'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill5Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.en_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill6'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill6Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
        )
        ORDER BY categoryId, zhName
        """,
    )
    suspend fun getTypesUnlockedBySkillAtLevel(skillTypeId: Int, level: Int): List<SkillUnlockTypeRow>

    /**
     * Published skill types in [categoryId] with dogma `skillTimeConstant`
     * (for skill-catalog group SP aggregation) and primary/secondary attributes.
     */
    @Query(
        """
        SELECT
            t.type_id AS typeId,
            t.groupID AS groupId,
            t.name AS name,
            t.zh_name AS zhName,
            t.en_name AS enName,
            ta.value AS skillTimeConstant,
            primary_ta.value AS primaryAttributeId,
            secondary_ta.value AS secondaryAttributeId,
            t.icon_filename AS iconFilename
        FROM types t
        INNER JOIN typeAttributes ta ON ta.type_id = t.type_id
        INNER JOIN dogmaAttributes da
            ON da.attribute_id = ta.attribute_id AND da.name = 'skillTimeConstant'
        LEFT JOIN typeAttributes primary_ta
            ON primary_ta.type_id = t.type_id
           AND primary_ta.attribute_id = (
               SELECT pda.attribute_id FROM dogmaAttributes pda
               WHERE pda.name = 'primaryAttribute' LIMIT 1
           )
        LEFT JOIN typeAttributes secondary_ta
            ON secondary_ta.type_id = t.type_id
           AND secondary_ta.attribute_id = (
               SELECT sda.attribute_id FROM dogmaAttributes sda
               WHERE sda.name = 'secondaryAttribute' LIMIT 1
           )
        WHERE t.categoryID = :categoryId
          AND t.published = 1
          AND ta.value IS NOT NULL
          AND ta.value > 0
        ORDER BY t.groupID, t.type_id
        """,
    )
    suspend fun getSkillCatalogTypes(categoryId: Int): List<SkillCatalogTypeRow>

    /**
     * Published categories (excluding [skillsCategoryId]) that contain at least one
     * published type with a `requiredSkill1`–`6` dogma attribute.
     */
    @Query(
        """
        SELECT * FROM categories
        WHERE published = 1
          AND category_id != :skillsCategoryId
          AND category_id IN (
            SELECT DISTINCT t.categoryID
            FROM types t
            INNER JOIN typeAttributes ta ON ta.type_id = t.type_id
            INNER JOIN dogmaAttributes da ON da.attribute_id = ta.attribute_id
            WHERE t.published = 1
              AND t.categoryID IS NOT NULL
              AND da.name IN (
                  'requiredSkill1', 'requiredSkill2', 'requiredSkill3',
                  'requiredSkill4', 'requiredSkill5', 'requiredSkill6'
              )
              AND ta.value IS NOT NULL
              AND CAST(ta.value AS INTEGER) > 0
          )
        ORDER BY category_id
        """,
    )
    suspend fun getCategoriesWithSkillRequirements(skillsCategoryId: Int): List<CategoryEntity>

    /**
     * Published groups in [categoryId] that contain at least one published type
     * with a skill requirement.
     */
    @Query(
        """
        SELECT * FROM `groups`
        WHERE published = 1
          AND categoryID = :categoryId
          AND group_id IN (
            SELECT DISTINCT t.groupID
            FROM types t
            INNER JOIN typeAttributes ta ON ta.type_id = t.type_id
            INNER JOIN dogmaAttributes da ON da.attribute_id = ta.attribute_id
            WHERE t.published = 1
              AND t.categoryID = :categoryId
              AND t.groupID IS NOT NULL
              AND da.name IN (
                  'requiredSkill1', 'requiredSkill2', 'requiredSkill3',
                  'requiredSkill4', 'requiredSkill5', 'requiredSkill6'
              )
              AND ta.value IS NOT NULL
              AND CAST(ta.value AS INTEGER) > 0
          )
        ORDER BY group_id
        """,
    )
    suspend fun getGroupsWithSkillRequirements(categoryId: Int): List<GroupEntity>

    /**
     * Published types in [groupId] that list at least one required skill.
     */
    @Query(
        """
        SELECT * FROM types
        WHERE published = 1
          AND groupID = :groupId
          AND type_id IN (
            SELECT DISTINCT t.type_id
            FROM types t
            INNER JOIN typeAttributes ta ON ta.type_id = t.type_id
            INNER JOIN dogmaAttributes da ON da.attribute_id = ta.attribute_id
            WHERE t.published = 1
              AND t.groupID = :groupId
              AND da.name IN (
                  'requiredSkill1', 'requiredSkill2', 'requiredSkill3',
                  'requiredSkill4', 'requiredSkill5', 'requiredSkill6'
              )
              AND ta.value IS NOT NULL
              AND CAST(ta.value AS INTEGER) > 0
          )
        ORDER BY type_id
        """,
    )
    suspend fun getTypesWithSkillRequirements(groupId: Int): List<TypeEntity>

    /**
     * Search published types (excluding skill category) that have skill requirements.
     */
    @Query(
        """
        SELECT * FROM types
        WHERE published = 1
          AND categoryID IS NOT NULL
          AND categoryID != :skillsCategoryId
          AND (
              zh_name LIKE :pattern COLLATE NOCASE
              OR en_name LIKE :pattern COLLATE NOCASE
              OR name LIKE :pattern COLLATE NOCASE
          )
          AND type_id IN (
            SELECT DISTINCT t.type_id
            FROM types t
            INNER JOIN typeAttributes ta ON ta.type_id = t.type_id
            INNER JOIN dogmaAttributes da ON da.attribute_id = ta.attribute_id
            WHERE t.published = 1
              AND t.categoryID IS NOT NULL
              AND t.categoryID != :skillsCategoryId
              AND da.name IN (
                  'requiredSkill1', 'requiredSkill2', 'requiredSkill3',
                  'requiredSkill4', 'requiredSkill5', 'requiredSkill6'
              )
              AND ta.value IS NOT NULL
              AND CAST(ta.value AS INTEGER) > 0
          )
        ORDER BY groupID, type_id
        LIMIT :limit
        """,
    )
    suspend fun searchTypesWithSkillRequirements(
        pattern: String,
        skillsCategoryId: Int,
        limit: Int,
    ): List<TypeEntity>

    /**
     * Published skill types in [categoryId] with bilingual names for plan import/export.
     */
    @Query(
        """
        SELECT
            type_id AS typeId,
            zh_name AS zhName,
            en_name AS enName,
            name AS name
        FROM types
        WHERE categoryID = :categoryId
          AND published = 1
        ORDER BY type_id
        """,
    )
    suspend fun getSkillNameRows(categoryId: Int): List<SkillNameRow>
}
