package com.marshall.pyerite.databaseHierarchyModule.search

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel

@Composable
fun DatabaseListSearchHost(
    pageKey: String,
    viewModel: DatabaseViewModel,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    listContent: LazyListScope.(query: String) -> Unit,
) {
    val searchState by remember(pageKey) { viewModel.searchState(pageKey) }.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchRowHeight = rememberSearchRowHeight()

    val showScrim = searchState.isActive && searchState.query.isBlank()

    val cancelSearch: () -> Unit = {
        viewModel.cancelSearch(pageKey)
        keyboardController?.hide()
    }

    val activateSearch: () -> Unit = {
        viewModel.setSearchActive(pageKey, true)
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
        Box(modifier = modifier.fillMaxSize().imePadding()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f),
                userScrollEnabled = !showScrim,
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

                stickyHeader(key = "search_bar") {
                    if (searchState.isActive) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(searchRowHeight),
                        )
                    } else {
                    DatabaseSearchIdleBar(
                        onActivate = activateSearch,
                    )
                    }
                }

                listContent(searchState.query)
            }
        }

        if (searchState.isActive) {
            DatabaseSearchActiveOverlay(
                query = searchState.query,
                showScrim = showScrim,
                onQueryChange = { viewModel.setSearchQuery(pageKey, it) },
                onClearQuery = { viewModel.setSearchQuery(pageKey, "") },
                onDismissScrim = cancelSearch,
                focusRequester = focusRequester,
            )
        }
    }
}

@Composable
private fun DatabaseSearchActiveOverlay(
    query: String,
    showScrim: Boolean,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onDismissScrim: () -> Unit,
    focusRequester: FocusRequester,
) {
    val scrimColor = colorResource(R.color.search_scrim)

    BoxWithConstraints(
        modifier = Modifier
            .then(if (showScrim) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
            .zIndex(10f),
    ) {
        if (showScrim) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = true,
                        onClick = onDismissScrim,
                    ),
            )
        }

        DatabaseSearchActiveField(
            query = query,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
            focusRequester = focusRequester,
            rowMaxWidth = maxWidth,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        )
    }
}

@Composable
fun SearchNoResultsItem(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.search_no_results),
        modifier = modifier.padding(
            start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
            top = dimensionResource(R.dimen.type_detail_section_gap),
            end = dimensionResource(R.dimen.detail_card_horizontal_padding),
        ),
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = colorResource(R.color.hint_text),
    )
}
