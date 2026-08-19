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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSheetModule.model.CharacterMedal
import com.marshall.pyerite.characterSheetModule.model.CharacterSheet
import com.marshall.pyerite.characterSheetModule.model.CharacterSheetLocation
import com.marshall.pyerite.characterSheetModule.viewModel.CharacterSheetViewModel
import com.marshall.pyerite.esiModule.model.EsiDateTimeConfig
import com.marshall.pyerite.eveAuthModule.sso.EveSsoConfig
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.CharacterAvatar
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeriteIconShape
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.LocalOpenEntityProfile
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityKind
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityRef
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private object CharacterSheetDisplayConfig {
    const val CHARACTER_SECURITY_FORMAT = "%.2f"
    const val SYSTEM_SECURITY_FORMAT = "%.1f"
    const val LOCATION_SEGMENT_GAP = " "
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
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refresh,
        ),
    )

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = endActions,
    ) { topBarPadding ->
        PyeritePullToRefreshBox(
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
    val nonePlaceholder = stringResource(R.string.character_org_none_placeholder)
    val avatarSize = dimensionResource(R.dimen.character_main_avatar_size)
    val onlineDotSize = dimensionResource(R.dimen.character_sheet_online_dot_size)
    val security = sheet.securityStatus
    val openEntityProfile = LocalOpenEntityProfile.current

    BaseContainer(
        title = stringResource(R.string.character_sheet_basic_info),
        useSystemBarsPadding = false,
    ) {
        Column {
            val isOnline = sheet.isOnline
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    itemName = sheet.name,
                    itemNameBold = true,
                    itemHints = listOf(
                        BaseLazyColumnItemHint(
                            text = orgHintText(
                                name = sheet.corporationName,
                                nonePlaceholder = nonePlaceholder,
                            ),
                            color = colorResource(
                                if (sheet.corporationId != null) {
                                    R.color.hyperlink_text
                                } else {
                                    R.color.text_primary
                                },
                            ),
                            iconUrl = sheet.corporationIconUrl,
                            onClick = sheet.corporationId?.let { corporationId ->
                                {
                                    openEntityProfile(
                                        UniverseEntityRef(
                                            UniverseEntityKind.CORPORATION,
                                            corporationId,
                                        ),
                                    )
                                }
                            },
                        ),
                        BaseLazyColumnItemHint(
                            text = orgHintText(
                                name = sheet.allianceName,
                                nonePlaceholder = nonePlaceholder,
                            ),
                            color = colorResource(
                                if (sheet.allianceId != null) {
                                    R.color.hyperlink_text
                                } else {
                                    R.color.text_primary
                                },
                            ),
                            iconUrl = sheet.allianceIconUrl,
                            onClick = sheet.allianceId?.let { allianceId ->
                                {
                                    openEntityProfile(
                                        UniverseEntityRef(
                                            UniverseEntityKind.ALLIANCE,
                                            allianceId,
                                        ),
                                    )
                                }
                            },
                        ),
                    ),
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = true,
                leadingContent = { _ ->
                    CharacterAvatar(
                        portraitUrl = sheet.portraitUrl,
                        size = avatarSize,
                        shape = PyeriteIconShape.shape,
                    )
                },
                titleLeadingContent = if (isOnline != null) {
                    {
                        Box(
                            modifier = Modifier
                                .size(onlineDotSize)
                                .clip(CircleShape)
                                .background(
                                    colorResource(
                                        if (isOnline) {
                                            R.color.character_status_online
                                        } else {
                                            R.color.character_status_offline
                                        },
                                    ),
                                ),
                        )
                    }
                } else {
                    null
                },
            )

            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconRes = R.drawable.ic_character_birthday,
                    itemName = stringResource(R.string.character_sheet_birthday),
                    itemHints = listOf(
                        BaseLazyColumnItemHint(
                            text = sheet.birthdayEpochMs?.let { formatBirthdayValue(it) }
                                ?: placeholder,
                        ),
                    ),
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = true,
            )

            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconRes = R.drawable.ic_character_security,
                    itemName = stringResource(R.string.character_sheet_security_status),
                    itemHints = listOf(
                        BaseLazyColumnItemHint(
                            text = security?.let {
                                String.format(
                                    Locale.US,
                                    CharacterSheetDisplayConfig.CHARACTER_SECURITY_FORMAT,
                                    it,
                                )
                            } ?: placeholder,
                            color = if (security != null) {
                                characterSecurityColor(security)
                            } else {
                                colorResource(R.color.hint_text)
                            },
                        ),
                    ),
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = true,
            )

            CharacterSheetLocationItem(
                location = sheet.location,
                placeholder = placeholder,
                detailsPending = detailsPending,
            )

            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconRes = R.drawable.ic_character_ship_placeholder,
                    iconFileName = sheet.shipIconFilename.takeUnless { detailsPending },
                    iconOnLightPlate = true,
                    itemName = stringResource(R.string.character_sheet_current_ship),
                    itemHints = listOf(
                        BaseLazyColumnItemHint(
                            text = sheet.shipDisplayName?.takeIf { it.isNotBlank() }
                                ?: placeholder,
                            color = colorResource(R.color.text_primary),
                        ),
                    ),
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = false,
            )
        }
    }
}

@Composable
private fun CharacterSheetLocationItem(
    location: CharacterSheetLocation?,
    placeholder: String,
    detailsPending: Boolean,
) {
    if (location == null) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_location,
                itemName = stringResource(R.string.character_sheet_current_location),
                itemHints = listOf(BaseLazyColumnItemHint(text = placeholder)),
                showChevron = false,
                onClick = null,
            ),
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

    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_character_location,
            iconFileName = location.placeIconFilename.takeUnless { detailsPending },
            iconOnLightPlate = true,
            itemName = stringResource(R.string.character_sheet_current_location),
            itemHints = listOf(
                BaseLazyColumnItemHint(
                    annotatedText = buildAnnotatedString {
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
                ),
            ),
            showChevron = false,
            onClick = null,
        ),
        showDivider = true,
    )
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
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_fatigue,
                itemName = stringResource(R.string.character_sheet_jump_fatigue),
                itemHints = buildList {
                    if (lastJumpHint.isNotEmpty()) {
                        add(
                            BaseLazyColumnItemHint(
                                text = lastJumpHint,
                                color = colorResource(R.color.text_caption),
                            ),
                        )
                    }
                },
                showChevron = false,
                onClick = null,
            ),
            showDivider = false,
            titleTrailingContent = {
                if (fatigueText.isNotEmpty()) {
                    CharacterSheetStatusTag(
                        text = fatigueText,
                        backgroundColor = fatigueColor,
                    )
                }
            },
        )
    }
}

@Composable
private fun CharacterSheetMedalsSection(medals: List<CharacterMedal>) {
    val captionColor = colorResource(R.color.text_caption)
    BaseContainer(
        title = stringResource(R.string.character_sheet_medals),
        useSystemBarsPadding = false,
    ) {
        Column {
            medals.forEachIndexed { index, medal ->
                val dateText = medal.dateEpochMs?.let { formatDisplayDate(it) }.orEmpty()
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        iconRes = R.drawable.ic_character_medal,
                        itemName = medal.title,
                        itemNameBold = true,
                        itemHints = buildList {
                            if (dateText.isNotEmpty()) {
                                add(
                                    BaseLazyColumnItemHint(
                                        text = dateText,
                                        color = captionColor,
                                    ),
                                )
                            }
                            if (medal.description.isNotBlank()) {
                                add(
                                    BaseLazyColumnItemHint(
                                        text = medal.description,
                                        color = captionColor,
                                    ),
                                )
                            }
                        },
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = index != medals.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun CharacterSheetStatusTag(
    text: String,
    backgroundColor: Color,
) {
    Text(
        text = text,
        color = colorResource(R.color.white),
        fontSize = dimensionResource(R.dimen.character_sheet_status_tag_text_size).value.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    dimensionResource(R.dimen.character_sheet_status_tag_corner),
                ),
            )
            .background(backgroundColor)
            .padding(
                horizontal = dimensionResource(
                    R.dimen.character_sheet_status_tag_horizontal_padding,
                ),
                vertical = dimensionResource(
                    R.dimen.character_sheet_status_tag_vertical_padding,
                ),
            ),
    )
}

private fun orgHintText(
    name: String?,
    nonePlaceholder: String,
): String = name?.takeIf { it.isNotBlank() } ?: nonePlaceholder

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
