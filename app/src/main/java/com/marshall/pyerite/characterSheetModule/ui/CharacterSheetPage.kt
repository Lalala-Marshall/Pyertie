package com.marshall.pyerite.characterSheetModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marshall.pyerite.R
import com.marshall.pyerite.esiModule.model.EsiDateTimeConfig
import com.marshall.pyerite.eveAuthModule.sso.EveSsoConfig
import com.marshall.pyerite.characterSheetModule.model.CharacterMedal
import com.marshall.pyerite.characterSheetModule.model.CharacterSheet
import com.marshall.pyerite.characterSheetModule.model.CharacterSheetLocation
import com.marshall.pyerite.characterSheetModule.viewModel.CharacterSheetViewModel
import com.marshall.pyerite.iconModule.manager.IconManager
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private object CharacterSheetDisplayConfig {
    const val CHARACTER_SECURITY_FORMAT = "%.2f"
    const val SYSTEM_SECURITY_FORMAT = "%.1f"
    const val LOCATION_SEGMENT_GAP = " "
    const val ONLINE_DOT_SIZE_DP = 8
    const val ONLINE_DOT_GAP_DP = 6
    const val TYPE_ICON_SIZE_DP = 24
    const val TYPE_ICON_CORNER_DP = 4
    const val TYPE_ICON_PADDING_DP = 1
}

@Composable
internal fun CharacterSheetPage(
    navController: NavController,
    viewModel: CharacterSheetViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_sheet)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val sheet = uiState.sheet
    val detailsPending = !uiState.detailsReady
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
    ) { topBarPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding)
                .verticalScroll(scrollState)
                .padding(bottom = bottomPadding),
        ) {
            PageTitle(text = pageTitle)
            if (uiState.loadFailed) {
                CharacterSheetLoadFailedBanner(onRetry = viewModel::refresh)
                Spacer(modifier = Modifier.height(sectionGap))
            }
            CharacterSheetBasicInfoSection(
                sheet = sheet,
                detailsPending = detailsPending,
                placeholder = placeholder,
            )
            Spacer(modifier = Modifier.height(sectionGap))
            CharacterSheetTimersSection(
                sheet = sheet,
                detailsPending = detailsPending,
                placeholder = placeholder,
            )
            if (sheet.medals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterSheetMedalsSection(medals = sheet.medals)
            }
        }
    }
}

@Composable
private fun CharacterSheetLoadFailedBanner(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.character_sheet_load_failed),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.character_sheet_retry))
        }
    }
}

@Composable
private fun CharacterSheetBasicInfoSection(
    sheet: CharacterSheet,
    detailsPending: Boolean,
    placeholder: String,
) {
    BaseContainer(
        title = stringResource(R.string.character_sheet_basic_info),
        useSystemBarsPadding = false,
    ) {
        Column {
            CharacterSheetHeaderRow(sheet = sheet)
            ItemDivider()
            BaseDetailRow(
                model = BaseDetailRowModel(
                    iconRes = R.drawable.ic_character_birthday,
                    label = stringResource(R.string.character_sheet_birthday),
                    value = sheet.birthdayEpochMs?.let { formatBirthdayValue(it) } ?: placeholder,
                ),
                showDivider = true,
            )
            val security = sheet.securityStatus
            CharacterSheetLabeledValueRow(
                iconRes = R.drawable.ic_character_security,
                label = stringResource(R.string.character_sheet_security_status),
                value = security?.let {
                    String.format(
                        Locale.US,
                        CharacterSheetDisplayConfig.CHARACTER_SECURITY_FORMAT,
                        it,
                    )
                } ?: placeholder,
                valueColor = if (security != null) {
                    characterSecurityColor(security)
                } else {
                    colorResource(R.color.hint_text)
                },
                showDivider = true,
            )
            CharacterSheetLocationRow(
                location = sheet.location,
                placeholder = placeholder,
                detailsPending = detailsPending,
            )
            CharacterSheetLabeledValueRow(
                iconRes = R.drawable.ic_character_ship_placeholder,
                iconFileName = sheet.shipIconFilename.takeUnless { detailsPending },
                label = stringResource(R.string.character_sheet_current_ship),
                value = sheet.shipDisplayName?.takeIf { it.isNotBlank() } ?: placeholder,
                showDivider = false,
            )
        }
    }
}

@Composable
private fun CharacterSheetTimersSection(
    sheet: CharacterSheet,
    detailsPending: Boolean,
    placeholder: String,
) {
    val nowMs = remember { System.currentTimeMillis() }
    val fatigueExpire = sheet.jumpFatigueExpireEpochMs
    val hasActiveFatigue = !detailsPending && fatigueExpire != null && fatigueExpire > nowMs
    val fatigueText = when {
        detailsPending -> placeholder
        hasActiveFatigue -> formatDurationDisplay(
            totalSeconds = (fatigueExpire - nowMs) / EveSsoConfig.MILLIS_PER_SECOND,
            includeSeconds = false,
        )
        else -> stringResource(R.string.character_sheet_jump_fatigue_none)
    }
    val fatigueColor = when {
        detailsPending -> colorResource(R.color.hint_text)
        hasActiveFatigue -> colorResource(R.color.hint_text)
        else -> colorResource(R.color.character_status_positive)
    }
    val lastJumpHint = if (detailsPending) {
        ""
    } else {
        sheet.lastJumpEpochMs?.let { lastJumpMs ->
            stringResource(
                R.string.character_sheet_last_jump,
                formatDisplayDateTime(lastJumpMs),
            )
        }.orEmpty()
    }

    BaseContainer(
        title = stringResource(R.string.character_sheet_timers),
        useSystemBarsPadding = false,
    ) {
        CharacterSheetLabeledValueRow(
            iconRes = R.drawable.ic_character_fatigue,
            label = stringResource(R.string.character_sheet_jump_fatigue),
            labelHint = lastJumpHint,
            value = fatigueText,
            valueColor = fatigueColor,
            showDivider = false,
        )
    }
}

@Composable
private fun CharacterSheetMedalsSection(medals: List<CharacterMedal>) {
    BaseContainer(
        title = stringResource(R.string.character_sheet_medals),
        useSystemBarsPadding = false,
    ) {
        Column {
            medals.forEachIndexed { index, medal ->
                CharacterSheetMedalRow(medal = medal)
                if (index != medals.lastIndex) {
                    ItemDivider()
                }
            }
        }
    }
}

@Composable
private fun CharacterSheetHeaderRow(sheet: CharacterSheet) {
    val avatarSize = dimensionResource(R.dimen.character_main_avatar_size)
    val nameSize = dimensionResource(R.dimen.character_main_name_text_size).value.sp
    val orgTextSize = dimensionResource(R.dimen.character_org_text_size).value.sp
    val orgLineHeight = dimensionResource(R.dimen.character_org_icon_size).value.sp
    val onlineDotSize = CharacterSheetDisplayConfig.ONLINE_DOT_SIZE_DP.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CharacterSheetAvatar(
            portraitUrl = sheet.portraitUrl,
            size = avatarSize,
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.character_main_avatar_gap)))
        Column(
            modifier = Modifier
                .weight(1f)
                .height(avatarSize),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sheet.name,
                    color = colorResource(R.color.text_primary),
                    fontWeight = FontWeight.Bold,
                    fontSize = nameSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                sheet.isOnline?.let { online ->
                    Spacer(modifier = Modifier.width(CharacterSheetDisplayConfig.ONLINE_DOT_GAP_DP.dp))
                    Box(
                        modifier = Modifier
                            .size(onlineDotSize)
                            .clip(CircleShape)
                            .background(
                                colorResource(
                                    if (online) {
                                        R.color.character_status_online
                                    } else {
                                        R.color.character_status_offline
                                    },
                                ),
                            ),
                    )
                }
            }
            CharacterSheetOrgLine(
                iconUrl = sheet.corporationIconUrl,
                label = sheet.corporationName,
                textSize = orgTextSize,
                lineHeight = orgLineHeight,
            )
            CharacterSheetOrgLine(
                iconUrl = sheet.allianceIconUrl,
                label = sheet.allianceName,
                textSize = orgTextSize,
                lineHeight = orgLineHeight,
            )
        }
    }
}

@Composable
private fun CharacterSheetOrgLine(
    iconUrl: String?,
    label: String?,
    textSize: TextUnit,
    lineHeight: TextUnit,
) {
    val hasLabel = !label.isNullOrBlank()
    if (!hasLabel && iconUrl.isNullOrBlank()) {
        Text(
            text = stringResource(R.string.character_org_none_placeholder),
            color = colorResource(R.color.hint_text),
            fontSize = textSize,
            lineHeight = lineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = compactTextStyle(lineHeight),
        )
        return
    }
    val iconSize = dimensionResource(R.dimen.character_org_icon_size)
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(2.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.character_org_icon_gap)))
        }
        Text(
            text = if (hasLabel) {
                label
            } else {
                stringResource(R.string.character_org_name_placeholder)
            },
            color = colorResource(R.color.text_primary),
            fontSize = textSize,
            lineHeight = lineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = compactTextStyle(lineHeight),
        )
    }
}

@Composable
private fun CharacterSheetLocationRow(
    location: CharacterSheetLocation?,
    placeholder: String,
    detailsPending: Boolean,
) {
    if (location == null) {
        CharacterSheetLabeledValueRow(
            iconRes = R.drawable.ic_character_location,
            label = stringResource(R.string.character_sheet_current_location),
            value = placeholder,
            showDivider = true,
        )
        return
    }

    val securityColor = systemSecurityColor(location.systemSecurityStatus)
    val placeCore = location.placeName?.takeIf { it.isNotBlank() }?.let { place ->
        stringResource(
            R.string.character_sheet_location_with_place,
            location.systemName,
            place,
        )
    } ?: location.systemName

    CharacterSheetLabeledValueRow(
        iconRes = R.drawable.ic_character_location,
        iconFileName = location.placeIconFilename.takeUnless { detailsPending },
        label = stringResource(R.string.character_sheet_current_location),
        valueAnnotated = buildAnnotatedString {
            withStyle(SpanStyle(color = securityColor)) {
                append(
                    String.format(
                        Locale.US,
                        CharacterSheetDisplayConfig.SYSTEM_SECURITY_FORMAT,
                        location.systemSecurityStatus,
                    ),
                )
            }
            append(CharacterSheetDisplayConfig.LOCATION_SEGMENT_GAP)
            withStyle(SpanStyle(color = colorResource(R.color.text_primary))) {
                append(placeCore)
            }
        },
        showDivider = true,
    )
}

@Composable
private fun CharacterSheetMedalRow(medal: CharacterMedal) {
    val dateText = medal.dateEpochMs?.let { formatDisplayDate(it) }.orEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.ic_character_medal),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (dateText.isNotEmpty()) {
                Text(
                    text = dateText,
                    color = colorResource(R.color.text_caption),
                    fontSize = 12.sp,
                )
            }
            Text(
                text = medal.title,
                color = colorResource(R.color.text_primary),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            if (medal.description.isNotBlank()) {
                Text(
                    text = medal.description,
                    color = colorResource(R.color.text_caption),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun CharacterSheetLabeledValueRow(
    iconRes: Int,
    label: String,
    value: String = "",
    valueAnnotated: AnnotatedString? = null,
    labelHint: String = "",
    valueColor: Color = colorResource(R.color.hint_text),
    iconFileName: String? = null,
    showDivider: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterSheetLeadingIcon(
                iconFileName = iconFileName,
                fallbackRes = iconRes,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = colorResource(R.color.text_primary),
                    fontSize = 16.sp,
                )
                if (labelHint.isNotEmpty()) {
                    Text(
                        text = labelHint,
                        color = colorResource(R.color.text_caption),
                        fontSize = 12.sp,
                    )
                }
            }
            if (valueAnnotated != null) {
                Text(text = valueAnnotated, fontSize = 14.sp)
            } else if (value.isNotEmpty()) {
                Text(text = value, color = valueColor, fontSize = 14.sp)
            }
        }
        if (showDivider) ItemDivider()
    }
}

/**
 * Loads a type icon from the local SDE icon pack ([IconManager]) by filename
 * resolved from a remote type id. Falls back to [fallbackRes] when missing.
 */
@Composable
private fun CharacterSheetLeadingIcon(
    iconFileName: String?,
    fallbackRes: Int,
    iconManager: IconManager = koinInject(),
) {
    val iconFile = iconFileName?.let { iconManager.getIconFile(it) }
    if (iconFile != null) {
        AsyncImage(
            model = iconFile,
            contentDescription = null,
            modifier = Modifier
                .size(CharacterSheetDisplayConfig.TYPE_ICON_SIZE_DP.dp)
                .clip(RoundedCornerShape(CharacterSheetDisplayConfig.TYPE_ICON_CORNER_DP.dp))
                .background(colorResource(R.color.white))
                .padding(CharacterSheetDisplayConfig.TYPE_ICON_PADDING_DP.dp),
            contentScale = ContentScale.Fit,
            placeholder = painterResource(fallbackRes),
            error = painterResource(fallbackRes),
        )
    } else {
        Icon(
            modifier = Modifier.size(CharacterSheetDisplayConfig.TYPE_ICON_SIZE_DP.dp),
            painter = painterResource(fallbackRes),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}

@Composable
private fun formatBirthdayValue(birthdayEpochMs: Long): String {
    val dateText = formatDisplayDate(birthdayEpochMs)
    val ageSeconds = ((System.currentTimeMillis() - birthdayEpochMs)
        .coerceAtLeast(0L)) / EveSsoConfig.MILLIS_PER_SECOND
    val age = DurationDisplayFormatter.split(ageSeconds, includeSeconds = false)
    return stringResource(
        R.string.character_sheet_birthday_value,
        dateText,
        age.years,
        age.months,
        age.days,
    )
}

@Composable
private fun characterSecurityColor(security: Double): Color =
    if (security <= 0.0) {
        colorResource(R.color.character_security_negative)
    } else {
        colorResource(R.color.character_status_positive)
    }

@Composable
private fun systemSecurityColor(security: Double): Color = when {
    security <= 0.0 -> colorResource(R.color.character_security_negative)
    security < 0.5 -> colorResource(R.color.character_security_low)
    else -> colorResource(R.color.character_security_high)
}

private fun formatDisplayDate(epochMs: Long): String =
    formatEpoch(epochMs, EsiDateTimeConfig.DISPLAY_DATE_PATTERN)

private fun formatDisplayDateTime(epochMs: Long): String =
    formatEpoch(epochMs, EsiDateTimeConfig.DISPLAY_DATE_TIME_PATTERN)

private fun formatEpoch(epochMs: Long, pattern: String): String {
    return SimpleDateFormat(pattern, Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }.format(Date(epochMs))
}

private fun compactTextStyle(lineHeight: TextUnit): TextStyle = TextStyle(
    lineHeight = lineHeight,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)
