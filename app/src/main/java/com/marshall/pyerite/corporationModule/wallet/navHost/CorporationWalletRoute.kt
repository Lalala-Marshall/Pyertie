package com.marshall.pyerite.corporationModule.wallet.navHost

sealed class CorporationWalletRoute(val route: String) {
    object Wallets : CorporationWalletRoute("corporation/wallets/{characterId}") {
        fun create(characterId: Long) = "corporation/wallets/$characterId"
    }

    object Division : CorporationWalletRoute(
        "corporation/wallets/{characterId}/division/{division}",
    ) {
        fun create(characterId: Long, division: Int) =
            "corporation/wallets/$characterId/division/$division"
    }

    object JournalDay : CorporationWalletRoute(
        "corporation/wallets/{characterId}/division/{division}/journal/{dayKey}",
    ) {
        fun create(characterId: Long, division: Int, dayKey: String) =
            "corporation/wallets/$characterId/division/$division/journal/$dayKey"
    }

    object TransactionDay : CorporationWalletRoute(
        "corporation/wallets/{characterId}/division/{division}/transactions/{dayKey}",
    ) {
        fun create(characterId: Long, division: Int, dayKey: String) =
            "corporation/wallets/$characterId/division/$division/transactions/$dayKey"
    }
}
