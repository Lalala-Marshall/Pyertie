package com.marshall.pyerite.loyaltyPointsModule.data

import android.util.Log
import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.api.EsiMarketApi
import com.marshall.pyerite.esiModule.data.EsiPublicDataSource
import com.marshall.pyerite.esiModule.data.corporationLogoUrl
import com.marshall.pyerite.esiModule.model.EsiLoyaltyPointsDto
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationBalance
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyCorporationItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyFactionItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferCategoryItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyOfferRequiredItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyPointsConfig
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyStationItem
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyStationRegionGroup
import com.marshall.pyerite.sdeModule.room.RoomProvider
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity
import com.marshall.pyerite.sdeModule.room.loyalty.LoyaltyCorporationRow
import com.marshall.pyerite.sdeModule.room.loyalty.LoyaltyFactionRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val LOG_TAG = "LoyaltyPoints"

internal class LoyaltyPointsLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
    private val marketApi: EsiMarketApi,
    private val publicEsi: EsiPublicDataSource,
    private val roomProvider: RoomProvider,
    private val localeController: LocaleController,
) {
    private val pricesLock = Mutex()
    private var cachedPrices: Map<Int, Double>? = null
    private var cachedPricesAtMs: Long = 0L

    suspend fun fetchLoyaltyPointDtos(characterId: Long): List<EsiLoyaltyPointsDto> =
        withContext(Dispatchers.IO) {
            tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchLoyaltyPoints(characterId, auth)
            }
        }

    suspend fun enrichBalances(
        dtos: List<EsiLoyaltyPointsDto>,
    ): List<LoyaltyCorporationBalance> = withContext(Dispatchers.IO) {
        if (dtos.isEmpty()) return@withContext emptyList()
        val ids = dtos.map { it.corporationId }
        val sdeById = runCatching {
            roomProvider.getDatabase().loyaltyDao().getNpcCorporations(ids)
        }.onFailure { error ->
            Log.w(LOG_TAG, "npc corp lookup failed", error)
        }.getOrElse { emptyList() }.associateBy { it.corporationId }
        dtos.map { dto ->
            val sde = sdeById[dto.corporationId]
            if (sde != null) {
                LoyaltyCorporationBalance(
                    corporationId = dto.corporationId,
                    loyaltyPoints = dto.loyaltyPoints,
                    zhName = sde.zhName,
                    enName = sde.enName,
                    name = sde.name,
                    iconFilename = sde.iconFilename,
                    iconUrl = null,
                    isMilitia = LoyaltyPointsConfig.isMilitiaCorporation(sde.militiaFactionId),
                )
            } else {
                val org = runCatching { publicEsi.fetchCorporation(dto.corporationId) }.getOrNull()
                LoyaltyCorporationBalance(
                    corporationId = dto.corporationId,
                    loyaltyPoints = dto.loyaltyPoints,
                    zhName = null,
                    enName = null,
                    name = org?.name,
                    iconFilename = null,
                    iconUrl = corporationLogoUrl(dto.corporationId),
                    isMilitia = false,
                )
            }
        }.sortedByDescending { it.loyaltyPoints }
    }

    suspend fun loadFactions(): List<LoyaltyFactionItem> = withContext(Dispatchers.IO) {
        roomProvider.getDatabase().loyaltyDao().getFactionsWithLoyaltyOffers()
            .map { it.toFactionItem() }
            .sortedBy { it.displayName(localeController) }
    }

    suspend fun loadFaction(factionId: Int): LoyaltyFactionItem? = withContext(Dispatchers.IO) {
        roomProvider.getDatabase().loyaltyDao().getFaction(factionId)?.toFactionItem()
    }

    suspend fun loadCorporationsForFaction(factionId: Int): List<LoyaltyCorporationItem> =
        withContext(Dispatchers.IO) {
            roomProvider.getDatabase().loyaltyDao()
                .getCorporationsWithOffersByFaction(factionId)
                .map { it.toCorporationItem() }
                .sortedBy { it.displayName(localeController) }
        }

    suspend fun loadCorporation(corporationId: Long): LoyaltyCorporationItem? =
        withContext(Dispatchers.IO) {
            roomProvider.getDatabase().loyaltyDao()
                .getNpcCorporation(corporationId)
                ?.toCorporationItem()
        }

    suspend fun loadCategory(categoryId: Int): CategoryEntity? = withContext(Dispatchers.IO) {
        roomProvider.getDatabase().loyaltyDao().getCategory(categoryId)
    }

    suspend fun loadOfferCategories(corporationId: Long): List<LoyaltyOfferCategoryItem> =
        withContext(Dispatchers.IO) {
            roomProvider.getDatabase().loyaltyDao().getOfferCategoryCounts(corporationId)
                .map { row ->
                    LoyaltyOfferCategoryItem(
                        categoryId = row.categoryId,
                        zhName = row.zhName,
                        enName = row.enName,
                        name = row.name,
                        iconFilename = row.iconFilename,
                        offerCount = row.offerCount,
                    )
                }
                .sortedBy { it.displayName(localeController) }
        }

    suspend fun loadOffers(
        corporationId: Long,
        categoryId: Int,
        forceRefreshPrices: Boolean,
    ): List<LoyaltyOfferItem> = withContext(Dispatchers.IO) {
        val dao = roomProvider.getDatabase().loyaltyDao()
        val rows = dao.getOffersByCorporationAndCategory(corporationId, categoryId)
        val offerIds = rows.map { it.offerId }
        val requirements = if (offerIds.isEmpty()) {
            emptyList()
        } else {
            dao.getOfferRequirements(offerIds)
        }
        val requirementsByOffer = requirements.groupBy { it.offerId }
        val prices = loadPrices(forceRefresh = forceRefreshPrices)
        rows.map { row ->
            val required = requirementsByOffer[row.offerId].orEmpty().map { req ->
                LoyaltyOfferRequiredItem(
                    typeId = req.typeId,
                    quantity = req.quantity,
                    zhName = req.zhName,
                    enName = req.enName,
                    name = req.name,
                    iconFilename = req.iconFilename,
                )
            }
            LoyaltyOfferItem(
                offerId = row.offerId,
                typeId = row.typeId,
                quantity = row.quantity,
                lpCost = row.lpCost,
                iskCost = row.iskCost,
                zhName = row.zhName,
                enName = row.enName,
                name = row.name,
                iconFilename = row.iconFilename,
                requiredItems = required,
                requiredItemsIskValue = requiredItemsValue(required, prices),
            )
        }.sortedBy { it.displayName(localeController) }
    }

    suspend fun loadStations(corporationId: Long): List<LoyaltyStationRegionGroup> =
        withContext(Dispatchers.IO) {
            val rows = roomProvider.getDatabase().loyaltyDao()
                .getStationsForCorporation(corporationId)
            rows.groupBy { it.regionId ?: LoyaltyPointsConfig.UNKNOWN_REGION_ID }
                .map { (regionId, stationRows) ->
                    val first = stationRows.first()
                    LoyaltyStationRegionGroup(
                        regionId = regionId,
                        zhName = first.regionZhName,
                        enName = first.regionEnName,
                        name = first.regionName,
                        stations = stationRows
                            .map { row ->
                                LoyaltyStationItem(
                                    stationId = row.stationId,
                                    stationName = row.stationName,
                                    security = row.security,
                                    iconFilename = row.iconFilename,
                                )
                            }
                            .sortedBy { it.stationName.orEmpty() },
                    )
                }
                .sortedBy { it.displayName(localeController) }
        }

    private suspend fun loadPrices(forceRefresh: Boolean): Map<Int, Double> {
        pricesLock.withLock {
            val now = System.currentTimeMillis()
            val cached = cachedPrices
            if (!forceRefresh &&
                cached != null &&
                now - cachedPricesAtMs < LoyaltyPointsConfig.MARKET_PRICE_CACHE_TTL_MS
            ) {
                return cached
            }
            val loaded = runCatching {
                marketApi.fetchPrices().mapNotNull { dto ->
                    val average = dto.averagePrice ?: return@mapNotNull null
                    dto.typeId to average
                }.toMap()
            }.getOrNull()
            if (loaded != null) {
                cachedPrices = loaded
                cachedPricesAtMs = now
                return loaded
            }
            return cached.orEmpty()
        }
    }

    private fun requiredItemsValue(
        required: List<LoyaltyOfferRequiredItem>,
        prices: Map<Int, Double>,
    ): Double? {
        if (required.isEmpty()) return null
        var sum = 0.0
        var priced = false
        required.forEach { item ->
            val unit = prices[item.typeId] ?: return@forEach
            sum += unit * item.quantity
            priced = true
        }
        return if (priced) sum else null
    }
}

private fun LoyaltyFactionRow.toFactionItem() = LoyaltyFactionItem(
    factionId = factionId,
    zhName = zhName,
    enName = enName,
    name = name,
    iconFilename = iconFilename,
)

private fun LoyaltyCorporationRow.toCorporationItem() = LoyaltyCorporationItem(
    corporationId = corporationId,
    zhName = zhName,
    enName = enName,
    name = name,
    iconFilename = iconFilename,
    isMilitia = LoyaltyPointsConfig.isMilitiaCorporation(militiaFactionId),
)
