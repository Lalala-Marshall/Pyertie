package com.marshall.pyerite.loyaltyPointsModule.model

import com.marshall.pyerite.localization.LocalizableName

data class LoyaltyCorporationBalance(
    val corporationId: Long,
    val loyaltyPoints: Long,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val iconFilename: String?,
    val iconUrl: String?,
    val isMilitia: Boolean,
) : LocalizableName

data class LoyaltyFactionItem(
    val factionId: Int,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val iconFilename: String?,
) : LocalizableName

data class LoyaltyCorporationItem(
    val corporationId: Long,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val iconFilename: String?,
    val isMilitia: Boolean,
) : LocalizableName

data class LoyaltyOfferCategoryItem(
    val categoryId: Int,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val iconFilename: String?,
    val offerCount: Int,
) : LocalizableName

data class LoyaltyOfferItem(
    val offerId: Int,
    val typeId: Int,
    val quantity: Int,
    val lpCost: Long,
    val iskCost: Long,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val iconFilename: String?,
    val requiredItems: List<LoyaltyOfferRequiredItem>,
    val requiredItemsIskValue: Double?,
) : LocalizableName

data class LoyaltyOfferRequiredItem(
    val typeId: Int,
    val quantity: Int,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val iconFilename: String?,
) : LocalizableName

data class LoyaltyStationItem(
    val stationId: Long,
    val stationName: String?,
    val security: Double?,
    val iconFilename: String?,
)

data class LoyaltyStationRegionGroup(
    val regionId: Int,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val stations: List<LoyaltyStationItem>,
) : LocalizableName
