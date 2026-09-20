package com.marshall.pyerite.corporationModule.wallet.model

internal fun mergeSimilarMarketTransactions(
    transactions: List<CorporationWalletMarketTransaction>,
): List<CorporationWalletMarketTransaction> {
    if (transactions.size <= 1) return transactions
    val grouped = LinkedHashMap<CorporationWalletTransactionMergeKey, MutableList<CorporationWalletMarketTransaction>>()
    transactions.forEach { transaction ->
        grouped.getOrPut(transaction.mergeKey()) { mutableListOf() }.add(transaction)
    }
    return grouped.values.map { group ->
        val first = group.first()
        if (group.size == 1) {
            first
        } else {
            first.copy(quantity = group.sumOf { it.quantity })
        }
    }
}

private data class CorporationWalletTransactionMergeKey(
    val dateEpochMs: Long,
    val typeId: Int,
    val locationId: Long,
    val isBuy: Boolean,
    val unitPrice: Double,
)

private fun CorporationWalletMarketTransaction.mergeKey() = CorporationWalletTransactionMergeKey(
    dateEpochMs = dateEpochMs,
    typeId = typeId,
    locationId = locationId,
    isBuy = isBuy,
    unitPrice = unitPrice,
)
