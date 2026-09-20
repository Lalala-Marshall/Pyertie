package com.marshall.pyerite.corporationModule.wallet.model

import androidx.annotation.StringRes
import com.marshall.pyerite.R

internal enum class CorporationWalletJournalDirection(
    @param:StringRes val titleRes: Int,
) {
    INCOME(R.string.corporation_wallet_filter_income),
    EXPENSE(R.string.corporation_wallet_filter_expense),
}

internal enum class CorporationWalletJournalFilterType(
    @param:StringRes val titleRes: Int,
    val refTypes: Set<String>,
) {
    MANUFACTURING(
        R.string.corporation_journal_ref_manufacturing,
        setOf("manufacturing", "industry_job"),
    ),
    MARKET_ESCROW(
        R.string.corporation_journal_ref_market_escrow,
        setOf("market_escrow"),
    ),
    REACTION(
        R.string.corporation_journal_ref_reaction,
        setOf("reaction"),
    ),
    MARKET_TRANSACTION(
        R.string.corporation_journal_ref_market_transaction,
        setOf("market_transaction"),
    ),
    TRANSACTION_TAX(
        R.string.corporation_journal_ref_transaction_tax,
        setOf("transaction_tax"),
    ),
    DAILY_GOAL_PAYOUTS(
        R.string.corporation_journal_ref_daily_goal_payouts,
        setOf("daily_goal_payouts", "daily_goal_payout", "daily_goal_reward"),
    ),
    CONTRACT_BROKERS_FEE_CORP(
        R.string.corporation_journal_ref_contract_brokers_fee_corp,
        setOf("contract_brokers_fee_corp"),
    ),
    CONTRACT_PRICE_PAYMENT_CORP(
        R.string.corporation_journal_ref_contract_price_payment_corp,
        setOf("contract_price_payment_corp"),
    ),
    CORPORATION_ACCOUNT_WITHDRAWAL(
        R.string.corporation_journal_ref_corporation_account_withdrawal,
        setOf("corporation_account_withdrawal"),
    ),
    CONTRACT_PRICE(
        R.string.corporation_journal_ref_contract_price,
        setOf("contract_price"),
    ),
    CONTRACT_REWARD_DEPOSITED(
        R.string.corporation_journal_ref_contract_reward_deposited,
        setOf("contract_reward_deposited", "contract_reward_deposited_corp"),
    ),
    PLAYER_DONATION(
        R.string.corporation_journal_ref_player_donation,
        setOf("player_donation"),
    ),
    CONTRACT_REWARD(
        R.string.corporation_journal_ref_contract_reward,
        setOf("contract_reward"),
    ),
    INDUSTRY_JOB_TAX(
        R.string.corporation_journal_ref_industry_job_tax,
        setOf("industry_job_tax"),
    ),
    OFFICE_RENTAL_FEE(
        R.string.corporation_journal_ref_office_rental_fee,
        setOf("office_rental_fee"),
    ),
    BROKERS_FEE(
        R.string.corporation_journal_ref_brokers_fee,
        setOf("brokers_fee"),
    ),
    CONTRACT_REWARD_REFUND(
        R.string.corporation_journal_ref_contract_reward_refund,
        setOf("contract_reward_refund"),
    ),
    CSPA(
        R.string.corporation_journal_ref_cspa,
        setOf("cspa", "cspaofflinerefund"),
    ),
    ;

    companion object {
        private val BY_REF_TYPE: Map<String, CorporationWalletJournalFilterType> =
            entries.flatMap { type -> type.refTypes.map { ref -> ref to type } }.toMap()

        fun forRefType(refType: String): CorporationWalletJournalFilterType? =
            BY_REF_TYPE[refType.trim()]
    }
}

internal data class CorporationWalletJournalFilter(
    val directions: Set<CorporationWalletJournalDirection>,
    val types: Set<CorporationWalletJournalFilterType>,
) {
    val allDirectionsSelected: Boolean =
        directions.containsAll(CorporationWalletJournalDirection.entries)

    val allTypesSelected: Boolean =
        types.containsAll(CorporationWalletJournalFilterType.entries)

    val isDefault: Boolean = allDirectionsSelected && allTypesSelected

    fun matches(entry: CorporationWalletJournalEntry): Boolean {
        val directionMatches = when {
            entry.amount > CorporationWalletConfig.ZERO_ISK ->
                CorporationWalletJournalDirection.INCOME in directions
            entry.amount < CorporationWalletConfig.ZERO_ISK ->
                CorporationWalletJournalDirection.EXPENSE in directions
            else -> directions.isNotEmpty()
        }
        if (!directionMatches) return false
        val type = CorporationWalletJournalFilterType.forRefType(entry.refType)
        return if (type == null) allTypesSelected else type in types
    }

    fun toggleAllDirections(): CorporationWalletJournalFilter = copy(
        directions = if (allDirectionsSelected) {
            emptySet()
        } else {
            CorporationWalletJournalDirection.entries.toSet()
        },
    )

    fun toggleDirection(direction: CorporationWalletJournalDirection): CorporationWalletJournalFilter {
        val next = directions.toMutableSet()
        if (!next.add(direction)) next.remove(direction)
        return copy(directions = next)
    }

    fun toggleAllTypes(): CorporationWalletJournalFilter = copy(
        types = if (allTypesSelected) {
            emptySet()
        } else {
            CorporationWalletJournalFilterType.entries.toSet()
        },
    )

    fun toggleType(type: CorporationWalletJournalFilterType): CorporationWalletJournalFilter {
        val next = types.toMutableSet()
        if (!next.add(type)) next.remove(type)
        return copy(types = next)
    }

    companion object {
        val ALL = CorporationWalletJournalFilter(
            directions = CorporationWalletJournalDirection.entries.toSet(),
            types = CorporationWalletJournalFilterType.entries.toSet(),
        )
    }
}
