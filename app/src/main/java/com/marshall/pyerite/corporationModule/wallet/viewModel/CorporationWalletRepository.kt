package com.marshall.pyerite.corporationModule.wallet.viewModel

import com.marshall.pyerite.corporationModule.wallet.data.CorporationWalletLoader
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletLedger
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletsSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class CorporationWalletRepository(
    private val loader: CorporationWalletLoader,
) {
    private val walletsByCharacterId = ConcurrentHashMap<Long, CorporationWalletsSnapshot>()
    private val ledgerByKey = ConcurrentHashMap<String, CorporationWalletLedger>()
    private val walletLocks = ConcurrentHashMap<Long, Mutex>()
    private val ledgerLocks = ConcurrentHashMap<String, Mutex>()

    fun cachedWallets(characterId: Long): CorporationWalletsSnapshot? =
        walletsByCharacterId[characterId]

    fun cachedLedger(characterId: Long, division: Int): CorporationWalletLedger? =
        ledgerByKey[ledgerKey(characterId, division)]

    suspend fun loadWallets(
        characterId: Long,
        forceRefresh: Boolean = false,
    ): CorporationWalletsSnapshot {
        val lock = walletLocks.getOrPut(characterId) { Mutex() }
        return lock.withLock {
            if (!forceRefresh) {
                walletsByCharacterId[characterId]?.let { return@withLock it }
            }
            val loaded = loader.loadWallets(characterId)
            walletsByCharacterId[characterId] = loaded
            loaded
        }
    }

    suspend fun loadLedger(
        characterId: Long,
        division: Int,
        forceRefresh: Boolean = false,
    ): CorporationWalletLedger = withContext(Dispatchers.IO) {
        val key = ledgerKey(characterId, division)
        val lock = ledgerLocks.getOrPut(key) { Mutex() }
        lock.withLock {
            if (!forceRefresh) {
                ledgerByKey[key]?.let { return@withLock it }
            }
            val loaded = loader.loadLedger(characterId, division)
            ledgerByKey[key] = loaded
            loaded
        }
    }

    private fun ledgerKey(characterId: Long, division: Int): String = "$characterId:$division"
}
