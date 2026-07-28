package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogFilter
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import com.marshall.pyerite.characterSkillsModule.model.SkillGroupIcons
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.LocalizableName
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.util.NumberDisplayFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CharacterSkillsCatalogDetailsPage(
    navController: NavController,
    viewModel: CharacterSkillsViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val pageTitle = stringResource(R.string.character_skills_catalog_details)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberLazyListTitleCollapsed(listState)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refreshCatalog,
        ),
    )

    LaunchedEffect(Unit) {
        viewModel.ensureCatalogLoaded()
    }

    val visibleGroups = remember(
        uiState.catalogGroups,
        uiState.catalogFilter,
        uiState.catalogSearchQuery,
        uiState.status.queuedTargetLevelsBySkillId,
        localeController.contentLanguage,
    ) {
        val queuedTargets = uiState.status.queuedTargetLevelsBySkillId
        uiState.catalogGroups
            .filter { group -> group.matchesFilter(uiState.catalogFilter, queuedTargets) }
            .filter { group ->
                val query = uiState.catalogSearchQuery
                if (query.isBlank()) return@filter true
                group.matchesCatalogQuery(query, localeController) ||
                    group.skills.any { skill ->
                        skill.matchesCatalogQuery(query, localeController)
                    }
            }
    }

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = endActions,
    ) { topBarPadding ->
        PyeritePullToRefreshBox(
            onRefresh = viewModel::refreshCatalog,
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = dimensionResource(R.dimen.type_detail_bottom_padding),
                ),
            ) {
                item(key = "page_title") {
                    PageTitle(text = pageTitle)
                }
                item(key = "search") {
                    SkillCatalogSearchField(
                        query = uiState.catalogSearchQuery,
                        onQueryChange = viewModel::setCatalogSearchQuery,
                        onClearQuery = { viewModel.setCatalogSearchQuery("") },
                    )
                }
                item(key = "catalog_content") {
                    BaseContainer(
                        title = null,
                        useSystemBarsPadding = false,
                    ) {
                        SkillCatalogFilterRow(
                            selected = uiState.catalogFilter,
                            onSelect = viewModel::setCatalogFilter,
                        )
                        visibleGroups.forEachIndexed { index, group ->
                            SkillCatalogGroupRow(
                                group = group,
                                filter = uiState.catalogFilter,
                                queuedTargetLevelsBySkillId =
                                    uiState.status.queuedTargetLevelsBySkillId,
                                localeController = localeController,
                                showDivider = index != visibleGroups.lastIndex,
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillCatalogSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
) {
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val horizontalPadding = dimensionResource(R.dimen.detail_card_horizontal_padding)
    val verticalPadding = dimensionResource(R.dimen.search_bar_vertical_padding)
    val corner = barHeight / 2
    val textColor = colorResource(R.color.text_primary)
    val hintColor = colorResource(R.color.hint_text)

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .height(barHeight)
            .clip(RoundedCornerShape(corner))
            .background(colorResource(R.color.search_field_idle_background)),
        textStyle = TextStyle(color = textColor, fontSize = 16.sp),
        singleLine = true,
        cursorBrush = SolidColor(textColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
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
                            text = stringResource(R.string.character_skills_catalog_search_hint),
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
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.search_clear),
                            tint = hintColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun SkillCatalogFilterRow(
    selected: SkillCatalogFilter,
    onSelect: (SkillCatalogFilter) -> Unit,
) {
    val filters = listOf(
        SkillCatalogFilter.ALL to R.string.character_skills_catalog_filter_all,
        SkillCatalogFilter.COMPLETED to R.string.character_skills_catalog_filter_completed,
        SkillCatalogFilter.UNTRAINED to R.string.character_skills_catalog_filter_untrained,
        SkillCatalogFilter.TRAINABLE to R.string.character_skills_catalog_filter_trainable,
    )
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val shape = RoundedCornerShape(barHeight / 2)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.character_skills_catalog_filter_outer_horizontal_padding),
                vertical = dimensionResource(R.dimen.character_skills_catalog_filter_outer_vertical_padding),
            ),
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colorResource(R.color.search_field_idle_background), shape)
                .padding(
                    horizontal = dimensionResource(R.dimen.character_skills_catalog_filter_bar_horizontal_padding),
                    vertical = dimensionResource(R.dimen.character_skills_catalog_filter_bar_vertical_padding),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filters.forEach { (filter, labelRes) ->
                val isSelected = selected == filter
                val itemShape = RoundedCornerShape(barHeight / 2)
                Text(
                    text = stringResource(labelRes),
                    color = if (isSelected) {
                        colorResource(R.color.text_primary)
                    } else {
                        colorResource(R.color.hint_text)
                    },
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .clip(itemShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    colorResource(R.color.character_skills_catalog_filter_selected),
                                    itemShape,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .clickable(onClick = { onSelect(filter) })
                        .padding(
                            horizontal = dimensionResource(R.dimen.character_skills_catalog_filter_item_horizontal_padding),
                            vertical = dimensionResource(R.dimen.character_skills_catalog_filter_item_vertical_padding),
                        ),
                )
            }
        }
    }
}

@Composable
private fun SkillCatalogGroupRow(
    group: SkillCatalogGroup,
    filter: SkillCatalogFilter,
    queuedTargetLevelsBySkillId: Map<Int, Int>,
    localeController: LocaleController,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    val progressColor = if (group.isCompleted) {
        colorResource(R.color.character_skills_catalog_progress_complete)
    } else {
        colorResource(R.color.character_skills_catalog_progress_partial)
    }
    val hintColor = colorResource(R.color.hint_text)
    val queueColor = colorResource(R.color.character_skills_catalog_queue_text)
    val baseHint = when (filter) {
        SkillCatalogFilter.ALL -> stringResource(
            R.string.character_skills_catalog_group_hint,
            group.skillCount,
            formatCatalogSp(group.trainedSp),
        )
        SkillCatalogFilter.COMPLETED -> stringResource(
            R.string.character_skills_catalog_group_hint_completed,
            group.matchingSkillCount(filter, queuedTargetLevelsBySkillId),
            formatCatalogSp(group.matchingTrainedSp(filter, queuedTargetLevelsBySkillId)),
        )
        SkillCatalogFilter.UNTRAINED -> stringResource(
            R.string.character_skills_catalog_group_hint_untrained,
            group.matchingSkillCount(filter, queuedTargetLevelsBySkillId),
            formatCatalogSp(group.matchingTrainedSp(filter, queuedTargetLevelsBySkillId)),
        )
        SkillCatalogFilter.TRAINABLE -> stringResource(
            R.string.character_skills_catalog_group_hint_trainable,
            group.matchingSkillCount(filter, queuedTargetLevelsBySkillId),
            formatCatalogSp(group.matchingTrainedSp(filter, queuedTargetLevelsBySkillId)),
        )
    }
    val queueCount = group.queuedMatchingSkillCount(filter, queuedTargetLevelsBySkillId)
    val queueSuffix = if (queueCount > 0) {
        stringResource(R.string.character_skills_catalog_group_hint_in_queue, queueCount)
    } else {
        null
    }
    val hintAnnotated = buildAnnotatedString {
        withStyle(SpanStyle(color = hintColor)) {
            append(baseHint)
        }
        if (queueSuffix != null) {
            withStyle(SpanStyle(color = queueColor)) {
                append(' ')
                append(queueSuffix)
            }
        }
    }
    val maxSp = group.maxSp.toFloat().coerceAtLeast(1f)
    val queuedRequiredSp = group.queuedMatchingRequiredSp(filter, queuedTargetLevelsBySkillId)
    val learnedSp = group.learnedSpExcludingQueued(filter, queuedTargetLevelsBySkillId)
    var learnedFraction = (learnedSp / maxSp).coerceAtLeast(SkillCatalogConfig.PROGRESS_MIN)
    var queuedFraction = (queuedRequiredSp / maxSp).coerceAtLeast(SkillCatalogConfig.PROGRESS_MIN)
    val segmentsTotal = learnedFraction + queuedFraction
    if (segmentsTotal > SkillCatalogConfig.PROGRESS_MAX) {
        learnedFraction /= segmentsTotal
        queuedFraction /= segmentsTotal
    }
    val remainingFraction = (
        SkillCatalogConfig.PROGRESS_MAX - learnedFraction - queuedFraction
        ).coerceAtLeast(SkillCatalogConfig.PROGRESS_MIN)

    Box(modifier = Modifier.fillMaxWidth()) {
        if (learnedFraction > 0f || queuedFraction > 0f) {
            Row(modifier = Modifier.matchParentSize()) {
                if (learnedFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(learnedFraction)
                            .fillMaxHeight()
                            .background(progressColor),
                    )
                }
                if (queuedFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(queuedFraction)
                            .fillMaxHeight()
                            .background(
                                colorResource(R.color.character_skills_catalog_queue_background),
                            ),
                    )
                }
                if (remainingFraction > 0f) {
                    Spacer(modifier = Modifier.weight(remainingFraction))
                }
            }
        }
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = SkillGroupIcons.drawableRes(group.groupId),
                iconTint = colorResource(R.color.text_primary),
                itemName = group.displayName(localeController),
                itemHints = listOf(
                    BaseLazyColumnItemHint(annotatedText = hintAnnotated),
                ),
                showChevron = true,
                onClick = onClick,
            ),
            showDivider = showDivider,
            titleTrailingContent = if (group.isCompleted) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colorResource(R.color.character_status_positive),
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(colorResource(R.color.character_status_positive).copy(alpha = 0.15f))
                            .padding(2.dp),
                    )
                }
            } else {
                null
            },
        )
    }
}

private fun formatCatalogSp(sp: Long): String =
    NumberDisplayFormatter.format(sp, NumberDisplayFormatter.Style.FULL)

private fun LocalizableName.matchesCatalogQuery(
    query: String,
    localeController: LocaleController,
): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    return displayName(localeController).contains(trimmed, ignoreCase = true) ||
        zhName?.contains(trimmed, ignoreCase = true) == true ||
        enName?.contains(trimmed, ignoreCase = true) == true ||
        name?.contains(trimmed, ignoreCase = true) == true
}
