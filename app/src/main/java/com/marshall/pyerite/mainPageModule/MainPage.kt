package com.marshall.pyerite.mainPageModule

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.data.sde.SdeUpdateUiState
import com.marshall.pyerite.data.sde.SdeUpdateViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumn
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainPage(
    navController: NavController,
    sdeUpdateViewModel: SdeUpdateViewModel = koinViewModel(),
) {
    val uiState by sdeUpdateViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        sdeUpdateViewModel.checkForUpdates()
    }

    val showUpdateBadge = uiState is SdeUpdateUiState.UpdateAvailable
    val showDownloadDialog = uiState is SdeUpdateUiState.Downloading ||
        uiState is SdeUpdateUiState.Completed ||
        uiState is SdeUpdateUiState.Failed

    BaseContainer(
        title = stringResource(R.string.data),
        titleTrailingContent = {
            BadgedBox(
                badge = {
                    if (showUpdateBadge) {
                        Badge()
                    }
                },
            ) {
                IconButton(
                    onClick = {
                        when (uiState) {
                            is SdeUpdateUiState.UpdateAvailable -> sdeUpdateViewModel.downloadUpdate()
                            is SdeUpdateUiState.Failed -> sdeUpdateViewModel.downloadUpdate()
                            else -> sdeUpdateViewModel.checkForUpdates()
                        }
                    },
                    enabled = uiState !is SdeUpdateUiState.Downloading && uiState !is SdeUpdateUiState.Checking,
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = stringResource(R.string.sde_update_download),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
        content = {
            BaseLazyColumn(
                items = listOf(
                    BaseLazyColumnItemModel(
                        iconRes = R.drawable.ic_database,
                        itemName = stringResource(R.string.database),
                        onClick = {
                            navController.navigate(DatabaseRoute.Root.route)
                        },
                    ),
                ),
            )
        },
    )

    if (showDownloadDialog) {
        SdeUpdateDialog(
            uiState = uiState,
            onDismissCompleted = sdeUpdateViewModel::dismissCompleted,
            onDismissError = sdeUpdateViewModel::dismissError,
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
