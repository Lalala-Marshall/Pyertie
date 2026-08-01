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

    val queuedSkills = remember(
        uiState.catalogGroups,
        uiState.status.queuedSkillIdsInOrder,
        uiState.status.queuedTargetLevelsBySkillId,
    ) {
        resolveQueuedCatalogSkills(
            groups = uiState.catalogGroups,
            queuedSkillIdsInOrder = uiState.status.queuedSkillIdsInOrder,
        )
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
                    skills = queuedSkills,
                    queuedTargetLevelsBySkillId = uiState.status.queuedTargetLevelsBySkillId,
                    attributes = uiState.attributes,
                    attributesReady = uiState.attributesReady,
                    activeTrainingSkillId = uiState.status.activeTrainingSkillId,
                    activeTrainingLevel = uiState.status.activeTrainingLevel,
                    localeController = localeController,
                    onSkillClick = { typeId ->
                        navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                    },
                )
            }
        }
    }
}

@Composable
private fun CharacterSkillsQueueSection(
    status: CharacterSkillQueueStatus,
    detailsReady: Boolean,
    skills: List<SkillCatalogSkill>,
    queuedTargetLevelsBySkillId: Map<Int, Int>,
    attributes: CharacterAttributes,
    attributesReady: Boolean,
    activeTrainingSkillId: Int?,
    activeTrainingLevel: Int?,
    localeController: LocaleController,
    onSkillClick: (Int) -> Unit,
) {
    val nowMs by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = status,
    ) {
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
    val summary = skillQueueSectionSummary(
        status = status,
        detailsReady = detailsReady,
        nowMs = nowMs,
    )
    val queueHead = status.queueHead
    BaseContainer(
        title = stringResource(
            R.string.character_skills_queue_section,
            summary.queuedCount,
            summary.remainingText,
        ),
        useSystemBarsPadding = false,
    ) {
        skills.forEachIndexed { index, skill ->
            val isQueueHead = queueHead != null && skill.typeId == queueHead.skillId && index == 0
            SkillCatalogSkillRow(
                skill = skill,
                queuedTargetLevel = queuedTargetLevelsBySkillId[skill.typeId],
                localeController = localeController,
                highlightQueueTarget = true,
                showDivider = index != skills.lastIndex,
                onClick = { onSkillClick(skill.typeId) },
                attributes = attributes,
                attributesReady = attributesReady,
                activeTrainingSkillId = activeTrainingSkillId,
                activeTrainingLevel = activeTrainingLevel,
                trainedSpOverride = if (isQueueHead) queueHead.currentSpAt(nowMs) else null,
                useQueueRemaining = isQueueHead,
                queueRemainingSeconds = if (isQueueHead) {
                    queueHead.remainingSecondsAt(nowMs)
                } else {
                    null
                },
                remainingDurationPrecision = QueueSectionDurationPrecision,
                remainingDurationMaxUnit = QueueSectionDurationMaxUnit,
                showLeadingIcon = false,
                showQueueTrainingLevel = true,
                omitContentBottomPadding = isQueueHead,
                belowContent = if (isQueueHead) {
                    {
                        SkillQueueTrainingProgressBar(
                            progress = queueHead.levelProgressAt(nowMs),
                            animateShimmer = status.state == CharacterSkillQueueState.TRAINING &&
                                (queueHead.remainingSecondsAt(nowMs) ?: 0L) > 0L,
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

private fun resolveQueuedCatalogSkills(
    groups: List<SkillCatalogGroup>,
    queuedSkillIdsInOrder: List<Int>,
): List<SkillCatalogSkill> {
    if (queuedSkillIdsInOrder.isEmpty()) return emptyList()
    val byTypeId = groups.asSequence()
        .flatMap { it.skills }
        .associateBy { it.typeId }
    return queuedSkillIdsInOrder.mapNotNull { typeId -> byTypeId[typeId] }
}
