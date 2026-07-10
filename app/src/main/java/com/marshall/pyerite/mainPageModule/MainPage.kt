package com.marshall.pyerite.mainPageModule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.data.sde.SdeUpdateUiState
import com.marshall.pyerite.data.sde.SdeUpdateViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainPage(
    navController: NavController,
    sdeUpdateViewModel: SdeUpdateViewModel = koinViewModel(),
) {
    val uiState by sdeUpdateViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(listState)

    LaunchedEffect(Unit) {
        sdeUpdateViewModel.checkForUpdates()
    }

    val showUpdateIcon = uiState is SdeUpdateUiState.UpdateAvailable ||
        uiState is SdeUpdateUiState.Downloading
    val showDownloadDialog = uiState is SdeUpdateUiState.Downloading ||
        uiState is SdeUpdateUiState.Completed ||
        uiState is SdeUpdateUiState.Failed
    val pageTitle = stringResource(R.string.main_page)
    val dataSectionTitle = stringResource(R.string.data)

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        trailingContent = if (showUpdateIcon) {
            {
                IconButton(
                    onClick = { sdeUpdateViewModel.downloadUpdate() },
                    enabled = uiState is SdeUpdateUiState.UpdateAvailable,
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = stringResource(R.string.sde_update_download),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        } else {
            null
        },
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

    if (showDownloadDialog) {
        SdeUpdateDialog(
            uiState = uiState,
            onDismissCompleted = sdeUpdateViewModel::dismissCompleted,
            onDismissError = sdeUpdateViewModel::dismissError,
        )
    }
}

@Composable
private fun MainPageSectionHeader(title: String) {
    val sectionHeaderTextSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val sectionHeaderBottomPadding = dimensionResource(R.dimen.list_section_header_bottom_padding)

    Text(
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

@Composable
private fun SdeUpdateDialog(
    uiState: SdeUpdateUiState,
    onDismissCompleted: () -> Unit,
    onDismissError: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            when (uiState) {
                is SdeUpdateUiState.Completed -> onDismissCompleted()
                is SdeUpdateUiState.Failed -> onDismissError()
                else -> Unit
            }
        },
        title = {
            Text(
                text = when (uiState) {
                    is SdeUpdateUiState.Downloading -> stringResource(R.string.sde_update_downloading_title)
                    is SdeUpdateUiState.Completed -> stringResource(R.string.sde_update_complete)
                    is SdeUpdateUiState.Failed -> stringResource(R.string.sde_update_failed_title)
                    else -> stringResource(R.string.sde_update_download)
                },
            )
        },
        text = {
            when (uiState) {
                is SdeUpdateUiState.Downloading -> {
                    LinearProgressIndicator(
                        progress = { uiState.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(R.string.sde_update_downloading, uiState.progressPercent),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                is SdeUpdateUiState.Completed -> {
                    Text(text = stringResource(R.string.sde_update_complete_message))
                }
                is SdeUpdateUiState.Failed -> {
                    Text(text = stringResource(R.string.sde_update_error, uiState.message))
                }
                else -> Unit
            }
        },
        confirmButton = {
            when (uiState) {
                is SdeUpdateUiState.Completed -> {
                    TextButton(onClick = onDismissCompleted) {
                        Text(text = stringResource(R.string.sde_update_ok))
                    }
                }
                is SdeUpdateUiState.Failed -> {
                    TextButton(onClick = onDismissError) {
                        Text(text = stringResource(R.string.sde_update_ok))
                    }
                }
                else -> Unit
            }
        },
    )
}
