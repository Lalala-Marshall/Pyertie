package com.marshall.pyerite.loyaltyPointsModule.navHost

import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyPointsNavArgs

sealed class LoyaltyPointsRoute(val route: String) {

    object Hub : LoyaltyPointsRoute(
        "character/loyalty/{${LoyaltyPointsNavArgs.CHARACTER_ID}}",
    ) {
        fun create(characterId: Long) = "character/loyalty/$characterId"
    }

    object Store : LoyaltyPointsRoute(
        "character/loyalty/{${LoyaltyPointsNavArgs.CHARACTER_ID}}/store",
    ) {
        fun create(characterId: Long) = "character/loyalty/$characterId/store"
    }

    object FactionCorps : LoyaltyPointsRoute(
        "character/loyalty/{${LoyaltyPointsNavArgs.CHARACTER_ID}}/store/faction/{${LoyaltyPointsNavArgs.FACTION_ID}}",
    ) {
        fun create(characterId: Long, factionId: Int) =
            "character/loyalty/$characterId/store/faction/$factionId"
    }

    object CorpDetail : LoyaltyPointsRoute(
        "character/loyalty/{${LoyaltyPointsNavArgs.CHARACTER_ID}}/corp/{${LoyaltyPointsNavArgs.CORPORATION_ID}}",
    ) {
        fun create(characterId: Long, corporationId: Long) =
            "character/loyalty/$characterId/corp/$corporationId"
    }

    object Stations : LoyaltyPointsRoute(
        "character/loyalty/{${LoyaltyPointsNavArgs.CHARACTER_ID}}/corp/{${LoyaltyPointsNavArgs.CORPORATION_ID}}/stations",
    ) {
        fun create(characterId: Long, corporationId: Long) =
            "character/loyalty/$characterId/corp/$corporationId/stations"
    }

    object Offers : LoyaltyPointsRoute(
        "character/loyalty/{${LoyaltyPointsNavArgs.CHARACTER_ID}}/corp/{${LoyaltyPointsNavArgs.CORPORATION_ID}}/category/{${LoyaltyPointsNavArgs.CATEGORY_ID}}",
    ) {
        fun create(characterId: Long, corporationId: Long, categoryId: Int) =
            "character/loyalty/$characterId/corp/$corporationId/category/$categoryId"
    }
}
