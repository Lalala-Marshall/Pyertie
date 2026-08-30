package com.marshall.pyerite.personalPropertyModule.data

import com.marshall.pyerite.esiModule.api.EsiCharacterApi
import com.marshall.pyerite.esiModule.api.EsiMarketApi
import com.marshall.pyerite.esiModule.model.EsiCharacterAssetDto
import com.marshall.pyerite.esiModule.model.EsiCharacterContractDto
import com.marshall.pyerite.esiModule.model.EsiCharacterOrderDto
import com.marshall.pyerite.esiModule.model.EsiContractItemDto
import com.marshall.pyerite.esiModule.model.EsiContractStatusValue
import com.marshall.pyerite.esiModule.model.EsiContractTypeValue
import com.marshall.pyerite.esiModule.model.EsiMarketPriceDto
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyBucket
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertyConfig
import com.marshall.pyerite.personalPropertyModule.model.PersonalPropertySummary
import com.marshall.pyerite.sdeModule.room.RoomProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Aggregates wallet, assets, plugged implants, market orders, and item-exchange
 * contracts into a net-worth summary. Does not keep per-item inventories.
 */
internal class PersonalPropertyLoader(
    private val tokenManager: EveTokenManager,
    private val characterApi: EsiCharacterApi,
    private val marketApi: EsiMarketApi,
    private val roomProvider: RoomProvider,
) {
    @Volatile
    private var cachedPrices: Map<Int, Double>? = null

    @Volatile
    private var cachedPricesAtMs: Long = 0L

    private val pricesLock = Mutex()

    suspend fun load(characterId: Long): PersonalPropertySummary = withContext(Dispatchers.IO) {
        coroutineScope {
            val pricesDeferred = async { loadPrices() }
            val walletDeferred = async { loadWallet(characterId) }
            val assetsDeferred = async { loadAssets(characterId) }
            val implantsDeferred = async { loadImplantTypeIds(characterId) }
            val ordersDeferred = async { loadOrders(characterId) }
            val contractsDeferred = async { loadContracts(characterId) }

            val prices = pricesDeferred.await()
            PersonalPropertySummary(
                characterId = characterId,
                walletIsk = walletDeferred.await(),
                assets = valueAssets(assetsDeferred.await(), prices),
                implants = valueImplants(implantsDeferred.await(), prices),
                marketOrders = valueOrders(ordersDeferred.await(), prices),
                contracts = valueContracts(contractsDeferred.await(), prices),
            )
        }
    }

    private suspend fun loadPrices(): Map<Int, Double>? {
        pricesLock.withLock {
            val now = System.currentTimeMillis()
            val cached = cachedPrices
            if (cached != null &&
                now - cachedPricesAtMs < PersonalPropertyConfig.MARKET_PRICE_CACHE_TTL_MS
            ) {
                return cached
            }
            return runCatching {
                marketApi.fetchPrices().associate { dto ->
                    dto.typeId to dto.unitPrice()
                }
            }.getOrNull()?.also { prices ->
                cachedPrices = prices
                cachedPricesAtMs = now
            }
        }
    }

    private suspend fun loadWallet(characterId: Long): Double? = runCatching {
        tokenManager.executeWithAuthRetry(characterId) { auth ->
            characterApi.fetchWallet(characterId, auth).use { body ->
                body.string().trim().toDouble()
            }
        }
    }.getOrNull()

    private suspend fun loadAssets(characterId: Long): List<EsiCharacterAssetDto>? = runCatching {
        val all = ArrayList<EsiCharacterAssetDto>()
        var page = PersonalPropertyConfig.FIRST_PAGE
        var pagesRemaining = PersonalPropertyConfig.ASSETS_MAX_PAGES
        while (pagesRemaining > 0) {
            pagesRemaining--
            val chunk = tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchAssets(characterId, auth, page)
            }
            all.addAll(chunk)
            if (chunk.size < PersonalPropertyConfig.ASSETS_PAGE_SIZE) break
            page++
        }
        all
    }.getOrNull()

    private suspend fun loadImplantTypeIds(characterId: Long): List<Int>? {
        val active = runCatching {
            tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchImplants(characterId, auth)
            }
        }.getOrNull()
        val jumpImplants = runCatching {
            tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchClones(characterId, auth)
            }.jumpClones.flatMap { it.implants }
        }.getOrNull()
        if (active == null && jumpImplants == null) return null
        return active.orEmpty() + jumpImplants.orEmpty()
    }

    private suspend fun loadOrders(characterId: Long): List<EsiCharacterOrderDto>? = runCatching {
        tokenManager.executeWithAuthRetry(characterId) { auth ->
            characterApi.fetchOrders(characterId, auth)
        }.filter { !it.isCorporation }
    }.getOrNull()

    private suspend fun loadContracts(characterId: Long): ContractLoadResult? {
        val contracts = runCatching { paginateContracts(characterId) }.getOrNull() ?: return null
        val outstanding = contracts.filter { contract ->
            contract.type == EsiContractTypeValue.ITEM_EXCHANGE &&
                contract.status == EsiContractStatusValue.OUTSTANDING &&
                contract.issuerId == characterId &&
                !contract.forCorporation
        }
        if (outstanding.isEmpty()) {
            return ContractLoadResult(count = 0, includedItems = emptyList())
        }
        val semaphore = Semaphore(PersonalPropertyConfig.CONTRACT_ITEMS_CONCURRENCY)
        val itemPages = coroutineScope {
            outstanding.map { contract ->
                async {
                    semaphore.withPermit {
                        runCatching {
                            tokenManager.executeWithAuthRetry(characterId) { auth ->
                                characterApi.fetchContractItems(
                                    characterId,
                                    contract.contractId,
                                    auth,
                                )
                            }
                        }.getOrNull()
                    }
                }
            }.awaitAll()
        }
        val included = itemPages.filterNotNull().flatten().filter { it.isIncluded }
        val allItemFetchesFailed = itemPages.all { it == null }
        return ContractLoadResult(
            count = outstanding.size,
            includedItems = if (allItemFetchesFailed) null else included,
        )
    }

    private suspend fun paginateContracts(characterId: Long): List<EsiCharacterContractDto> {
        val all = ArrayList<EsiCharacterContractDto>()
        var page = PersonalPropertyConfig.FIRST_PAGE
        var pagesRemaining = PersonalPropertyConfig.CONTRACTS_MAX_PAGES
        while (pagesRemaining > 0) {
            pagesRemaining--
            val chunk = tokenManager.executeWithAuthRetry(characterId) { auth ->
                characterApi.fetchContracts(characterId, auth, page)
            }
            all.addAll(chunk)
            if (chunk.size < PersonalPropertyConfig.CONTRACTS_PAGE_SIZE) break
            page++
        }
        return all
    }

    private suspend fun valueAssets(
        stacks: List<EsiCharacterAssetDto>?,
        prices: Map<Int, Double>?,
    ): PersonalPropertyBucket {
        if (stacks == null) return PersonalPropertyBucket()
        if (stacks.isEmpty()) return PersonalPropertyBucket(count = 0, isk = 0.0)
        val qtyByType = HashMap<Int, Long>()
        stacks.forEach { asset ->
            qtyByType[asset.typeId] = (qtyByType[asset.typeId] ?: 0L) + asset.quantity.toLong()
        }
        val blueprintIds = blueprintTypeIds(qtyByType.keys)
        val countedStacks = stacks.count { it.typeId !in blueprintIds }
        val isk = prices?.let { map ->
            qtyByType.entries.sumOf { (typeId, qty) ->
                if (typeId in blueprintIds) 0.0 else qty * map.priceOf(typeId)
            }
        }
        return PersonalPropertyBucket(count = countedStacks, isk = isk)
    }

    private fun valueImplants(
        typeIds: List<Int>?,
        prices: Map<Int, Double>?,
    ): PersonalPropertyBucket {
        if (typeIds == null) return PersonalPropertyBucket()
        if (typeIds.isEmpty()) return PersonalPropertyBucket(count = 0, isk = 0.0)
        return PersonalPropertyBucket(
            count = typeIds.size,
            isk = prices?.let { map -> typeIds.sumOf { map.priceOf(it) } },
        )
    }

    private fun valueOrders(
        orders: List<EsiCharacterOrderDto>?,
        prices: Map<Int, Double>?,
    ): PersonalPropertyBucket {
        if (orders == null) return PersonalPropertyBucket()
        if (orders.isEmpty()) return PersonalPropertyBucket(count = 0, isk = 0.0)
        val escrow = orders.filter { it.isBuyOrder }.sumOf { it.escrow }
        val sellOrders = orders.filter { !it.isBuyOrder }
        val isk = if (prices == null && sellOrders.isNotEmpty()) {
            null
        } else {
            val sellValue = prices?.let { map ->
                sellOrders.sumOf { order ->
                    order.volumeRemain * map.priceOf(order.typeId)
                }
            } ?: 0.0
            escrow + sellValue
        }
        return PersonalPropertyBucket(count = orders.size, isk = isk)
    }

    private fun valueContracts(
        result: ContractLoadResult?,
        prices: Map<Int, Double>?,
    ): PersonalPropertyBucket {
        if (result == null) return PersonalPropertyBucket()
        if (result.count == 0) return PersonalPropertyBucket(count = 0, isk = 0.0)
        val items = result.includedItems
        val isk = if (items == null || prices == null) {
            null
        } else {
            items.sumOf { item -> item.quantity * prices.priceOf(item.typeId) }
        }
        return PersonalPropertyBucket(count = result.count, isk = isk)
    }

    private suspend fun blueprintTypeIds(typeIds: Set<Int>): Set<Int> {
        if (typeIds.isEmpty()) return emptySet()
        return runCatching {
            val dao = roomProvider.getDatabase().sdeTypeDao()
            typeIds.chunked(PersonalPropertyConfig.TYPE_ID_QUERY_CHUNK)
                .flatMap { chunk -> dao.getTypeCategories(chunk) }
                .mapNotNull { row ->
                    row.typeId.takeIf {
                        row.categoryId == PersonalPropertyConfig.BLUEPRINT_CATEGORY_ID
                    }
                }
                .toSet()
        }.getOrDefault(emptySet())
    }

    private data class ContractLoadResult(
        val count: Int,
        val includedItems: List<EsiContractItemDto>?,
    )
}

private fun EsiMarketPriceDto.unitPrice(): Double = averagePrice ?: adjustedPrice ?: 0.0

private fun Map<Int, Double>.priceOf(typeId: Int): Double = this[typeId] ?: 0.0
