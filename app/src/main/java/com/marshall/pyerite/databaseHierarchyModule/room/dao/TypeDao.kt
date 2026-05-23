package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingMaterial
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingProduct
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingSkill
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeApplicableBlueprintCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeCompatibleGroupRef
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputItem
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceItem
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillUnlockTypeRow

@Dao
interface TypeDao {
    @Query("SELECT * FROM types WHERE groupID = :groupId ORDER BY type_id")
    suspend fun getTypesByGroup(groupId: Int): List<TypeEntity>

    @Query("SELECT * FROM types WHERE type_id = :typeId")
    suspend fun getTypeById(typeId: Int): TypeEntity?

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

    @Query(
        """
        SELECT DISTINCT
            blueprintTypeID AS typeId,
            blueprintTypeName AS name,
            blueprintTypeIcon AS iconFilename
        FROM blueprint_manufacturing_output
        WHERE typeID = :typeId
        ORDER BY blueprintTypeID
        """
    )
    suspend fun getBlueprintsForProduct(typeId: Int): List<TypeBlueprintDetail>

    @Query(
        """
        SELECT
            process_size AS processSize,
            COUNT(DISTINCT output_material) AS outputMaterialCount
        FROM typeMaterials
        WHERE typeid = :typeId
        GROUP BY typeid, process_size
        """
    )
    suspend fun getRefiningOutputSummary(typeId: Int): TypeRefiningOutputSummary?

    @Query(
        """
        SELECT COUNT(DISTINCT blueprintTypeID) AS count
        FROM (
            SELECT blueprintTypeID FROM blueprint_manufacturing_materials WHERE typeID = :typeId
            UNION SELECT blueprintTypeID FROM blueprint_invention_materials WHERE typeID = :typeId
            UNION SELECT blueprintTypeID FROM blueprint_copying_materials WHERE typeID = :typeId
            UNION SELECT blueprintTypeID FROM blueprint_research_material_materials WHERE typeID = :typeId
            UNION SELECT blueprintTypeID FROM blueprint_research_time_materials WHERE typeID = :typeId
        )
        """,
    )
    suspend fun getApplicableBlueprintCount(typeId: Int): TypeApplicableBlueprintCount?

    /** Count ore sources only (category 25 raw/compressed ore; excludes ice, moon, decorative asteroids). */
    @Query(
        """
        SELECT COUNT(DISTINCT tm.typeid) AS count
        FROM typeMaterials tm
        JOIN types t ON t.type_id = tm.typeid
        WHERE tm.output_material = :typeId
          AND t.categoryID = 25
          AND t.groupID NOT IN (
              465, 903, 519,
              1884, 1911, 1920, 1921, 1922, 1923,
              2006, 2022, 4094, 4161, 4714
          )
        """,
    )
    suspend fun getRefiningSourceCount(typeId: Int): TypeRefiningSourceCount?

    @Query(
        """
        SELECT
            output_material AS typeId,
            output_material_name AS name,
            output_material_icon AS iconFilename,
            output_quantity AS quantity
        FROM typeMaterials
        WHERE typeid = :typeId
        ORDER BY output_quantity DESC
        """,
    )
    suspend fun getRefiningOutputs(typeId: Int): List<TypeRefiningOutputItem>

    @Query(
        """
        SELECT
            tm.typeid AS typeId,
            COALESCE(t.zh_name, t.name) AS name,
            t.icon_filename AS iconFilename,
            t.metaGroupID AS metaGroupId,
            tm.output_quantity AS quantityPerUnit,
            tm.process_size AS processSize
        FROM typeMaterials tm
        JOIN types t ON t.type_id = tm.typeid
        WHERE tm.output_material = :typeId
          AND t.categoryID = 25
          AND t.groupID NOT IN (
              465, 903, 519,
              1884, 1911, 1920, 1921, 1922, 1923,
              2006, 2022, 4094, 4161, 4714
          )
        ORDER BY tm.output_quantity DESC
        """,
    )
    suspend fun getRefiningSources(typeId: Int): List<TypeRefiningSourceItem>

    @Query(
        """
        SELECT DISTINCT
            blueprintTypeID AS typeId,
            blueprintTypeName AS name,
            blueprintTypeIcon AS iconFilename
        FROM (
            SELECT blueprintTypeID, blueprintTypeName, blueprintTypeIcon
            FROM blueprint_manufacturing_materials WHERE typeID = :typeId
            UNION
            SELECT blueprintTypeID, blueprintTypeName, blueprintTypeIcon
            FROM blueprint_invention_materials WHERE typeID = :typeId
            UNION
            SELECT blueprintTypeID, blueprintTypeName, blueprintTypeIcon
            FROM blueprint_copying_materials WHERE typeID = :typeId
            UNION
            SELECT blueprintTypeID, blueprintTypeName, blueprintTypeIcon
            FROM blueprint_research_material_materials WHERE typeID = :typeId
            UNION
            SELECT blueprintTypeID, blueprintTypeName, blueprintTypeIcon
            FROM blueprint_research_time_materials WHERE typeID = :typeId
        )
        ORDER BY blueprintTypeID
        """,
    )
    suspend fun getApplicableBlueprints(typeId: Int): List<TypeBlueprintDetail>

    @Query(
        """
        SELECT
            da.attribute_id AS attributeId,
            da.name AS attributeName,
            da.display_name AS attributeDisplayName,
            da.icon_filename AS attributeIconFilename,
            CAST(ta.value AS INTEGER) AS groupId
        FROM typeAttributes ta
        JOIN dogmaAttributes da ON ta.attribute_id = da.attribute_id
        WHERE ta.type_id = :typeId
          AND ta.value IS NOT NULL
          AND ta.value > 0
          AND (da.name GLOB 'chargeGroup*' OR da.name GLOB 'launcherGroup*')
        ORDER BY da.attribute_id
        """,
    )
    suspend fun getCompatibleGroupRefs(typeId: Int): List<TypeCompatibleGroupRef>

    @Query(
        """
        SELECT COUNT(*) FROM types
        WHERE (type_id = :rootTypeId OR variationParentTypeID = :rootTypeId)
          AND published = 1
        """,
    )
    suspend fun getVariantCount(rootTypeId: Int): Int

    @Query(
        """
        SELECT * FROM types
        WHERE (type_id = :rootTypeId OR variationParentTypeID = :rootTypeId)
          AND published = 1
        ORDER BY metaGroupID, type_id
        """,
    )
    suspend fun getVariantsByRoot(rootTypeId: Int): List<TypeEntity>

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
            name,
            iconFilename,
            categoryId,
            categoryName
        FROM (
            SELECT
                t.type_id AS typeId,
                t.zh_name AS zhName,
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
            SELECT t.type_id, t.zh_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill2'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill2Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill3'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill3Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill4'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill4Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.name, t.icon_filename, t.categoryID, t.category_name
            FROM types t
            INNER JOIN typeAttributes ta_s ON ta_s.type_id = t.type_id
            INNER JOIN dogmaAttributes da_s ON ta_s.attribute_id = da_s.attribute_id AND da_s.name = 'requiredSkill5'
            INNER JOIN typeAttributes ta_l ON ta_l.type_id = t.type_id
            INNER JOIN dogmaAttributes da_l ON ta_l.attribute_id = da_l.attribute_id AND da_l.name = 'requiredSkill5Level'
            WHERE CAST(ta_s.value AS INTEGER) = :skillTypeId
              AND CAST(ta_l.value AS INTEGER) = :level
              AND t.published = 1
            UNION
            SELECT t.type_id, t.zh_name, t.name, t.icon_filename, t.categoryID, t.category_name
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

    @Query(
        """
        SELECT
            typeID AS typeId,
            typeName AS name,
            typeIcon AS iconFilename,
            quantity
        FROM blueprint_manufacturing_output
        WHERE blueprintTypeID = :blueprintTypeId
        """,
    )
    suspend fun getBlueprintManufacturingProducts(
        blueprintTypeId: Int,
    ): List<BlueprintManufacturingProduct>

    @Query(
        """
        SELECT
            typeID AS typeId,
            typeName AS name,
            typeIcon AS iconFilename,
            quantity
        FROM blueprint_manufacturing_materials
        WHERE blueprintTypeID = :blueprintTypeId
        ORDER BY quantity DESC, typeID
        """,
    )
    suspend fun getBlueprintManufacturingMaterials(
        blueprintTypeId: Int,
    ): List<BlueprintManufacturingMaterial>

    @Query(
        """
        SELECT
            typeID AS typeId,
            typeName AS name,
            typeIcon AS iconFilename,
            level
        FROM blueprint_manufacturing_skills
        WHERE blueprintTypeID = :blueprintTypeId
        ORDER BY level DESC, typeID
        """,
    )
    suspend fun getBlueprintManufacturingSkills(
        blueprintTypeId: Int,
    ): List<BlueprintManufacturingSkill>

    @Query(
        """
        SELECT manufacturing_time
        FROM blueprint_process_time
        WHERE blueprintTypeID = :blueprintTypeId
        """,
    )
    suspend fun getBlueprintManufacturingTime(blueprintTypeId: Int): Int?

    @Query(
        """
        SELECT research_material_time
        FROM blueprint_process_time
        WHERE blueprintTypeID = :blueprintTypeId
        """,
    )
    suspend fun getBlueprintResearchMaterialTime(blueprintTypeId: Int): Int?

    @Query(
        """
        SELECT research_time_time
        FROM blueprint_process_time
        WHERE blueprintTypeID = :blueprintTypeId
        """,
    )
    suspend fun getBlueprintResearchTimeTime(blueprintTypeId: Int): Int?
}