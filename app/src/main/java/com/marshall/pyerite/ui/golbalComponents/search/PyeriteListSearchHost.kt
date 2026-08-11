package com.marshall.pyerite.ui.golbalComponents.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBar
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListSearchPinned
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.rememberTopBarTotalHeight

/**
 * List page shell with pinned search chrome (idle bar, active field, dismiss scrim).
 * Feature pages supply [searchState] and callbacks; content is built via [listContent].
 */
@Composable
fun PyeriteListSearchHost(
    searchState: ListSearchState,
    onActivateSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCancelSearch: () -> Unit,
    listState: LazyListState,
    navTitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    endActions: List<PyeriteTopBarActionItem> = emptyList(),
    title: @Composable () -> Unit,
    listContent: LazyListScope.(query: String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchRowHeight = rememberSearchRowHeight()
    val topBarTotalHeight = rememberTopBarTotalHeight()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(
        listState = listState,
        enabled = !searchState.isActive,
    )
    val isSearchPinned = rememberLazyListSearchPinned(
        listState = listState,
        enabled = !searchState.isActive,
    )

    val showDismissScrim = searchState.isActive && searchState.query.isBlank()

    val cancelSearch: () -> Unit = {
        onCancelSearch()
        keyboardController?.hide()
    }

    LaunchedEffect(searchState.isActive) {
        if (searchState.isActive) {
            focusRequester.requestFocus()
        }
    }

    BackHandler(enabled = searchState.isActive) {
        cancelSearch()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = topBarTotalHeight),
                userScrollEnabled = !showDismissScrim,
            ) {
                item(key = "page_title") {
                    AnimatedVisibility(
                        visible = !searchState.isActive,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        title()
                    }
                }

                item(key = "search_bar") {
                    if (searchState.isActive || isSearchPinned) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(searchRowHeight),
                        )
                    } else {
                        PyeriteSearchIdleBar(onActivate = onActivateSearch)
                    }
                }

                listContent(searchState.query)
            }
        }

        val showTopBarScrim = showCollapsedTitle || searchState.isActive
        val showTopBarTitle = showCollapsedTitle || searchState.isActive
        val topBarOnBack = if (searchState.isActive) cancelSearch else onBack

        PyeriteTopBar(
            title = navTitle,
            showTitle = showTopBarTitle,
            showScrim = showTopBarScrim,
            onBack = topBarOnBack,
            endActions = if (searchState.isActive) emptyList() else endActions,
            pinnedSearch = when {
                searchState.isActive -> {
                    {
                        PyeriteSearchActiveBar(
                            query = searchState.query,
                            onQueryChange = onQueryChange,
                            onClearQuery = { onQueryChange("") },
                            focusRequester = focusRequester,
                        )
                    }
                }
                isSearchPinned -> {
                    {
                        PyeriteSearchIdleBar(
                            onActivate = onActivateSearch,
                            transparentContainer = true,
                        )
                    }
                }
                else -> null
            },
            pinnedSearchHeight = searchRowHeight,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(20f),
        )

        if (showDismissScrim) {
            ListSearchDismissScrim(
                topOffset = topBarTotalHeight + searchRowHeight,
                onDismiss = cancelSearch,
            )
        }
    }
}

@Composable
private fun ListSearchDismissScrim(
    topOffset: Dp,
    onDismiss: () -> Unit,
) {
    val scrimColor = colorResource(R.color.search_scrim)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(15f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topOffset)
                .background(scrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = true,
                    onClick = onDismiss,
                ),
        )
    }
}
