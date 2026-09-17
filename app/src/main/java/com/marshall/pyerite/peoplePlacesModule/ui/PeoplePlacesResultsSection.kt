package com.marshall.pyerite.peoplePlacesModule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.marshall.pyerite.R
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesConfig
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesResult
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesSearchStatus
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesStanding
import com.marshall.pyerite.peoplePlacesModule.viewModel.PeoplePlacesUiState
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.LocalOpenEntityProfile
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityKind
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityRef
import java.util.Locale

@Composable
internal fun PeoplePlacesResultsSection(uiState: PeoplePlacesUiState) {
    val truncated = uiState.displayedCount < uiState.totalCount
    val title = if (truncated) {
        stringResource(
            R.string.people_places_results_title_truncated,
            uiState.displayedCount,
            uiState.totalCount,
        )
    } else {
        stringResource(
            R.string.people_places_results_title,
            uiState.displayedCount,
            uiState.totalCount,
        )
    }
    val missing = stringResource(R.string.people_places_missing_affiliation)
    val openEntityProfile = LocalOpenEntityProfile.current
    BaseContainer(
        title = title,
        useSystemBarsPadding = false,
    ) {
        when (uiState.searchStatus) {
            PeoplePlacesSearchStatus.QUERY_TOO_SHORT -> {
                PeoplePlacesSearchStatusItem(
                    text = stringResource(
                        R.string.people_places_search_min_length,
                        PeoplePlacesConfig.SEARCH_MIN_LENGTH,
                    ),
                )
            }
            PeoplePlacesSearchStatus.FAILED -> {
                PeoplePlacesSearchStatusItem(
                    text = stringResource(R.string.people_places_search_failed),
                )
            }
            PeoplePlacesSearchStatus.SEARCHING -> if (uiState.results.isEmpty()) {
                PeoplePlacesSearchStatusItem(
                    text = stringResource(R.string.people_places_searching),
                )
            } else {
                PeoplePlacesResultList(
                    results = uiState.results,
                    missingAffiliation = missing,
                    onOpenEntity = openEntityProfile,
                )
            }
            PeoplePlacesSearchStatus.IDLE,
            PeoplePlacesSearchStatus.RESULTS,
            -> if (uiState.results.isEmpty()) {
                PeoplePlacesSearchStatusItem(
                    text = stringResource(R.string.search_no_results),
                )
            } else {
                PeoplePlacesResultList(
                    results = uiState.results,
                    missingAffiliation = missing,
                    onOpenEntity = openEntityProfile,
                )
            }
        }
    }
}

@Composable
private fun PeoplePlacesResultList(
    results: List<PeoplePlacesResult>,
    missingAffiliation: String,
    onOpenEntity: (UniverseEntityRef) -> Unit,
) {
    results.forEachIndexed { index, result ->
        PeoplePlacesResultRow(
            result = result,
            missingAffiliation = missingAffiliation,
            showDivider = index != results.lastIndex,
            onOpenEntity = onOpenEntity,
        )
    }
}

@Composable
private fun PeoplePlacesSearchStatusItem(text: String) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            itemName = text,
            itemNameColor = colorResource(R.color.hint_text),
            showChevron = false,
            onClick = null,
        ),
        showDivider = false,
    )
}

@Composable
private fun PeoplePlacesResultRow(
    result: PeoplePlacesResult,
    missingAffiliation: String,
    showDivider: Boolean,
    onOpenEntity: (UniverseEntityRef) -> Unit,
) {
    when (result) {
        is PeoplePlacesResult.Character -> {
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconUrl = result.portraitUrl,
                    itemName = result.name,
                    alignHintLeadingColumn = false,
                    itemHints = listOf(
                        BaseLazyColumnItemHint(
                            text = result.corporationName?.takeIf { it.isNotBlank() }
                                ?: missingAffiliation,
                            iconUrl = result.corporationIconUrl,
                        ),
                        BaseLazyColumnItemHint(
                            text = result.allianceName?.takeIf { it.isNotBlank() }
                                ?: missingAffiliation,
                            iconUrl = result.allianceIconUrl,
                        ),
                    ),
                    showChevron = true,
                    onClick = {
                        onOpenEntity(
                            UniverseEntityRef(UniverseEntityKind.CHARACTER, result.id),
                        )
                    },
                ),
                showDivider = showDivider,
                trailingContent = { StandingIcon(result.standing) },
            )
        }
        is PeoplePlacesResult.Corporation -> {
            val allianceName = result.allianceName?.takeIf { it.isNotBlank() }
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconUrl = result.logoUrl,
                    itemName = result.name,
                    alignHintLeadingColumn = false,
                    itemHints = if (allianceName == null) {
                        emptyList()
                    } else {
                        listOf(
                            BaseLazyColumnItemHint(
                                text = allianceName,
                                iconUrl = result.allianceIconUrl,
                            ),
                        )
                    },
                    showChevron = true,
                    onClick = {
                        onOpenEntity(
                            UniverseEntityRef(UniverseEntityKind.CORPORATION, result.id),
                        )
                    },
                ),
                showDivider = showDivider,
                trailingContent = { StandingIcon(result.standing) },
            )
        }
        is PeoplePlacesResult.Alliance -> {
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconUrl = result.logoUrl,
                    itemName = result.name,
                    showChevron = true,
                    onClick = {
                        onOpenEntity(
                            UniverseEntityRef(UniverseEntityKind.ALLIANCE, result.id),
                        )
                    },
                ),
                showDivider = showDivider,
                trailingContent = { StandingIcon(result.standing) },
            )
        }
        is PeoplePlacesResult.Building -> {
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconRes = R.drawable.ic_character_station,
                    iconFileName = result.iconFileName,
                    iconOnLightPlate = true,
                    itemName = result.name,
                    itemHints = listOfNotNull(buildingLocationHint(result)),
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = showDivider,
            )
        }
    }
}

@Composable
private fun StandingIcon(standing: PeoplePlacesStanding?) {
    standing ?: return
    val size = dimensionResource(R.dimen.people_places_standing_icon_size)
    Image(
        painter = painterResource(standing.iconRes()),
        contentDescription = null,
        modifier = Modifier.size(size),
    )
}

@Composable
private fun buildingLocationHint(result: PeoplePlacesResult.Building): BaseLazyColumnItemHint? {
    val security = result.securityStatus
    val systemName = result.systemName?.takeIf { it.isNotBlank() }
    val regionName = result.regionName?.takeIf { it.isNotBlank() }
    if (security == null && systemName == null) return null
    val primaryColor = colorResource(R.color.text_primary)
    val hintColor = colorResource(R.color.hint_text)
    val annotated = buildAnnotatedString {
        if (security != null) {
            withStyle(SpanStyle(color = systemSecurityColor(security))) {
                append(
                    String.format(
                        Locale.US,
                        PeoplePlacesConfig.SYSTEM_SECURITY_FORMAT,
                        security,
                    ),
                )
            }
            if (systemName != null) {
                append(PeoplePlacesConfig.LOCATION_SEGMENT_GAP)
            }
        }
        if (systemName != null) {
            val place = if (regionName == null) {
                systemName
            } else {
                systemName + PeoplePlacesConfig.LOCATION_NAME_SEPARATOR + regionName
            }
            withStyle(SpanStyle(color = hintColor)) {
                append(place)
            }
        }
    }
    return BaseLazyColumnItemHint(annotatedText = annotated, color = primaryColor)
}

@Composable
private fun systemSecurityColor(security: Double): Color = when {
    security <= 0.0 -> colorResource(R.color.character_security_negative)
    security < PeoplePlacesConfig.SECURITY_LOW_THRESHOLD ->
        colorResource(R.color.character_security_low)
    else -> colorResource(R.color.character_security_high)
}

private fun PeoplePlacesStanding.iconRes(): Int = when (this) {
    PeoplePlacesStanding.SAME_CORPORATION -> R.drawable.ic_standing_same_corp
    PeoplePlacesStanding.SAME_ALLIANCE -> R.drawable.ic_standing_same_alliance
    PeoplePlacesStanding.PLUS_10 -> R.drawable.ic_standing_plus_10
    PeoplePlacesStanding.PLUS_5 -> R.drawable.ic_standing_plus_5
    PeoplePlacesStanding.NEUTRAL -> R.drawable.ic_standing_neutral
    PeoplePlacesStanding.MINUS_5 -> R.drawable.ic_standing_minus_5
    PeoplePlacesStanding.MINUS_10 -> R.drawable.ic_standing_minus_10
}
