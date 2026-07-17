package com.marshall.pyerite.characterModule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.browser.customtabs.CustomTabsIntent
import android.content.Context
import android.content.Intent
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import com.marshall.pyerite.characterModule.ui.CharacterLoggedInListItem
import com.marshall.pyerite.characterModule.viewModel.CharacterViewModel
import com.marshall.pyerite.characterModule.auth.EveSsoUiStatus
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.topBarActionSurface
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri

@Composable
fun CharacterManagementPage(
    navController: NavController,
    viewModel: CharacterViewModel = koinViewModel(),
) {
    val loggedInCharacters by viewModel.loggedInCharacters.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val ssoStatus by viewModel.ssoStatus.collectAsState()
    val listState = rememberLazyListState()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(listState)
    var pendingDeleteCharacter by remember { mutableStateOf<LoggedInCharacter?>(null) }
    val navigateUp = navController.rememberNavigateUpAction()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.openAuthorizationUrl.collect { url ->
            openEveAuthorizationUrl(context, url)
        }
    }

    val pageTitle = stringResource(R.string.character_management)
    val hasLoggedInCharacters = loggedInCharacters.isNotEmpty()
    val editActionLabel = if (isEditMode) {
        stringResource(R.string.character_done)
    } else {
        stringResource(R.string.character_edit)
    }
    val endActions = if (hasLoggedInCharacters) {
        listOf(
            PyeriteTopBarActionItem(
                onClick = viewModel::toggleEditMode,
                icon = Icons.Default.Check,
                contentDescription = editActionLabel,
                label = editActionLabel,
                accentColor = colorResource(R.color.hyperlink_text),
                showIcon = false,
            ),
        )
    } else {
        emptyList()
    }

    LaunchedEffect(hasLoggedInCharacters, isEditMode) {
        if (!hasLoggedInCharacters && isEditMode) {
            viewModel.toggleEditMode()
        }
    }

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = navigateUp,
        endActions = endActions,
    ) { topBarPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding),
        ) {
            item(key = "page_title") {
                Text(
                    text = pageTitle,
                    fontSize = dimensionResource(R.dimen.list_page_title_text_size).value.sp,
                    fontWeight = FontWeight.Black,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier.padding(
                        start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
                        top = dimensionResource(R.dimen.type_detail_page_title_vertical_padding),
                        bottom = dimensionResource(R.dimen.list_page_title_bottom_padding),
                    ),
                )
            }

            item(key = "add_character") {
                CharacterAddButton(onClick = viewModel::onAddCharacterClicked)
            }

            if (hasLoggedInCharacters) {
                item(key = "logged_in_header") {
                    CharacterSectionHeader(
                        title = stringResource(
                            R.string.character_logged_in_section,
                            loggedInCharacters.size,
                        ),
                    )
                }

                item(key = "logged_in_card") {
                    CharacterLoggedInCard(
                        characters = loggedInCharacters,
                        isEditMode = isEditMode,
                        onCharacterClick = { character ->
                            viewModel.selectCurrentCharacter(character)
                            navigateUp?.invoke()
                        },
                        onDeleteClick = { pendingDeleteCharacter = it },
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.type_detail_bottom_padding)))
            }
        }
    }

    pendingDeleteCharacter?.let { character ->
        Dialog(onDismissRequest = { pendingDeleteCharacter = null }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = colorResource(R.color.main_background),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.character_delete_confirm_title),
                        color = colorResource(R.color.text_primary),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = character.name,
                        color = colorResource(R.color.text_caption),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CharacterDialogActionButton(
                            label = stringResource(R.string.character_delete_cancel),
                            labelColor = colorResource(R.color.text_primary),
                            onClick = { pendingDeleteCharacter = null },
                            modifier = Modifier.weight(1f),
                        )
                        CharacterDialogActionButton(
                            label = stringResource(R.string.character_delete_confirm),
                            labelColor = colorResource(R.color.character_delete),
                            onClick = {
                                viewModel.removeLoggedInCharacter(character.characterId)
                                pendingDeleteCharacter = null
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    when (val status = ssoStatus) {
        is EveSsoUiStatus.Idle -> Unit

        is EveSsoUiStatus.AwaitingBrowser -> {
            Dialog(onDismissRequest = viewModel::cancelSsoLogin) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = colorResource(R.color.main_background),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.character_sso_awaiting_browser),
                            color = colorResource(R.color.text_primary),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                        )
                        CharacterDialogActionButton(
                            label = stringResource(R.string.character_sso_cancel),
                            labelColor = colorResource(R.color.hyperlink_text),
                            onClick = viewModel::cancelSsoLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                        )
                    }
                }
            }
        }

        is EveSsoUiStatus.ExchangingToken -> {
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = colorResource(R.color.main_background),
                ) {
                    Text(
                        text = stringResource(R.string.character_sso_signing_in),
                        color = colorResource(R.color.text_primary),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        is EveSsoUiStatus.Failed -> {
            Dialog(onDismissRequest = viewModel::clearSsoStatus) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = colorResource(R.color.main_background),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.character_sso_failed,
                                if (status.formatArgs.isEmpty()) {
                                    stringResource(status.messageRes)
                                } else {
                                    stringResource(
                                        status.messageRes,
                                        *status.formatArgs.toTypedArray(),
                                    )
                                },
                            ),
                            color = colorResource(R.color.text_primary),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                        )
                        CharacterDialogActionButton(
                            label = stringResource(R.string.character_sso_ok),
                            labelColor = colorResource(R.color.hyperlink_text),
                            onClick = viewModel::clearSsoStatus,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                        )
                    }
                }
            }
        }

        is EveSsoUiStatus.Succeeded -> {
            Dialog(onDismissRequest = viewModel::clearSsoStatus) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = colorResource(R.color.main_background),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.character_sso_success,
                                status.characterName,
                            ),
                            color = colorResource(R.color.text_primary),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                        )
                        CharacterDialogActionButton(
                            label = stringResource(R.string.character_sso_ok),
                            labelColor = colorResource(R.color.hyperlink_text),
                            onClick = viewModel::clearSsoStatus,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterDialogActionButton(
    label: String,
    labelColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(percent = 50)
    val buttonHeight = dimensionResource(R.dimen.top_bar_back_button_size)

    Box(
        modifier = modifier
            .height(buttonHeight)
            .topBarActionSurface(pillShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { role = Role.Button }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = labelColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CharacterAddButton(onClick: () -> Unit) {
    val cardCornerRadius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = RoundedCornerShape(cardCornerRadius)
    val innerPadding = dimensionResource(R.dimen.character_card_inner_padding)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
            .padding(bottom = dimensionResource(R.dimen.character_section_spacing))
            .clip(shape)
            .background(colorResource(R.color.second_background), shape)
            .clickable(onClick = onClick)
            .padding(innerPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_character),
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.character_add_icon_size)),
                tint = colorResource(R.color.hyperlink_text),
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.detail_row_icon_gap)))
            Text(
                text = stringResource(R.string.character_add),
                color = colorResource(R.color.hyperlink_text),
                fontWeight = FontWeight.SemiBold,
                fontSize = dimensionResource(R.dimen.character_main_name_text_size).value.sp,
            )
        }
    }
}

@Composable
private fun CharacterSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = dimensionResource(R.dimen.list_section_subheader_text_size).value.sp,
        fontWeight = FontWeight.SemiBold,
        color = colorResource(R.color.text_caption),
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
            end = dimensionResource(R.dimen.detail_card_horizontal_padding),
            bottom = dimensionResource(R.dimen.list_section_header_bottom_padding),
        ),
    )
}

@Composable
private fun CharacterLoggedInCard(
    characters: List<LoggedInCharacter>,
    isEditMode: Boolean,
    onCharacterClick: (LoggedInCharacter) -> Unit,
    onDeleteClick: (LoggedInCharacter) -> Unit,
) {
    val cardCornerRadius = dimensionResource(R.dimen.detail_card_corner_radius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding)),
    ) {
        characters.forEachIndexed { index, character ->
            val shape = sectionItemShape(index, characters.size, cardCornerRadius)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colorResource(R.color.second_background), shape),
            ) {
                CharacterLoggedInListItem(
                    character = character,
                    showDivider = index < characters.lastIndex,
                    isEditMode = isEditMode,
                    onClick = { onCharacterClick(character) },
                    onDeleteClick = { onDeleteClick(character) },
                )
            }
        }
    }
}

private fun sectionItemShape(indexInSection: Int, sectionItemCount: Int, corner: Dp): Shape {
    return when {
        sectionItemCount == 1 -> RoundedCornerShape(corner)
        indexInSection == 0 -> RoundedCornerShape(topStart = corner, topEnd = corner)
        indexInSection == sectionItemCount - 1 -> RoundedCornerShape(bottomStart = corner, bottomEnd = corner)
        else -> RectangleShape
    }
}

private fun openEveAuthorizationUrl(context: Context, url: String) {
    val uri = url.toUri()
    runCatching {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    }.onFailure {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
