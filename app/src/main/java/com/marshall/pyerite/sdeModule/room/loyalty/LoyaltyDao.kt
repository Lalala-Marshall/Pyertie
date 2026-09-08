package com.marshall.pyerite.sdeModule.room.loyalty

import androidx.room.Dao
import androidx.room.Query
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity

@Dao
interface LoyaltyDao {
    @Query(
        """
        SELECT
            f.id AS factionId,
            f.name,
            f.zh_name AS zhName,
            f.en_name AS enName,
            f.iconName AS iconFilename
        FROM factions f
        WHERE f.id IN (
            SELECT nc.faction_id
            FROM npcCorporations nc
            WHERE nc.corporation_id IN (SELECT corporation_id FROM loyalty_offers)
                AND nc.faction_id IS NOT NULL
        )
        ORDER BY f.name
        """,
    )
    suspend fun getFactionsWithLoyaltyOffers(): List<LoyaltyFactionRow>

    @Query(
        """
        SELECT
            id AS factionId,
            name,
            zh_name AS zhName,
            en_name AS enName,
            iconName AS iconFilename
        FROM factions
        WHERE id = :factionId
        LIMIT 1
        """,
    )
    suspend fun getFaction(factionId: Int): LoyaltyFactionRow?

    @Query(
        """
        SELECT
            nc.corporation_id AS corporationId,
            nc.name,
            nc.zh_name AS zhName,
            nc.en_name AS enName,
            nc.icon_filename AS iconFilename,
            nc.militia_faction AS militiaFactionId
        FROM npcCorporations nc
        WHERE nc.faction_id = :factionId
            AND nc.corporation_id IN (SELECT corporation_id FROM loyalty_offers)
        ORDER BY nc.name
        """,
    )
    suspend fun getCorporationsWithOffersByFaction(factionId: Int): List<LoyaltyCorporationRow>

    @Query(
        """
        SELECT
            corporation_id AS corporationId,
            name,
            zh_name AS zhName,
            en_name AS enName,
            icon_filename AS iconFilename,
            militia_faction AS militiaFactionId
        FROM npcCorporations
        WHERE corporation_id = :corporationId
        LIMIT 1
        """,
    )
    suspend fun getNpcCorporation(corporationId: Long): LoyaltyCorporationRow?

    @Query(
        """
        SELECT
            corporation_id AS corporationId,
            name,
            zh_name AS zhName,
            en_name AS enName,
            icon_filename AS iconFilename,
            militia_faction AS militiaFactionId
        FROM npcCorporations
        WHERE corporation_id IN (:corporationIds)
        """,
    )
    suspend fun getNpcCorporations(corporationIds: List<Long>): List<LoyaltyCorporationRow>

    @Query(
        """
        SELECT category_id, name, de_name, en_name, es_name, fr_name, ja_name, ko_name,
            ru_name, zh_name, icon_filename, iconID, published
        FROM categories
        WHERE category_id = :categoryId
        LIMIT 1
        """,
    )
    suspend fun getCategory(categoryId: Int): CategoryEntity?

    @Query(
        """
        SELECT
            c.category_id AS categoryId,
            c.name AS name,
            c.zh_name AS zhName,
            c.en_name AS enName,
            c.icon_filename AS iconFilename,
            COUNT(*) AS offerCount
        FROM loyalty_offers lo
        INNER JOIN loyalty_offer_outputs o ON o.offer_id = lo.offer_id
        INNER JOIN types t ON t.type_id = o.type_id
        INNER JOIN categories c ON c.category_id = t.categoryID
        WHERE lo.corporation_id = :corporationId
        GROUP BY c.category_id
        ORDER BY c.name
        """,
    )
    suspend fun getOfferCategoryCounts(corporationId: Long): List<LoyaltyOfferCategoryCountRow>

    @Query(
        """
        SELECT
            o.offer_id AS offerId,
            o.type_id AS typeId,
            o.quantity AS quantity,
            o.lp_cost AS lpCost,
            o.isk_cost AS iskCost,
            t.name AS name,
            t.zh_name AS zhName,
            t.en_name AS enName,
            t.icon_filename AS iconFilename
        FROM loyalty_offers lo
        INNER JOIN loyalty_offer_outputs o ON o.offer_id = lo.offer_id
        INNER JOIN types t ON t.type_id = o.type_id
        WHERE lo.corporation_id = :corporationId
            AND t.categoryID = :categoryId
        ORDER BY t.name
        """,
    )
    suspend fun getOffersByCorporationAndCategory(
        corporationId: Long,
        categoryId: Int,
    ): List<LoyaltyOfferRow>

    @Query(
        """
        SELECT
            r.offer_id AS offerId,
            r.required_type_id AS typeId,
            r.required_quantity AS quantity,
            t.name AS name,
            t.zh_name AS zhName,
            t.en_name AS enName,
            t.icon_filename AS iconFilename
        FROM loyalty_offer_requirements r
        INNER JOIN types t ON t.type_id = r.required_type_id
        WHERE r.offer_id IN (:offerIds)
        """,
    )
    suspend fun getOfferRequirements(offerIds: List<Int>): List<LoyaltyOfferRequirementRow>

    @Query(
        """
        SELECT DISTINCT
            s.stationID AS stationId,
            s.stationName AS stationName,
            s.security AS security,
            s.regionID AS regionId,
            r.regionName AS regionName,
            r.regionName_zh AS regionZhName,
            r.regionName_en AS regionEnName,
            t.icon_filename AS iconFilename
        FROM agents a
        INNER JOIN stations s ON s.stationID = a.locationID
        LEFT JOIN regions r ON r.regionID = s.regionID
        LEFT JOIN types t ON t.type_id = s.stationTypeID
        WHERE a.corporationID = :corporationId
        ORDER BY r.regionName, s.stationName
        """,
    )
    suspend fun getStationsForCorporation(corporationId: Long): List<LoyaltyStationRow>
}
