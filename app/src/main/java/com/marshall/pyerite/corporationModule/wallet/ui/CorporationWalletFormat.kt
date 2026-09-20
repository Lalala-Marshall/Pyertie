package com.marshall.pyerite.corporationModule.wallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletConfig
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletJournalDescription
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletJournalDescriptionKind
import com.marshall.pyerite.corporationModule.wallet.model.CorporationWalletSummaryWindow
import com.marshall.pyerite.util.NumberDisplayFormatter
import kotlin.math.abs

@Composable
internal fun formatWalletIsk(
    value: Double,
    style: NumberDisplayFormatter.Style,
): String {
    val formatted = NumberDisplayFormatter.format(abs(value), style)
    return stringResource(R.string.personal_property_isk_value, formatted)
}

@Composable
internal fun walletSignedColor(signedValue: Double): Color {
    return when {
        signedValue > CorporationWalletConfig.ZERO_ISK ->
            colorResource(R.color.wallet_income)
        signedValue < CorporationWalletConfig.ZERO_ISK ->
            colorResource(R.color.wallet_expense)
        else -> colorResource(R.color.hint_text)
    }
}

@Composable
internal fun walletSummaryWindowLabel(window: CorporationWalletSummaryWindow): String {
    val res = when (window) {
        CorporationWalletSummaryWindow.DAYS_30 -> R.string.corporation_wallet_window_30d
        CorporationWalletSummaryWindow.DAYS_7 -> R.string.corporation_wallet_window_7d
        CorporationWalletSummaryWindow.DAYS_1 -> R.string.corporation_wallet_window_1d
    }
    return stringResource(res)
}

@Composable
internal fun corporationWalletDivisionTitle(division: Int, name: String?): String {
    val trimmed = name?.takeIf { it.isNotBlank() }
    if (trimmed != null) return trimmed
    return if (division == CorporationWalletConfig.MASTER_WALLET_DIVISION) {
        stringResource(R.string.corporation_wallet_division_master)
    } else {
        stringResource(R.string.corporation_wallet_division_numbered, division)
    }
}

@Composable
internal fun journalDescriptionText(
    description: CorporationWalletJournalDescription,
): String {
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val first = description.firstPartyName?.takeIf { it.isNotBlank() } ?: placeholder
    val second = description.secondPartyName?.takeIf { it.isNotBlank() } ?: placeholder
    return when (description.kind) {
        CorporationWalletJournalDescriptionKind.INDUSTRY_PROJECT -> {
            val contextId = description.contextId
            if (contextId != null) {
                stringResource(
                    R.string.corporation_wallet_journal_hint_industry,
                    first,
                    second,
                    contextId.toString(),
                )
            } else {
                stringResource(
                    R.string.corporation_wallet_journal_hint_industry_no_id,
                    first,
                    second,
                )
            }
        }
        CorporationWalletJournalDescriptionKind.FALLBACK ->
            description.fallbackText.ifBlank { placeholder }
    }
}

@Composable
internal fun CorporationWalletStatusBanner(
    permissionDenied: Boolean,
    loadFailed: Boolean,
    onRetry: () -> Unit,
) {
    val messageRes = when {
        permissionDenied -> R.string.corporation_wallet_permission_denied
        loadFailed -> R.string.corporation_wallet_load_failed
        else -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(messageRes),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f),
        )
        if (loadFailed && !permissionDenied) {
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.character_sheet_retry))
            }
        }
    }
}
