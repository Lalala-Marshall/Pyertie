package com.marshall.pyerite.mainPageModule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.data.sde.SdeUpdateBottomSheet
import com.marshall.pyerite.data.sde.SdeUpdateUiState
import com.marshall.pyerite.data.sde.SdeUpdateViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.characterModule.navHost.CharacterRoute
import com.marshall.pyerite.characterModule.ui.MainPageCharacterCard
import com.marshall.pyerite.characterModule.viewModel.CharacterViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainPage(
    navController: NavController,
    sdeUpdateViewModel: SdeUpdateViewModel = koinViewModel(),
    characterViewModel: CharacterViewModel = koinViewModel(),
) {
    val uiState by sdeUpdateViewModel.uiState.collectAsState()
    val currentCharacter by characterViewModel.currentCharacter.collectAsState()
    val listState = rememberLazyListState()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(listState)

    val updateActionLabel = when (uiState) {
        is SdeUpdateUiState.CheckFailed -> stringResource(R.string.sde_update_check_manually)
        else -> stringResource(R.string.sde_update_available)
    }
    val showUpdateIcon = uiState is SdeUpdateUiState.UpdateReady ||
        uiState is SdeUpdateUiState.CheckFailed
    val pageTitle = stringResource(R.string.main_page)
    val dataSectionTitle = stringResource(R.string.data)
    val endActions = buildList {
        if (showUpdateIcon) {
            add(
                PyeriteTopBarActionItem(
                    onClick = { sdeUpdateViewModel.openUpdateSheet() },
                    icon = Icons.Default.ArrowDownward,
                    contentDescription = stringResource(R.string.sde_update_download),
                    label = updateActionLabel,
                    accentColor = colorResource(R.color.sde_update_accent),
                    iconBadge = true,
                ),
            )
        }
        if (currentCharacter != null) {
            add(
                PyeriteTopBarActionItem(
                    onClick = characterViewModel::clearCurrentCharacter,
                    icon = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = stringResource(R.string.character_exit_current),
                    iconTint = colorResource(R.color.character_delete),
                ),
            )
        }
    }

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        endActions = endActions,
    ) { topBarPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding),
        ) {
            item(key = "page_title") {
                PageTitle(text = pageTitle)
            }
            item(key = "current_character") {
                MainPageCharacterCard(
                    currentCharacter = currentCharacter,
                    onClick = { navController.navigate(CharacterRoute.Root.route) },
                )
            }
            item(key = "data_section_header") {
                MainPageSectionHeader(title = dataSectionTitle)
            }
            item(key = "database_entry") {
                MainPageDatabaseItem(
                    onClick = { navController.navigate(DatabaseRoute.Root.route) },
                )
            }
        }
    }

    val sheetState = uiState as? SdeUpdateUiState.Sheet
    if (sheetState != null) {
        key(sheetState.instanceId) {
            SdeUpdateBottomSheet(
                content = sheetState.content,
                onDismiss = sdeUpdateViewModel::dismissSheet,
                onConfirmDownload = sdeUpdateViewModel::startDownload,
                onRetryDownload = sdeUpdateViewModel::retryDownload,
                onRetryCheck = sdeUpdateViewModel::retryCheck,
            )
        }
    }

    LaunchedEffect(Unit) {
        sdeUpdateViewModel.repairStaleCheckingState()
    }
}

@Composable
private fun MainPageSectionHeader(title: String) {
    val sectionHeaderTextSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val sectionHeaderBottomPadding = dimensionResource(R.dimen.list_section_header_bottom_padding)

    androidx.compose.material3.Text(
        text = title,
        fontSize = sectionHeaderTextSize,
        fontWeight = FontWeight.Black,
        color = colorResource(R.color.text_primary),
        modifier = Modifier.padding(
            start = titleStartPadding,
            bottom = sectionHeaderBottomPadding,
        ),
    )
}

@Composable
private fun MainPageDatabaseItem(onClick: () -> Unit) {
    val cardCornerRadius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = RoundedCornerShape(cardCornerRadius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
            .clip(shape)
            .background(colorResource(R.color.second_background), shape),
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_database,
                itemName = stringResource(R.string.database),
                onClick = onClick,
            ),
            showDivider = false,
        )
    }
}
