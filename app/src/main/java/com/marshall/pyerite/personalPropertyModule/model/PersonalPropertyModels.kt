package com.marshall.pyerite.personalPropertyModule.model

import com.marshall.pyerite.localization.LocalizableName

internal data class PersonalPropertyBucket(
    val count: Long? = null,
    val isk: Double? = null,
)

internal data class PersonalPropertySummary(
    val characterId: Long,
    val walletIsk: Double? = null,
    val assets: PersonalPropertyBucket = PersonalPropertyBucket(),
    val implants: PersonalPropertyBucket = PersonalPropertyBucket(),
    val marketOrders: PersonalPropertyBucket = PersonalPropertyBucket(),
    val contracts: PersonalPropertyBucket = PersonalPropertyBucket(),
) {
    val totalIsk: Double?
        get() {
            val parts = listOfNotNull(
                walletIsk,
                assets.isk,
                implants.isk,
                marketOrders.isk,
                contracts.isk,
            )
            if (parts.isEmpty()) return null
            return parts.sum()
        }

    fun hasAnyValue(): Boolean =
        walletIsk != null ||
            assets.count != null ||
            implants.count != null ||
            marketOrders.count != null ||
            contracts.count != null

    companion object {
        fun empty(characterId: Long) = PersonalPropertySummary(characterId = characterId)
    }
}

internal enum class PersonalPropertyCategory(val routeValue: String) {
    ASSETS("assets"),
    IMPLANTS("implants"),
    MARKET_ORDERS("orders"),
    CONTRACTS("contracts"),
    ;

    companion object {
        fun fromRouteValue(value: String): PersonalPropertyCategory? =
            entries.firstOrNull { it.routeValue == value }
    }
}

internal data class PersonalPropertyRankedItem(
    val typeId: Int,
    val quantity: Long,
    val unitPrice: Double?,
) {
    val totalIsk: Double
        get() = quantity.toDouble() * (unitPrice ?: 0.0)
}

internal data class PersonalPropertyRanking(
    val priced: List<PersonalPropertyRankedItem>,
    val unpriced: List<PersonalPropertyRankedItem>,
) {
    companion object {
        val EMPTY = PersonalPropertyRanking(priced = emptyList(), unpriced = emptyList())
    }
}

internal data class PersonalPropertyDecoratedItem(
    val typeId: Int,
    val quantity: Long,
    val unitPrice: Double?,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val iconFilename: String?,
) : LocalizableName {
    val totalIsk: Double
        get() = quantity.toDouble() * (unitPrice ?: 0.0)
}

internal data class PersonalPropertyDecoratedRanking(
    val priced: List<PersonalPropertyDecoratedItem>,
    val unpriced: List<PersonalPropertyDecoratedItem>,
)

internal data class PersonalPropertySnapshot(
    val summary: PersonalPropertySummary,
    val assetsRanking: PersonalPropertyRanking,
    val implantsRanking: PersonalPropertyRanking,
    val marketOrdersRanking: PersonalPropertyRanking,
    val contractsRanking: PersonalPropertyRanking,
) {
    fun rankingFor(category: PersonalPropertyCategory): PersonalPropertyRanking = when (category) {
        PersonalPropertyCategory.ASSETS -> assetsRanking
        PersonalPropertyCategory.IMPLANTS -> implantsRanking
        PersonalPropertyCategory.MARKET_ORDERS -> marketOrdersRanking
        PersonalPropertyCategory.CONTRACTS -> contractsRanking
    }
}
