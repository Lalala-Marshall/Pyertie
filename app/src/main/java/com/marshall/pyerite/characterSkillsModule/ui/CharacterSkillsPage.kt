package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CharacterSkillsPage(
    navController: NavController,
    viewModel: CharacterSkillsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_skills)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val refreshTopBarAction = pyeritePullRefreshTopBarAction(
        isRefreshing = uiState.isLoading,
        refreshFailed = uiState.loadFailed,
        onRefresh = viewModel::refresh,
    )

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = listOfNotNull(refreshTopBarAction),
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
                    .verticalScroll(scrollState),
            ) {
                PageTitle(text = pageTitle)
                Text(
                    text = stringResource(R.string.character_skills_page_placeholder),
                    color = colorResource(R.color.hint_text),
                    fontSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding),
                    ),
                )
            }
        }
    }
}
