package com.marshall.pyerite.databaseHierarchyModule.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
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
}