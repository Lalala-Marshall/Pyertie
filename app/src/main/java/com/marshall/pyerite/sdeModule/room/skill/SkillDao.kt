package com.marshall.pyerite.sdeModule.room.skill

import androidx.room.Dao
import androidx.room.Query

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
}
