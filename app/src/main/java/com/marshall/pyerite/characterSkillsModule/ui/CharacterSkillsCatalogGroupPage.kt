package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogFilter
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CharacterSkillsCatalogGroupPage(
    navController: NavController,
    viewModel: CharacterSkillsViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val groupId = viewModel.routeCatalogGroupId
    val filter = viewModel.routeCatalogFilter
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
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
    val (queuedSkills, otherSkills) = remember(group, filter, queuedTargets) {
        splitCatalogGroupSkills(
            group = group,
            filter = filter,
            queuedTargetLevelsBySkillId = queuedTargets,
        )
    }
    val otherSectionTitle = catalogFilterSectionTitle(filter)

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = bottomPadding),
            ) {
                if (pageTitle.isNotEmpty()) {
                    PageTitle(text = pageTitle)
                    Spacer(modifier = Modifier.height(sectionGap))
                }
                if (queuedSkills.isNotEmpty()) {
                    CatalogGroupSkillSection(
                        title = stringResource(R.string.character_skills_catalog_section_in_queue),
                        skills = queuedSkills,
                        queuedTargetLevelsBySkillId = queuedTargets,
                        localeController = localeController,
                        highlightQueueTargets = true,
                        onSkillClick = { typeId ->
                            navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                        },
                    )
                    if (otherSkills.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(sectionGap))
                    }
                }
                if (otherSkills.isNotEmpty()) {
                    CatalogGroupSkillSection(
                        title = otherSectionTitle,
                        skills = otherSkills,
                        queuedTargetLevelsBySkillId = queuedTargets,
                        localeController = localeController,
                        highlightQueueTargets = false,
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

@Composable
private fun CatalogGroupSkillSection(
    title: String?,
    skills: List<SkillCatalogSkill>,
    queuedTargetLevelsBySkillId: Map<Int, Int>,
    localeController: LocaleController,
    highlightQueueTargets: Boolean,
    onSkillClick: (Int) -> Unit,
) {
    BaseContainer(
        title = title,
        useSystemBarsPadding = false,
    ) {
        Column {
            skills.forEachIndexed { index, skill ->
                SkillCatalogSkillRow(
                    skill = skill,
                    queuedTargetLevel = queuedTargetLevelsBySkillId[skill.typeId],
                    localeController = localeController,
                    highlightQueueTarget = highlightQueueTargets,
                    showDivider = index != skills.lastIndex,
                    onClick = { onSkillClick(skill.typeId) },
                )
            }
        }
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
