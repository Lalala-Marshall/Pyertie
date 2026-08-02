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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueConfig
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueState
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.characterSkillsModule.model.SkillQueueHeadTraining
import com.marshall.pyerite.characterSkillsModule.navHost.CharacterSkillsRoute
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private val QueueSectionDurationPrecision = DurationDisplayFormatter.Precision.HOUR
private val QueueSectionDurationMaxUnit = DurationDisplayFormatter.MaxUnit.DAY

@Composable
internal fun CharacterSkillsPage(
    navController: NavController,
    viewModel: CharacterSkillsViewModel = koinViewModel(),
    localeController: LocaleController = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_skills)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = {
                viewModel.refresh()
                viewModel.refreshCatalog()
            },
        ),
    )

    LaunchedEffect(Unit) {
        viewModel.ensureCatalogLoaded()
        // Fresh ESI queue head (training_start_sp / finish dates) for live SP + remaining.
        viewModel.refresh()
    }

    val queuedRows = remember(
        uiState.catalogGroups,
        uiState.status.queuedEntries,
    ) {
        resolveQueuedCatalogRows(
            groups = uiState.catalogGroups,
            queuedEntries = uiState.status.queuedEntries,
        )
    }

    val nowMs by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = uiState.status,
    ) {
        val status = uiState.status
        val shouldTick = status.state == CharacterSkillQueueState.TRAINING ||
            status.queueHead?.finishAtEpochMs != null
        if (!shouldTick) {
            value = System.currentTimeMillis()
            return@produceState
        }
        while (isActive) {
            value = System.currentTimeMillis()
            val headFinish = status.queueHead?.finishAtEpochMs
            val remainingEnds = status.trainingFinishAtEpochMs.filter { it > value }
            val headDone = headFinish == null || value >= headFinish
            if (remainingEnds.isEmpty() && headDone) break
            delay(CharacterSkillQueueConfig.UI_TICK_MS.milliseconds)
        }
    }

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = endActions,
    ) { topBarPadding ->
        PyeritePullToRefreshBox(
            onRefresh = {
                viewModel.refresh()
                viewModel.refreshCatalog()
            },
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
                PageTitle(text = pageTitle)
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterSkillsCatalogSection(
                    onAttributesClick = {
                        navController.navigate(
                            CharacterSkillsRoute.Attributes.create(uiState.status.characterId),
                        )
                    },
                    onSkillPlanClick = {
                        navController.navigate(
                            CharacterSkillsRoute.SkillPlan.create(uiState.status.characterId),
                        )
                    },
                    onCatalogDetailsClick = {
                        navController.navigate(
                            CharacterSkillsRoute.CatalogDetails.create(uiState.status.characterId),
                        )
                    },
                )
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterSkillsQueueSection(
                    status = uiState.status,
                    detailsReady = uiState.detailsReady,
                    rows = queuedRows,
                    attributes = uiState.attributes,
                    attributesReady = uiState.attributesReady,
                    activeTrainingSkillId = uiState.status.activeTrainingSkillId,
                    activeTrainingLevel = uiState.status.activeTrainingLevel,
                    localeController = localeController,
                    nowMs = nowMs,
                    onSkillClick = { typeId ->
                        navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                    },
                )
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterSkillsQueueInjectorsSection(
                    status = uiState.status,
                    skillPoints = uiState.skillPoints,
                    nowMs = nowMs,
                    localeController = localeController,
                    onInjectorClick = { typeId ->
                        navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                    },
                )
                CharacterSkillsOptimalAttributesSection(
                    status = uiState.status,
                    catalogGroups = uiState.catalogGroups,
                    attributes = uiState.attributes,
                    implantBonuses = uiState.implantBonuses,
                    attributesReady = uiState.attributesReady,
                    catalogReady = uiState.catalogReady,
                    implantBonusesReady = uiState.implantBonusesReady,
                    nowMs = nowMs,
                    sectionGap = sectionGap,
                )
            }
        }
    }
}

@Composable
private fun CharacterSkillsQueueSection(
    status: CharacterSkillQueueStatus,
    detailsReady: Boolean,
    rows: List<QueuedCatalogRow>,
    attributes: CharacterAttributes,
    attributesReady: Boolean,
    activeTrainingSkillId: Int?,
    activeTrainingLevel: Int?,
    localeController: LocaleController,
    nowMs: Long,
    onSkillClick: (Int) -> Unit,
) {
    val summary = skillQueueSectionSummary(
        status = status,
        detailsReady = detailsReady,
        nowMs = nowMs,
    )
    BaseContainer(
        title = stringResource(
            R.string.character_skills_queue_section,
            summary.queuedCount,
            summary.remainingText,
        ),
        useSystemBarsPadding = false,
    ) {
        rows.forEachIndexed { index, row ->
            val entry = row.entry
            val skill = row.skill
            val isQueueHead = index == 0
            SkillCatalogSkillRow(
                skill = skill,
                queuedTargetLevel = entry.finishedLevel,
                localeController = localeController,
                highlightQueueTarget = true,
                showDivider = index != rows.lastIndex,
                onClick = { onSkillClick(skill.typeId) },
                attributes = attributes,
                attributesReady = attributesReady,
                activeTrainingSkillId = activeTrainingSkillId,
                activeTrainingLevel = activeTrainingLevel,
                trainedSpOverride = if (isQueueHead) entry.currentSpAt(nowMs) else null,
                useQueueRemaining = true,
                queueRemainingSeconds = entry.remainingSecondsAt(nowMs),
                remainingDurationPrecision = QueueSectionDurationPrecision,
                remainingDurationMaxUnit = QueueSectionDurationMaxUnit,
                showLeadingIcon = false,
                queueEntryFinishedLevel = entry.finishedLevel,
                omitContentBottomPadding = isQueueHead,
                belowContent = if (isQueueHead) {
                    {
                        SkillQueueTrainingProgressBar(
                            progress = entry.levelProgressAt(nowMs),
                            animateShimmer = status.state == CharacterSkillQueueState.TRAINING &&
                                (entry.remainingSecondsAt(nowMs) ?: 0L) > 0L,
                            showLeadingIcon = false,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun CharacterSkillsCatalogSection(
    onAttributesClick: () -> Unit,
    onSkillPlanClick: () -> Unit,
    onCatalogDetailsClick: () -> Unit,
) {
    BaseContainer(
        title = stringResource(R.string.character_skills_catalog_section),
        useSystemBarsPadding = false,
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_attributes,
                itemName = stringResource(R.string.character_skills_attributes),
                onClick = onAttributesClick,
            ),
            showDivider = true,
        )
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_skills,
                itemName = stringResource(R.string.character_skills_catalog_details),
                onClick = onCatalogDetailsClick,
            ),
            showDivider = true,
        )
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_skill_plan,
                itemName = stringResource(R.string.character_skills_skill_plan),
                onClick = onSkillPlanClick,
            ),
            showDivider = false,
        )
    }
}

private data class SkillQueueSectionSummary(
    val queuedCount: Int,
    val remainingText: String,
)

@Composable
private fun skillQueueSectionSummary(
    status: CharacterSkillQueueStatus,
    detailsReady: Boolean,
    nowMs: Long,
): SkillQueueSectionSummary {
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val zeroDuration = formatDurationDisplay(
        totalSeconds = 0L,
        precision = QueueSectionDurationPrecision,
        maxUnit = QueueSectionDurationMaxUnit,
    )

    if (!detailsReady &&
        status.state == CharacterSkillQueueState.IDLE &&
        status.trainingFinishAtEpochMs.isEmpty() &&
        status.pausedSkillCount == 0
    ) {
        return SkillQueueSectionSummary(queuedCount = 0, remainingText = placeholder)
    }

    return when (status.state) {
        CharacterSkillQueueState.IDLE ->
            SkillQueueSectionSummary(queuedCount = 0, remainingText = zeroDuration)
        CharacterSkillQueueState.PAUSED ->
            SkillQueueSectionSummary(
                queuedCount = status.pausedSkillCount,
                remainingText = status.pausedRemainingSeconds?.let { seconds ->
                    formatDurationDisplay(
                        totalSeconds = seconds,
                        precision = QueueSectionDurationPrecision,
                        maxUnit = QueueSectionDurationMaxUnit,
                    )
                } ?: placeholder,
            )
        CharacterSkillQueueState.TRAINING -> {
            val remainingEnds = status.trainingFinishAtEpochMs.filter { it > nowMs }
            if (remainingEnds.isEmpty()) {
                SkillQueueSectionSummary(queuedCount = 0, remainingText = zeroDuration)
            } else {
                val endMs = remainingEnds.maxOrNull() ?: nowMs
                SkillQueueSectionSummary(
                    queuedCount = remainingEnds.size,
                    remainingText = formatDurationDisplay(
                        totalSeconds = (endMs - nowMs) / CharacterSkillQueueConfig.MILLIS_PER_SECOND,
                        precision = QueueSectionDurationPrecision,
                        maxUnit = QueueSectionDurationMaxUnit,
                    ),
                )
            }
        }
    }
}

private data class QueuedCatalogRow(
    val entry: SkillQueueHeadTraining,
    val skill: SkillCatalogSkill,
)

private fun resolveQueuedCatalogRows(
    groups: List<SkillCatalogGroup>,
    queuedEntries: List<SkillQueueHeadTraining>,
): List<QueuedCatalogRow> {
    if (queuedEntries.isEmpty()) return emptyList()
    val byTypeId = groups.asSequence()
        .flatMap { it.skills }
        .associateBy { it.typeId }
    return queuedEntries.mapNotNull { entry ->
        val skill = byTypeId[entry.skillId] ?: return@mapNotNull null
        QueuedCatalogRow(entry = entry, skill = skill)
    }
}
