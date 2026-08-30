package com.marshall.pyerite.personalPropertyModule.model

internal data class PersonalPropertyBucket(
    val count: Int? = null,
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
