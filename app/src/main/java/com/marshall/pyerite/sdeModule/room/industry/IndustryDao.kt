package com.marshall.pyerite.sdeModule.room.industry

import androidx.room.Dao
import androidx.room.Query

@Dao
interface IndustryDao {
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

    @Query(
        """
        SELECT copying_time AS copyingTimeSeconds, maxRunsPerCopy AS maxRunsPerCopy
        FROM blueprint_process_time
        WHERE blueprintTypeID = :blueprintTypeId
        """,
    )
    suspend fun getBlueprintCopyDetail(blueprintTypeId: Int): BlueprintCopyDetail?

    @Query(
        """
        SELECT
            typeID AS typeId,
            typeName AS name,
            typeIcon AS iconFilename,
            quantity,
            probability
        FROM blueprint_invention_products
        WHERE blueprintTypeID = :blueprintTypeId
        ORDER BY probability DESC, typeID
        """,
    )
    suspend fun getBlueprintInventionProducts(
        blueprintTypeId: Int,
    ): List<BlueprintInventionProduct>

    @Query(
        """
        SELECT
            typeID AS typeId,
            typeName AS name,
            typeIcon AS iconFilename,
            quantity
        FROM blueprint_invention_materials
        WHERE blueprintTypeID = :blueprintTypeId
        ORDER BY quantity DESC, typeID
        """,
    )
    suspend fun getBlueprintInventionMaterials(
        blueprintTypeId: Int,
    ): List<BlueprintInventionMaterial>

    @Query(
        """
        SELECT
            typeID AS typeId,
            typeName AS name,
            typeIcon AS iconFilename,
            level
        FROM blueprint_invention_skills
        WHERE blueprintTypeID = :blueprintTypeId
        ORDER BY level DESC, typeID
        """,
    )
    suspend fun getBlueprintInventionSkills(
        blueprintTypeId: Int,
    ): List<BlueprintInventionSkill>

    @Query(
        """
        SELECT invention_time
        FROM blueprint_process_time
        WHERE blueprintTypeID = :blueprintTypeId
        """,
    )
    suspend fun getBlueprintInventionTime(blueprintTypeId: Int): Int?
}
