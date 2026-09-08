package com.marshall.pyerite.loyaltyPointsModule.viewModel

import com.marshall.pyerite.esiModule.model.EsiLoyaltyPointsDto
import com.marshall.pyerite.loyaltyPointsModule.data.LoyaltyPointsLoader
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationBalance
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyFactionItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferCategoryItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyStationRegionGroup
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class LoyaltyPointsRepository(
    private val loader: LoyaltyPointsLoader,
) {
    private val dtosByCharacterId = ConcurrentHashMap<Long, List<EsiLoyaltyPointsDto>>()

    suspend fun loadBalances(
        characterId: Long,
        forceRefresh: Boolean,
    ): List<LoyaltyCorporationBalance> = withContext(Dispatchers.IO) {
        val cachedDtos = dtosByCharacterId[characterId]
        val dtos = if (!forceRefresh && cachedDtos != null) {
            cachedDtos
        } else {
            runCatching { loader.fetchLoyaltyPointDtos(characterId) }
                .onSuccess { dtosByCharacterId[characterId] = it }
                .getOrElse { error ->
                    if (cachedDtos == null) throw error
                    cachedDtos
                }
        }
        loader.enrichBalances(dtos)
    }

    suspend fun loadFactions(): List<LoyaltyFactionItem> = loader.loadFactions()

    suspend fun loadFaction(factionId: Int): LoyaltyFactionItem? = loader.loadFaction(factionId)

    suspend fun loadCorporationsForFaction(factionId: Int): List<LoyaltyCorporationItem> =
        loader.loadCorporationsForFaction(factionId)

    suspend fun loadCorporation(corporationId: Long): LoyaltyCorporationItem? =
        loader.loadCorporation(corporationId)

    suspend fun loadCategory(categoryId: Int): CategoryEntity? = loader.loadCategory(categoryId)

    suspend fun loadOfferCategories(corporationId: Long): List<LoyaltyOfferCategoryItem> =
        loader.loadOfferCategories(corporationId)

    suspend fun loadOffers(
        corporationId: Long,
        categoryId: Int,
        forceRefreshPrices: Boolean,
    ): List<LoyaltyOfferItem> = loader.loadOffers(
        corporationId = corporationId,
        categoryId = categoryId,
        forceRefreshPrices = forceRefreshPrices,
    )

    suspend fun loadStations(corporationId: Long): List<LoyaltyStationRegionGroup> =
        loader.loadStations(corporationId)
}
