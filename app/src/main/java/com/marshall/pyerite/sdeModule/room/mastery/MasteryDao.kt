package com.marshall.pyerite.sdeModule.room.mastery

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.sdeModule.room.catalog.GroupEntity

@Dao
interface MasteryDao {
    @Query("SELECT * FROM masteries")
    suspend fun getAllMasteries(): List<MasteryEntity>

    @Query("SELECT * FROM certificateSkills")
    suspend fun getAllCertificateSkills(): List<CertificateSkillEntity>

    @Query(
        """
        SELECT
            t.type_id AS typeId,
            t.groupID AS groupId,
            t.name AS name,
            t.zh_name AS zhName,
            t.en_name AS enName,
            t.icon_filename AS iconFilename,
            t.metaGroupID AS metaGroupId,
            t.published AS published
        FROM types t
        WHERE t.categoryID = :categoryId
          AND t.type_id IN (SELECT DISTINCT typeid FROM masteries)
        """,
    )
    suspend fun getMasteryShipTypes(categoryId: Int): List<MasteryShipTypeRow>

    @Query(
        """
        SELECT * FROM `groups`
        WHERE categoryID = :categoryId
          AND published = 1
          AND group_id IN (
            SELECT DISTINCT t.groupID
            FROM types t
            WHERE t.categoryID = :categoryId
              AND t.groupID IS NOT NULL
              AND t.type_id IN (SELECT DISTINCT typeid FROM masteries)
          )
        ORDER BY group_id
        """,
    )
    suspend fun getMasteryShipGroups(categoryId: Int): List<GroupEntity>

    @Query(
        """
        SELECT * FROM typeSkillRequirement
        WHERE typeid IN (SELECT DISTINCT typeid FROM masteries)
        """,
    )
    suspend fun getMasteryTypeSkillRequirements(): List<TypeSkillRequirementEntity>
}
