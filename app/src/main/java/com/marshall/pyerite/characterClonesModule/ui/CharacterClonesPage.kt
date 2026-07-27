package com.marshall.pyerite.characterClonesModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterClonesModule.model.CharacterCloneStatus
import com.marshall.pyerite.characterClonesModule.viewModel.CharacterClonesViewModel
import com.marshall.pyerite.esiModule.model.EsiDateTimeConfig
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun CharacterClonesPage(
    navController: NavController,
    viewModel: CharacterClonesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_clone_status)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
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
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterClonesHomeStationSection(
                    status = uiState.status,
                    detailsPending = !uiState.detailsReady,
                    placeholder = placeholder,
                )
            }
        }
    }
}

@Composable
private fun CharacterClonesHomeStationSection(
    status: CharacterCloneStatus,
    detailsPending: Boolean,
    placeholder: String,
) {
    val captionColor = colorResource(R.color.hint_text)
    val primaryHintColor = colorResource(R.color.text_primary)

    BaseContainer(
        title = stringResource(R.string.character_clone_home_station_section),
        useSystemBarsPadding = false,
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_location,
                iconFileName = status.homeLocationIconFilename.takeUnless { detailsPending },
                iconOnLightPlate = true,
                itemName = stringResource(R.string.character_clone_base_location),
                itemHints = listOf(
                    BaseLazyColumnItemHint(
                        text = when {
                            detailsPending -> placeholder
                            else -> status.homeLocationName?.takeIf { it.isNotBlank() }
                                ?: placeholder
                        },
                        color = primaryHintColor,
                    ),
                ),
                showChevron = false,
                onClick = null,
            ),
            showDivider = true,
        )
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_clones,
                itemName = stringResource(R.string.character_clone_last_clone_jump),
                itemHints = listOf(
                    BaseLazyColumnItemHint(
                        text = when {
                            detailsPending -> placeholder
                            else -> status.lastCloneJumpEpochMs?.let(::formatEveDateTime)
                                ?: placeholder
                        },
                        color = captionColor,
                    ),
                ),
                showChevron = false,
                onClick = null,
            ),
            showDivider = true,
        )
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_station,
                itemName = stringResource(R.string.character_clone_last_station_change),
                itemHints = listOf(
                    BaseLazyColumnItemHint(
                        text = when {
                            detailsPending -> placeholder
                            else -> status.lastStationChangeEpochMs?.let(::formatEveDateTime)
                                ?: placeholder
                        },
                        color = captionColor,
                    ),
                ),
                showChevron = false,
                onClick = null,
            ),
            showDivider = false,
        )
    }
}

/** EVE time is UTC — display without converting to the device timezone. */
private fun formatEveDateTime(epochMs: Long): String {
    return SimpleDateFormat(
        EsiDateTimeConfig.DISPLAY_DATE_TIME_PATTERN,
        Locale.US,
    ).apply {
        timeZone = TimeZone.getTimeZone(EsiDateTimeConfig.TIME_ZONE_UTC)
    }.format(Date(epochMs))
}
