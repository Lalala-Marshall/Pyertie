package com.marshall.pyerite.sdeModule.room.loyalty

import com.marshall.pyerite.localization.LocalizableName

data class LoyaltyFactionRow(
    val factionId: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
) : LocalizableName

data class LoyaltyCorporationRow(
    val corporationId: Long,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
    val militiaFactionId: Int?,
) : LocalizableName

data class LoyaltyOfferCategoryCountRow(
    val categoryId: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
    val offerCount: Int,
) : LocalizableName

data class LoyaltyOfferRow(
    val offerId: Int,
    val typeId: Int,
    val quantity: Int,
    val lpCost: Long,
    val iskCost: Long,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
) : LocalizableName

data class LoyaltyOfferRequirementRow(
    val offerId: Int,
    val typeId: Int,
    val quantity: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
) : LocalizableName

data class LoyaltyStationRow(
    val stationId: Long,
    val stationName: String?,
    val security: Double?,
    val regionId: Int?,
    val regionName: String?,
    val regionZhName: String?,
    val regionEnName: String?,
    val iconFilename: String?,
)
