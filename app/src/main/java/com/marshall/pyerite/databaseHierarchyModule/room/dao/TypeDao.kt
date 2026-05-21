package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeApplicableBlueprintCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeCompatibleGroupRef
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceCount
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

    @Query(
        """
        SELECT COUNT(DISTINCT typeid) AS count
        FROM typeMaterials
        WHERE output_material = :typeId
        """,
    )
    suspend fun getRefiningSourceCount(typeId: Int): TypeRefiningSourceCount?

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
}