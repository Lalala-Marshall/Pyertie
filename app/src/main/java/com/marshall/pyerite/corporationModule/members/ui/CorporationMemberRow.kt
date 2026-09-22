package com.marshall.pyerite.corporationModule.members.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.marshall.pyerite.R
import com.marshall.pyerite.corporationModule.members.model.CorporationMember
import com.marshall.pyerite.corporationModule.members.model.CorporationMembersConfig
import com.marshall.pyerite.corporationModule.members.model.shipDisplayName
import com.marshall.pyerite.corporationModule.members.model.systemDisplayName
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import java.util.Locale

@Composable
internal fun CorporationMemberRow(
    member: CorporationMember,
    watched: Boolean,
    placeholder: String,
    showDivider: Boolean,
    localeController: LocaleController,
    onOpen: () -> Unit,
    onToggleWatch: () -> Unit,
) {
    val language = localeController.contentLanguage
    val hintColor = colorResource(R.color.hint_text)
    val shipLabel = member.shipDisplayName(language).ifBlank { placeholder }
    val systemLabel = member.systemDisplayName(language)
    val security = member.systemSecurityStatus
    val hint = buildAnnotatedString {
        withStyle(SpanStyle(color = hintColor)) {
            append(shipLabel)
            append(CorporationMembersConfig.HINT_SEPARATOR)
        }
        if (security != null) {
            withStyle(SpanStyle(color = systemSecurityColor(security))) {
                append(
                    String.format(
                        Locale.US,
                        CorporationMembersConfig.SYSTEM_SECURITY_FORMAT,
                        security,
                    ),
                )
            }
            withStyle(SpanStyle(color = hintColor)) {
                append(CorporationMembersConfig.LOCATION_SEGMENT_GAP)
                append(systemLabel.ifBlank { placeholder })
            }
        } else {
            withStyle(SpanStyle(color = hintColor)) {
                append(systemLabel.ifBlank { placeholder })
            }
        }
    }
    val markDescription = stringResource(
        if (watched) {
            R.string.corporation_members_unmark
        } else {
            R.string.corporation_members_mark
        },
    )
    val markTint = colorResource(
        if (watched) R.color.hyperlink_text else R.color.hint_text,
    )

    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconUrl = member.portraitUrl,
            itemName = member.name.ifBlank { placeholder },
            itemNameMaxLines = 1,
            alignHintLeadingColumn = false,
            itemHints = listOf(
                BaseLazyColumnItemHint(
                    annotatedText = hint,
                    iconFileName = member.shipIconFilename?.takeIf { it.isNotBlank() },
                ),
            ),
            onClick = onOpen,
        ),
        showDivider = showDivider,
        trailingContent = {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.top_bar_back_button_size))
                    .clickable(onClick = onToggleWatch),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = markDescription,
                    tint = markTint,
                    modifier = Modifier.size(dimensionResource(R.dimen.detail_row_chevron_size)),
                )
            }
        },
    )
}

@Composable
private fun systemSecurityColor(security: Double): Color = when {
    security <= CorporationMembersConfig.SECURITY_NEGATIVE_MAX ->
        colorResource(R.color.character_security_negative)
    security < CorporationMembersConfig.SECURITY_LOW_MAX ->
        colorResource(R.color.character_security_low)
    else -> colorResource(R.color.character_security_high)
}
