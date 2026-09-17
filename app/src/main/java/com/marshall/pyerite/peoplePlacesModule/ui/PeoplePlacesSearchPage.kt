package com.marshall.pyerite.peoplePlacesModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesBuildingType
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCategory
import com.marshall.pyerite.peoplePlacesModule.viewModel.PeoplePlacesViewModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeriteSegmentedControl
import com.marshall.pyerite.ui.golbalComponents.PyeriteSegmentedOption
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun PeoplePlacesSearchPage(
    navController: NavController,
    viewModel: PeoplePlacesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val pageTitle = stringResource(R.string.search)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(listState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val searchBarVertical = dimensionResource(R.dimen.search_bar_vertical_padding)
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val categoryOptions = listOf(
        PyeriteSegmentedOption(
            PeoplePlacesCategory.CHARACTER,
            stringResource(R.string.people_places_category_character),
        ),
        PyeriteSegmentedOption(
            PeoplePlacesCategory.CORPORATION,
            stringResource(R.string.people_places_category_corporation),
        ),
        PyeriteSegmentedOption(
            PeoplePlacesCategory.ALLIANCE,
            stringResource(R.string.people_places_category_alliance),
        ),
        PyeriteSegmentedOption(
            PeoplePlacesCategory.STRUCTURE,
            stringResource(R.string.people_places_category_structure),
        ),
    )

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
    ) { topBarPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding)
                .imePadding()
                .navigationBarsPadding(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    bottom = bottomPadding,
                ),
            ) {
                item(key = "page_title") {
                    PageTitle(text = pageTitle)
                }
                item(key = "type_filter") {
                    PyeriteSegmentedControl(
                        options = categoryOptions,
                        selected = uiState.category,
                        onSelect = viewModel::onCategoryChange,
                    )
                }
                item(key = "precise_filters") {
                    Box(modifier = Modifier.padding(top = sectionGap)) {
                        PeoplePlacesFilterSection(
                            uiState = uiState,
                            onCharacterFiltersChange = viewModel::onCharacterFiltersChange,
                            onCorporationFiltersChange = viewModel::onCorporationFiltersChange,
                            onAllianceExactMatchChange = viewModel::onAllianceExactMatchChange,
                            onBuildingFiltersChange = viewModel::onBuildingFiltersChange,
                            onClearFilters = viewModel::onClearFilters,
                        )
                    }
                }
                if (uiState.hasCommittedSearch) {
                    item(key = "results") {
                        Box(modifier = Modifier.padding(top = sectionGap)) {
                            PeoplePlacesResultsSection(uiState = uiState)
                        }
                    }
                }
            }
            PeoplePlacesBottomSearchBar(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = {
                    keyboard?.hide()
                    focusManager.clearFocus()
                    viewModel.onSearchAction()
                },
                onClearQuery = {
                    viewModel.onQueryChange("")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = searchBarVertical),
            )
        }
    }
}

@Composable
private fun PeoplePlacesBottomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val horizontalPadding = dimensionResource(R.dimen.detail_card_horizontal_padding)
    val corner = dimensionResource(R.dimen.detail_card_corner_radius)
    val fieldBackground = colorResource(R.color.search_field_background)
    val textColor = colorResource(R.color.text_primary)
    val hintColor = colorResource(R.color.hint_text)
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier.padding(horizontal = horizontalPadding),
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(corner))
                .background(fieldBackground)
                .focusRequester(focusRequester),
            textStyle = TextStyle(color = textColor, fontSize = 16.sp),
            singleLine = true,
            cursorBrush = SolidColor(textColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = hintColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search),
                                color = hintColor,
                                fontSize = 16.sp,
                            )
                        }
                        innerTextField()
                    }
                    if (query.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onClearQuery,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.search_clear_button_background)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.search_clear),
                                    tint = colorResource(R.color.search_clear_icon),
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}

internal fun PeoplePlacesBuildingType.labelRes(): Int = when (this) {
    PeoplePlacesBuildingType.ALL -> R.string.people_places_building_type_all
    PeoplePlacesBuildingType.STATION -> R.string.people_places_building_type_station
    PeoplePlacesBuildingType.STRUCTURE -> R.string.people_places_building_type_structure
}
