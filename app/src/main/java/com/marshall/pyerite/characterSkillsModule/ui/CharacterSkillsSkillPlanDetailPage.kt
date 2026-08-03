package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanEntry
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanListItem
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanProgressCalculator
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanProgressSummary
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.characterSkillsModule.viewModel.SkillPlanDetailViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.util.NumberDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CharacterSkillsSkillPlanDetailPage(
    navController: NavController,
    planViewModel: SkillPlanDetailViewModel = koinViewModel(),
    skillsViewModel: CharacterSkillsViewModel = koinViewModel(),
) {
    val plan by planViewModel.plan.collectAsState()
    val showCompleted by planViewModel.showCompleted.collectAsState()
    val skillsUiState by skillsViewModel.uiState.collectAsState()
    val onBack = navController.rememberNavigateUpAction()
    val scrollState = rememberScrollState()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val localeController = koinInject<LocaleController>()

    LaunchedEffect(Unit) {
        skillsViewModel.ensureCatalogLoaded()
    }

    LaunchedEffect(plan) {
        if (plan == null) {
            onBack?.invoke()
        }
    }

    val currentPlan = plan ?: return
    val pageTitle = currentPlan.name
    val skillsByTypeId = remember(skillsUiState.catalogGroups) {
        skillsUiState.catalogGroups
            .asSequence()
            .flatMap { it.skills }
            .associateBy { it.typeId }
    }
    val summary = remember(
        currentPlan.entries,
        skillsByTypeId,
        skillsUiState.attributes,
        skillsUiState.attributesReady,
    ) {
        SkillPlanProgressCalculator.summarize(
            entries = currentPlan.entries,
            skillsByTypeId = skillsByTypeId,
            attributes = skillsUiState.attributes,
            attributesReady = skillsUiState.attributesReady,
        )
    }
    val visibleEntries = remember(currentPlan.entries, skillsByTypeId, showCompleted) {
        currentPlan.entries.filter { entry ->
            showCompleted ||
                !SkillPlanProgressCalculator.isEntryCompleted(entry, skillsByTypeId)
        }
    }

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
    ) { topBarPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding)
                .verticalScroll(scrollState)
                .padding(bottom = bottomPadding),
        ) {
            PageTitle(text = pageTitle)
            Spacer(modifier = Modifier.height(sectionGap))
            SkillPlanSpSection(summary = summary)
            Spacer(modifier = Modifier.height(sectionGap))
            SkillPlanEntriesSection(
                plan = currentPlan,
                visibleEntries = visibleEntries,
                skillsByTypeId = skillsByTypeId,
                showCompleted = showCompleted,
                onShowCompletedChange = planViewModel::setShowCompleted,
                attributes = skillsUiState.attributes,
                attributesReady = skillsUiState.attributesReady,
                queuedTargets = skillsUiState.status.queuedTargetLevelsBySkillId,
                activeTrainingSkillId = skillsUiState.status.activeTrainingSkillId,
                activeTrainingLevel = skillsUiState.status.activeTrainingLevel,
                localeController = localeController,
                onSkillClick = { typeId ->
                    navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                },
            )
        }
    }
}

@Composable
private fun SkillPlanSpSection(
    summary: SkillPlanProgressSummary,
) {
    val needLearnSp = stringResource(
        R.string.character_skills_skill_plan_sp_value,
        NumberDisplayFormatter.format(summary.needLearnSp, NumberDisplayFormatter.Style.FULL),
    )
    val totalSp = stringResource(
        R.string.character_skills_skill_plan_sp_value,
        NumberDisplayFormatter.format(summary.totalSp, NumberDisplayFormatter.Style.FULL),
    )
    val needTime = formatDurationDisplay(
        totalSeconds = summary.needSeconds,
        includeSeconds = true,
    )
    val rows = listOf(
        stringResource(R.string.character_skills_skill_plan_need_learn) to needLearnSp,
        stringResource(R.string.character_skills_skill_plan_need_time) to needTime,
        stringResource(R.string.character_skills_skill_plan_total_sp) to totalSp,
    )

    BaseContainer(
        title = stringResource(R.string.character_skills_skill_plan_sp_section),
        useSystemBarsPadding = false,
    ) {
        rows.forEachIndexed { index, (label, value) ->
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    showLeadingIcon = false,
                    itemName = label,
                    trailingValue = value,
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = index != rows.lastIndex,
            )
        }
    }
}

@Composable
private fun SkillPlanEntriesSection(
    plan: SkillPlanListItem,
    visibleEntries: List<SkillPlanEntry>,
    skillsByTypeId: Map<Int, SkillCatalogSkill>,
    showCompleted: Boolean,
    onShowCompletedChange: (Boolean) -> Unit,
    attributes: CharacterAttributes,
    attributesReady: Boolean,
    queuedTargets: Map<Int, Int>,
    activeTrainingSkillId: Int?,
    activeTrainingLevel: Int?,
    localeController: LocaleController,
    onSkillClick: (Int) -> Unit,
) {
    BaseContainer(
        title = stringResource(
            R.string.character_skills_skill_plan_section,
            plan.skillCount,
        ),
        titleTrailingContent = {
            SkillPlanShowCompletedToggle(
                checked = showCompleted,
                onCheckedChange = onShowCompletedChange,
            )
        },
        useSystemBarsPadding = false,
    ) {
        if (visibleEntries.isEmpty()) {
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    showLeadingIcon = false,
                    itemName = stringResource(R.string.character_skills_skill_plan_empty),
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = false,
            )
        } else {
            visibleEntries.forEachIndexed { index, entry ->
                val skill = skillsByTypeId[entry.skillTypeId]
                if (skill != null) {
                    SkillCatalogSkillRow(
                        skill = skill,
                        queuedTargetLevel = queuedTargets[skill.typeId]
                            ?: entry.targetLevel.takeIf { it > skill.trainedLevel },
                        localeController = localeController,
                        highlightQueueTarget = false,
                        showDivider = index != visibleEntries.lastIndex,
                        onClick = { onSkillClick(skill.typeId) },
                        attributes = attributes,
                        attributesReady = attributesReady,
                        activeTrainingSkillId = activeTrainingSkillId,
                        activeTrainingLevel = activeTrainingLevel,
                        queueEntryFinishedLevel = entry.targetLevel,
                    )
                } else {
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            showLeadingIcon = false,
                            itemName = stringResource(
                                R.string.character_skills_skill_plan_entry_unknown,
                                entry.skillTypeId,
                            ),
                            trailingValue = stringResource(
                                R.string.character_skills_skill_plan_entry_level,
                                entry.targetLevel,
                            ),
                            showChevron = false,
                            onClick = null,
                        ),
                        showDivider = index != visibleEntries.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillPlanShowCompletedToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val labelColor = colorResource(R.color.text_primary)
    val checkedBackground = colorResource(R.color.hyperlink_text)
    val uncheckedBorder = colorResource(R.color.border)
    val checkSize = dimensionResource(R.dimen.skill_plan_show_completed_check_size)
    val labelGap = dimensionResource(R.dimen.skill_plan_show_completed_label_gap)

    Row(
        modifier = Modifier
            .clickable(onClick = { onCheckedChange(!checked) })
            .semantics { role = Role.Checkbox },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .size(checkSize)
                .clip(CircleShape)
                .then(
                    if (checked) {
                        Modifier.background(checkedBackground)
                    } else {
                        Modifier
                            .background(colorResource(R.color.second_background))
                            .border(
                                width = dimensionResource(R.dimen.skill_plan_show_completed_border),
                                color = uncheckedBorder,
                                shape = CircleShape,
                            )
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colorResource(R.color.white),
                    modifier = Modifier.size(
                        dimensionResource(R.dimen.skill_plan_show_completed_icon_size),
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.width(labelGap))
        Text(
            text = stringResource(R.string.character_skills_skill_plan_show_completed),
            color = labelColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
