package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogFilter
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberLazyListTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CharacterSkillsCatalogGroupPage(
    navController: NavController,
    viewModel: CharacterSkillsViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val groupId = viewModel.routeCatalogGroupId
    val filter = viewModel.routeCatalogFilter
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
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

    val group = remember(uiState.catalogGroups, groupId) {
        groupId?.let { id -> uiState.catalogGroups.find { it.groupId == id } }
    }
    val pageTitle = group?.displayName(localeController).orEmpty()
    val queuedTargets = uiState.status.queuedTargetLevelsBySkillId
    val activeTrainingSkillId = uiState.status.activeTrainingSkillId
    val activeTrainingLevel = uiState.status.activeTrainingLevel
    val attributes = uiState.attributes
    val attributesReady = uiState.attributesReady
    val (queuedSkills, otherSkills) = remember(group, filter, queuedTargets) {
        splitCatalogGroupSkills(
            group = group,
            filter = filter,
            queuedTargetLevelsBySkillId = queuedTargets,
        )
    }
    val otherSectionTitle = catalogFilterSectionTitle(filter)
    val queueSectionTitle = stringResource(R.string.character_skills_catalog_section_in_queue)

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
                contentPadding = PaddingValues(bottom = bottomPadding),
            ) {
                if (pageTitle.isNotEmpty()) {
                    item(key = "page_title") {
                        PageTitle(text = pageTitle)
                        Spacer(modifier = Modifier.height(sectionGap))
                    }
                }
                if (queuedSkills.isNotEmpty()) {
                    catalogGroupSkillSection(
                        sectionKey = "queue",
                        title = queueSectionTitle,
                        addTopGap = false,
                        skills = queuedSkills,
                        queuedTargetLevelsBySkillId = queuedTargets,
                        localeController = localeController,
                        highlightQueueTargets = true,
                        attributes = attributes,
                        attributesReady = attributesReady,
                        activeTrainingSkillId = activeTrainingSkillId,
                        activeTrainingLevel = activeTrainingLevel,
                        onSkillClick = { typeId ->
                            navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                        },
                    )
                }
                if (otherSkills.isNotEmpty()) {
                    catalogGroupSkillSection(
                        sectionKey = "other",
                        title = otherSectionTitle,
                        addTopGap = queuedSkills.isNotEmpty(),
                        skills = otherSkills,
                        queuedTargetLevelsBySkillId = queuedTargets,
                        localeController = localeController,
                        highlightQueueTargets = false,
                        attributes = attributes,
                        attributesReady = attributesReady,
                        activeTrainingSkillId = activeTrainingSkillId,
                        activeTrainingLevel = activeTrainingLevel,
                        onSkillClick = { typeId ->
                            navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun catalogFilterSectionTitle(filter: SkillCatalogFilter): String? =
    when (filter) {
        SkillCatalogFilter.ALL -> null
        SkillCatalogFilter.COMPLETED ->
            stringResource(R.string.character_skills_catalog_filter_completed)
        SkillCatalogFilter.UNTRAINED ->
            stringResource(R.string.character_skills_catalog_filter_untrained)
        SkillCatalogFilter.TRAINABLE ->
            stringResource(R.string.character_skills_catalog_filter_trainable)
    }

private fun LazyListScope.catalogGroupSkillSection(
    sectionKey: String,
    title: String?,
    addTopGap: Boolean,
    skills: List<SkillCatalogSkill>,
    queuedTargetLevelsBySkillId: Map<Int, Int>,
    localeController: LocaleController,
    highlightQueueTargets: Boolean,
    attributes: CharacterAttributes,
    attributesReady: Boolean,
    activeTrainingSkillId: Int?,
    activeTrainingLevel: Int?,
    onSkillClick: (Int) -> Unit,
) {
    if (title != null) {
        item(key = "$sectionKey:header") {
            CatalogGroupSectionHeader(title = title, addTopGap = addTopGap)
        }
    } else if (addTopGap) {
        item(key = "$sectionKey:gap") {
            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.type_detail_section_gap),
                ),
            )
        }
    }
    itemsIndexed(
        items = skills,
        key = { _, skill -> "$sectionKey:${skill.typeId}" },
    ) { index, skill ->
        CatalogGroupSkillListItem(
            skill = skill,
            queuedTargetLevel = queuedTargetLevelsBySkillId[skill.typeId],
            localeController = localeController,
            highlightQueueTarget = highlightQueueTargets,
            showDivider = index != skills.lastIndex,
            indexInSection = index,
            sectionItemCount = skills.size,
            attributes = attributes,
            attributesReady = attributesReady,
            activeTrainingSkillId = activeTrainingSkillId,
            activeTrainingLevel = activeTrainingLevel,
            onClick = { onSkillClick(skill.typeId) },
        )
    }
}

@Composable
private fun CatalogGroupSectionHeader(
    title: String,
    addTopGap: Boolean,
) {
    val sectionHeaderTextSize = dimensionResource(R.dimen.list_section_header_text_size).value.sp
    Text(
        text = title,
        fontSize = sectionHeaderTextSize,
        fontWeight = FontWeight.Black,
        color = colorResource(R.color.text_primary),
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
            bottom = dimensionResource(R.dimen.list_section_header_bottom_padding),
            top = if (addTopGap) {
                dimensionResource(R.dimen.type_detail_section_gap)
            } else {
                0.dp
            },
        ),
    )
}

@Composable
private fun CatalogGroupSkillListItem(
    skill: SkillCatalogSkill,
    queuedTargetLevel: Int?,
    localeController: LocaleController,
    highlightQueueTarget: Boolean,
    showDivider: Boolean,
    indexInSection: Int,
    sectionItemCount: Int,
    attributes: CharacterAttributes,
    attributesReady: Boolean,
    activeTrainingSkillId: Int?,
    activeTrainingLevel: Int?,
    onClick: () -> Unit,
) {
    val cardCornerRadius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = catalogSectionItemShape(indexInSection, sectionItemCount, cardCornerRadius)
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
            highlightQueueTarget = highlightQueueTarget,
            showDivider = showDivider,
            onClick = onClick,
            attributes = attributes,
            attributesReady = attributesReady,
            activeTrainingSkillId = activeTrainingSkillId,
            activeTrainingLevel = activeTrainingLevel,
        )
    }
}

private fun catalogSectionItemShape(
    indexInSection: Int,
    sectionItemCount: Int,
    corner: Dp,
): Shape {
    return when {
        sectionItemCount == 1 -> RoundedCornerShape(corner)
        indexInSection == 0 -> RoundedCornerShape(topStart = corner, topEnd = corner)
        indexInSection == sectionItemCount - 1 ->
            RoundedCornerShape(bottomStart = corner, bottomEnd = corner)
        else -> RectangleShape
    }
}

private fun splitCatalogGroupSkills(
    group: SkillCatalogGroup?,
    filter: SkillCatalogFilter,
    queuedTargetLevelsBySkillId: Map<Int, Int>,
): Pair<List<SkillCatalogSkill>, List<SkillCatalogSkill>> {
    if (group == null) return emptyList<SkillCatalogSkill>() to emptyList()
    val matching = group.skillsMatching(filter, queuedTargetLevelsBySkillId)
        .sortedBy { it.typeId }
    val queuedIds = queuedTargetLevelsBySkillId.keys
    val queued = matching.filter { it.typeId in queuedIds }
    val other = matching.filter { it.typeId !in queuedIds }
    return queued to other
}
