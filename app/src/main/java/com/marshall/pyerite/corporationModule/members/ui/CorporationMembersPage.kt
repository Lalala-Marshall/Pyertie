package com.marshall.pyerite.corporationModule.members.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.corporationModule.members.model.CorporationMemberSort
import com.marshall.pyerite.corporationModule.members.model.matchesQuery
import com.marshall.pyerite.corporationModule.members.model.sortedFor
import com.marshall.pyerite.corporationModule.members.navHost.CorporationMembersRoute
import com.marshall.pyerite.corporationModule.members.viewModel.CorporationMembersViewModel
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.LocalOpenEntityProfile
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityKind
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityRef
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarMenuItem
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.search.SearchNoResultsItem
import com.marshall.pyerite.util.NumberDisplayFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CorporationMembersPage(
    navController: NavController,
    viewModel: CorporationMembersViewModel = koinViewModel(),
) {
    CorporationMembersBrowser(
        navController = navController,
        viewModel = viewModel,
        pageTitle = stringResource(R.string.corporation_members),
        showWatchEntry = true,
        showMemberCount = true,
        watchedOnly = false,
        emptyMessageRes = R.string.corporation_members_empty,
    )
}

@Composable
internal fun CorporationMemberWatchPage(
    navController: NavController,
    viewModel: CorporationMembersViewModel = koinViewModel(),
) {
    CorporationMembersBrowser(
        navController = navController,
        viewModel = viewModel,
        pageTitle = stringResource(R.string.corporation_members_watched),
        showWatchEntry = false,
        showMemberCount = false,
        watchedOnly = true,
        emptyMessageRes = R.string.corporation_members_watched_empty,
    )
}

@Composable
private fun CorporationMembersBrowser(
    navController: NavController,
    viewModel: CorporationMembersViewModel,
    pageTitle: String,
    showWatchEntry: Boolean,
    showMemberCount: Boolean,
    watchedOnly: Boolean,
    @StringRes emptyMessageRes: Int,
) {
    val uiState by viewModel.uiState.collectAsState()
    val localeController: LocaleController = koinInject()
    val listState = rememberLazyListState()
    val onBack = navController.rememberNavigateUpAction()
    val searching = uiState.query.isNotBlank()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(listState) || searching
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    DockMembersImeToInsets()
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val sortByName = stringResource(R.string.corporation_members_sort_by_name)
    val sortByShip = stringResource(R.string.corporation_members_sort_by_ship)
    val language = localeController.contentLanguage
    val visibleMembers = remember(
        uiState.members,
        uiState.watchedIds,
        uiState.query,
        uiState.sort,
        language,
        watchedOnly,
    ) {
        val source = if (watchedOnly) {
            uiState.members.filter { it.characterId in uiState.watchedIds }
        } else {
            uiState.members
        }
        source
            .filter { it.matchesQuery(uiState.query, language) }
            .sortedFor(uiState.sort, language)
    }
    val sourceCount = if (watchedOnly) {
        uiState.members.count { it.characterId in uiState.watchedIds }
    } else {
        uiState.members.size
    }
    val refreshAction = pyeritePullRefreshTopBarAction(
        isRefreshing = uiState.isLoading,
        refreshFailed = uiState.loadFailed,
        onRefresh = viewModel::refresh,
    )
    val sortAction = PyeriteTopBarActionItem(
        onClick = {},
        icon = Icons.AutoMirrored.Filled.Sort,
        contentDescription = stringResource(R.string.corporation_members_sort),
        menuItems = listOf(
            PyeriteTopBarMenuItem(
                label = sortByName,
                trailingIcon = if (uiState.sort == CorporationMemberSort.NAME) {
                    Icons.Filled.Check
                } else {
                    null
                },
                onClick = { viewModel.onSortChange(CorporationMemberSort.NAME) },
            ),
            PyeriteTopBarMenuItem(
                label = sortByShip,
                trailingIcon = if (uiState.sort == CorporationMemberSort.SHIP) {
                    Icons.Filled.Check
                } else {
                    null
                },
                showDividerBelow = false,
                onClick = { viewModel.onSortChange(CorporationMemberSort.SHIP) },
            ),
        ),
    )
    val memberCountTitle = stringResource(
        R.string.corporation_members_count,
        NumberDisplayFormatter.format(
            uiState.members.size.toLong(),
            NumberDisplayFormatter.Style.FULL,
        ),
    )
    val rosterReady = !uiState.isLoading && !uiState.permissionDenied && !uiState.loadFailed
    val showCount = showMemberCount && !uiState.permissionDenied &&
        (uiState.members.isNotEmpty() || rosterReady)
    val showEmpty = rosterReady && sourceCount == 0
    val showNoResults = !uiState.permissionDenied &&
        !uiState.isLoading &&
        sourceCount > 0 &&
        visibleMembers.isEmpty()

    LaunchedEffect(searching) {
        if (searching) {
            listState.scrollToItem(0)
        }
    }

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = listOfNotNull(refreshAction, sortAction),
    ) { topBarPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding)
                .membersBottomInset(),
        ) {
            PyeritePullToRefreshBox(
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (!searching) {
                        item(key = "page_title") {
                            PageTitle(text = pageTitle)
                        }
                    }
                    if (uiState.permissionDenied || uiState.loadFailed) {
                        item(key = "status") {
                            CorporationMembersStatusBanner(
                                permissionDenied = uiState.permissionDenied,
                                loadFailed = uiState.loadFailed,
                                onRetry = viewModel::refresh,
                            )
                        }
                    }
                    if (showWatchEntry && !searching) {
                        item(key = "watched_entry") {
                            CorporationMembersWatchEntry(
                                onClick = {
                                    navController.navigate(
                                        CorporationMembersRoute.Watched.create(viewModel.characterId),
                                    )
                                },
                            )
                        }
                    }
                    if (showCount && !searching) {
                        item(key = "member_count") {
                            CorporationMembersSectionTitle(text = memberCountTitle)
                        }
                    }
                    if (showEmpty) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(emptyMessageRes),
                                color = colorResource(R.color.text_primary),
                                fontSize = dimensionResource(R.dimen.type_detail_body_text_size).value.sp,
                                modifier = Modifier.padding(
                                    start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
                                    end = dimensionResource(R.dimen.detail_card_horizontal_padding),
                                    top = dimensionResource(R.dimen.type_detail_section_gap),
                                ),
                            )
                        }
                    }
                    if (showNoResults) {
                        item(key = "search_no_results") {
                            SearchNoResultsItem()
                        }
                    }
                    if (!uiState.permissionDenied && visibleMembers.isNotEmpty()) {
                        itemsIndexed(
                            items = visibleMembers,
                            key = { _, member -> member.characterId },
                        ) { index, member ->
                            val openEntityProfile = LocalOpenEntityProfile.current
                            Box(
                                modifier = Modifier.corporationMemberCard(
                                    isFirst = index == 0,
                                    isLast = index == visibleMembers.lastIndex,
                                ),
                            ) {
                                CorporationMemberRow(
                                    member = member,
                                    watched = member.characterId in uiState.watchedIds,
                                    placeholder = placeholder,
                                    showDivider = index < visibleMembers.lastIndex,
                                    localeController = localeController,
                                    onOpen = {
                                        openEntityProfile(
                                            UniverseEntityRef(
                                                UniverseEntityKind.CHARACTER,
                                                member.characterId,
                                            ),
                                        )
                                    },
                                    onToggleWatch = { viewModel.toggleWatch(member.characterId) },
                                )
                            }
                        }
                    }
                }
            }
            CorporationMembersSearchBar(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = {
                    keyboard?.hide()
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.search_bar_vertical_padding)),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Modifier.membersBottomInset(): Modifier =
    windowInsetsPadding(
        WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom),
    )

/**
 * Edge-to-edge already reports the IME as an inset. The default adjust mode also
 * pans the window when the field focuses, so the bar lifts twice and a blank band
 * appears above the keyboard.
 */
@Composable
private fun DockMembersImeToInsets() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findHostActivity()?.window
        val previousMode = window?.attributes?.softInputMode
        if (window != null && previousMode != null) {
            val dockedMode = previousMode and
                WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST.inv() or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            window.setSoftInputMode(dockedMode)
        }
        onDispose {
            if (window != null && previousMode != null) {
                window.setSoftInputMode(previousMode)
            }
        }
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

@Composable
private fun Modifier.corporationMemberCard(isFirst: Boolean, isLast: Boolean): Modifier {
    val radius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = when {
        isFirst && isLast -> RoundedCornerShape(radius)
        isFirst -> RoundedCornerShape(topStart = radius, topEnd = radius)
        isLast -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
    return this
        .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
        .clip(shape)
        .background(colorResource(R.color.second_background))
}

@Composable
private fun CorporationMembersWatchEntry(
    onClick: () -> Unit,
) {
    BaseContainer(title = null, useSystemBarsPadding = false) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(R.string.corporation_members_watched),
                onClick = onClick,
            ),
            showDivider = false,
        )
    }
}

@Composable
private fun CorporationMembersSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp,
        fontWeight = FontWeight.Black,
        color = colorResource(R.color.text_primary),
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
            end = dimensionResource(R.dimen.detail_card_horizontal_padding),
            top = dimensionResource(R.dimen.type_detail_section_gap),
            bottom = dimensionResource(R.dimen.list_section_header_bottom_padding),
        ),
    )
}

@Composable
private fun CorporationMembersStatusBanner(
    permissionDenied: Boolean,
    loadFailed: Boolean,
    onRetry: () -> Unit,
) {
    val messageRes = when {
        permissionDenied -> R.string.corporation_members_permission_denied
        loadFailed -> R.string.corporation_members_load_failed
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
