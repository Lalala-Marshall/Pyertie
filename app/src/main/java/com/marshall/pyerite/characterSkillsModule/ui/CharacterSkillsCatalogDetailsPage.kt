package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogFilter
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.characterSkillsModule.model.SkillGroupIcons
import com.marshall.pyerite.characterSkillsModule.navHost.CharacterSkillsRoute
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.ui.golbalComponents.search.ListSearchState
import com.marshall.pyerite.ui.golbalComponents.search.PyeriteListSearchHost
import com.marshall.pyerite.ui.golbalComponents.search.SearchNoResultsItem
import com.marshall.pyerite.ui.golbalComponents.search.matchesSearchQuery
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.util.NumberDisplayFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private sealed class CatalogListEntry(val key: String) {
    data class SectionHeader(
        val groupId: Int,
        val title: String,
        val addTopGap: Boolean,
    ) : CatalogListEntry("header:$groupId")

    data class SkillItem(
        val groupId: Int,
        val skill: SkillCatalogSkill,
        val showDivider: Boolean,
        val indexInSection: Int,
        val sectionItemCount: Int,
    ) : CatalogListEntry("skill:$groupId:${skill.typeId}")

    data object BottomPadding : CatalogListEntry("page:bottom_padding")
}

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
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refreshCatalog,
        ),
    )
    val searchState = ListSearchState(
        isActive = uiState.catalogSearchActive,
        query = uiState.catalogSearchQuery,
    )

    LaunchedEffect(Unit) {
        viewModel.ensureCatalogLoaded()
    }

    val queuedTargets = uiState.status.queuedTargetLevelsBySkillId
    val visibleGroups = remember(
        uiState.catalogGroups,
        uiState.catalogFilter,
        queuedTargets,
    ) {
        uiState.catalogGroups.filter { group ->
            group.matchesFilter(uiState.catalogFilter, queuedTargets)
        }
    }
    val searchEntries = remember(
        uiState.catalogGroups,
        uiState.catalogFilter,
        uiState.catalogSearchQuery,
        queuedTargets,
        localeController.contentLanguage,
    ) {
        buildCatalogSearchEntries(
            groups = uiState.catalogGroups,
            filter = uiState.catalogFilter,
            query = uiState.catalogSearchQuery,
            queuedTargetLevelsBySkillId = queuedTargets,
            localeController = localeController,
        )
    }
    val hasSearchItems = searchEntries.any { it is CatalogListEntry.SkillItem }
    val isSearchMode = uiState.catalogSearchQuery.isNotBlank()

    PyeritePullToRefreshBox(
        onRefresh = viewModel::refreshCatalog,
        modifier = Modifier.fillMaxSize(),
    ) {
        PyeriteListSearchHost(
            searchState = searchState,
            onActivateSearch = { viewModel.setCatalogSearchActive(true) },
            onQueryChange = viewModel::setCatalogSearchQuery,
            onCancelSearch = viewModel::cancelCatalogSearch,
            listState = listState,
            navTitle = pageTitle,
            modifier = Modifier.fillMaxSize(),
            onBack = onBack,
            endActions = endActions,
            title = {
                PageTitle(text = pageTitle)
            },
        ) { query ->
            if (query.isNotBlank() && !hasSearchItems) {
                item(key = "search_no_results") {
                    SearchNoResultsItem()
                }
            }
            if (isSearchMode) {
                items(
                    items = searchEntries,
                    key = { entry -> entry.key },
                ) { entry ->
                    CatalogSearchEntryContent(
                        entry = entry,
                        queuedTargetLevelsBySkillId = queuedTargets,
                        activeTrainingSkillId = uiState.status.activeTrainingSkillId,
                        activeTrainingLevel = uiState.status.activeTrainingLevel,
                        localeController = localeController,
                        onSkillClick = { typeId ->
                            navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                        },
                    )
                }
            } else {
                item(key = "catalog_filter") {
                    CatalogFilterCard(
                        selected = uiState.catalogFilter,
                        onSelect = viewModel::setCatalogFilter,
                        hasGroupsBelow = visibleGroups.isNotEmpty(),
                    )
                }
                itemsIndexed(
                    items = visibleGroups,
                    key = { _, group -> "catalog_group:${group.groupId}" },
                ) { index, group ->
                    CatalogGroupListItem(
                        group = group,
                        filter = uiState.catalogFilter,
                        queuedTargetLevelsBySkillId = queuedTargets,
                        localeController = localeController,
                        showDivider = index != visibleGroups.lastIndex,
                        indexInSection = index,
                        sectionItemCount = visibleGroups.size,
                        onClick = {
                            navController.navigate(
                                CharacterSkillsRoute.CatalogGroup.create(
                                    characterId = uiState.status.characterId,
                                    groupId = group.groupId,
                                    filter = uiState.catalogFilter,
                                ),
                            )
                        },
                    )
                }
                item(key = "page:bottom_padding") {
                    Spacer(
                        modifier = Modifier.height(
                            dimensionResource(R.dimen.type_detail_bottom_padding),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogSearchEntryContent(
    entry: CatalogListEntry,
    queuedTargetLevelsBySkillId: Map<Int, Int>,
    activeTrainingSkillId: Int?,
    activeTrainingLevel: Int?,
    localeController: LocaleController,
    onSkillClick: (Int) -> Unit,
) {
    val sectionHeaderTextSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val sectionHeaderBottomPadding = dimensionResource(R.dimen.list_section_header_bottom_padding)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    when (entry) {
        is CatalogListEntry.SectionHeader -> {
            Text(
                text = entry.title,
                fontSize = sectionHeaderTextSize,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(
                    start = titleStartPadding,
                    bottom = sectionHeaderBottomPadding,
                    top = if (entry.addTopGap) sectionGap else 0.dp,
                ),
            )
        }
        is CatalogListEntry.SkillItem -> {
            CatalogSearchSkillSectionItem(
                skill = entry.skill,
                queuedTargetLevel = queuedTargetLevelsBySkillId[entry.skill.typeId],
                localeController = localeController,
                showDivider = entry.showDivider,
                indexInSection = entry.indexInSection,
                sectionItemCount = entry.sectionItemCount,
                activeTrainingSkillId = activeTrainingSkillId,
                activeTrainingLevel = activeTrainingLevel,
                onClick = { onSkillClick(entry.skill.typeId) },
            )
        }
        CatalogListEntry.BottomPadding -> {
            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun CatalogSearchSkillSectionItem(
    skill: SkillCatalogSkill,
    queuedTargetLevel: Int?,
    localeController: LocaleController,
    showDivider: Boolean,
    indexInSection: Int,
    sectionItemCount: Int,
    activeTrainingSkillId: Int?,
    activeTrainingLevel: Int?,
    onClick: () -> Unit,
) {
    val cardCornerRadius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = sectionItemShape(indexInSection, sectionItemCount, cardCornerRadius)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
            .clip(shape)
            .background(colorResource(R.color.second_background), shape),
    ) {
        SkillCatalogSkillRow(
            skill = skill,
            queuedTargetLevel = queuedTargetLevel,
            localeController = localeController,
            highlightQueueTarget = queuedTargetLevel != null,
            showDivider = showDivider,
            onClick = onClick,
            activeTrainingSkillId = activeTrainingSkillId,
            activeTrainingLevel = activeTrainingLevel,
        )
    }
}

private fun sectionItemShape(indexInSection: Int, sectionItemCount: Int, corner: Dp): Shape {
    return when {
        sectionItemCount == 1 -> RoundedCornerShape(corner)
        indexInSection == 0 -> RoundedCornerShape(topStart = corner, topEnd = corner)
        indexInSection == sectionItemCount - 1 ->
            RoundedCornerShape(bottomStart = corner, bottomEnd = corner)
        else -> RectangleShape
    }
}

private fun buildCatalogSearchEntries(
    groups: List<SkillCatalogGroup>,
    filter: SkillCatalogFilter,
    query: String,
    queuedTargetLevelsBySkillId: Map<Int, Int>,
    localeController: LocaleController,
): List<CatalogListEntry> = buildList {
    if (query.isBlank()) {
        add(CatalogListEntry.BottomPadding)
        return@buildList
    }

    var addTopGap = false
    groups
        .sortedBy { it.groupId }
        .forEach { group ->
            val skills = group.skillsMatching(filter, queuedTargetLevelsBySkillId)
                .filter { it.matchesSearchQuery(query, localeController) }
                .sortedBy { it.typeId }
            if (skills.isEmpty()) return@forEach

            add(
                CatalogListEntry.SectionHeader(
                    groupId = group.groupId,
                    title = group.displayName(localeController),
                    addTopGap = addTopGap,
                ),
            )
            skills.forEachIndexed { index, skill ->
                add(
                    CatalogListEntry.SkillItem(
                        groupId = group.groupId,
                        skill = skill,
                        showDivider = index != skills.lastIndex,
                        indexInSection = index,
                        sectionItemCount = skills.size,
                    ),
                )
            }
            addTopGap = true
        }

    add(CatalogListEntry.BottomPadding)
}

@Composable
private fun CatalogFilterCard(
    selected: SkillCatalogFilter,
    onSelect: (SkillCatalogFilter) -> Unit,
    hasGroupsBelow: Boolean,
) {
    val corner = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = if (hasGroupsBelow) {
        RoundedCornerShape(topStart = corner, topEnd = corner)
    } else {
        RoundedCornerShape(corner)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
            .clip(shape)
            .background(colorResource(R.color.second_background), shape),
    ) {
        SkillCatalogFilterRow(
            selected = selected,
            onSelect = onSelect,
        )
    }
}

@Composable
private fun CatalogGroupListItem(
    group: SkillCatalogGroup,
    filter: SkillCatalogFilter,
    queuedTargetLevelsBySkillId: Map<Int, Int>,
    localeController: LocaleController,
    showDivider: Boolean,
    indexInSection: Int,
    sectionItemCount: Int,
    onClick: () -> Unit,
) {
    val corner = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = catalogGroupsItemShape(
        indexInSection = indexInSection,
        sectionItemCount = sectionItemCount,
        corner = corner,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
            .clip(shape)
            .background(colorResource(R.color.second_background), shape),
    ) {
        SkillCatalogGroupRow(
            group = group,
            filter = filter,
            queuedTargetLevelsBySkillId = queuedTargetLevelsBySkillId,
            localeController = localeController,
            showDivider = showDivider,
            onClick = onClick,
        )
    }
}

/** Groups sit under the filter card — never round the top edge. */
private fun catalogGroupsItemShape(
    indexInSection: Int,
    sectionItemCount: Int,
    corner: Dp,
): Shape {
    return when {
        sectionItemCount == 1 || indexInSection == sectionItemCount - 1 ->
            RoundedCornerShape(bottomStart = corner, bottomEnd = corner)
        else -> RectangleShape
    }
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
