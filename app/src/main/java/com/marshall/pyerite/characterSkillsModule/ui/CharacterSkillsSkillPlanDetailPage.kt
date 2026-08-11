package com.marshall.pyerite.characterSkillsModule.ui

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanEntry
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanExportLanguage
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanExportResult
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanImportResult
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
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarMenuItem
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.ui.golbalComponents.topBarActionSurface
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.NumberDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CharacterSkillsSkillPlanDetailPage(
    navController: NavController,
    planViewModel: SkillPlanDetailViewModel = koinViewModel(),
    skillsViewModel: CharacterSkillsViewModel = koinViewModel(),
) {
    val plan by planViewModel.plan.collectAsState()
    val levelSteps by planViewModel.levelSteps.collectAsState()
    val showCompleted by planViewModel.showCompleted.collectAsState()
    val skillsUiState by skillsViewModel.uiState.collectAsState()
    val onBack = navController.rememberNavigateUpAction()
    val scrollState = rememberScrollState()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val localeController = koinInject<LocaleController>()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showAddSkillSheet by rememberSaveable { mutableStateOf(false) }
    var showAddItemSheet by rememberSaveable { mutableStateOf(false) }
    var messageDialogRes by remember { mutableStateOf<Int?>(null) }
    var showExportLanguageDialog by remember { mutableStateOf(false) }
    val addContentDescription = stringResource(R.string.character_skills_skill_plan_add_content)
    val addSkillLabel = stringResource(R.string.character_skills_skill_plan_add_skill)
    val addItemLabel = stringResource(R.string.character_skills_skill_plan_add_item)
    val importExportDescription =
        stringResource(R.string.character_skills_skill_plan_import_export)
    val importLabel = stringResource(R.string.character_skills_skill_plan_import)
    val exportLabel = stringResource(R.string.character_skills_skill_plan_export)
    val exportZhLabel = stringResource(R.string.character_skills_skill_plan_export_zh)
    val exportEnLabel = stringResource(R.string.character_skills_skill_plan_export_en)

    fun runExport(language: SkillPlanExportLanguage) {
        showExportLanguageDialog = false
        coroutineScope.launch {
            when (val result = planViewModel.exportToClipboardText(language)) {
                is SkillPlanExportResult.Success -> {
                    clipboard.setClipEntry(
                        ClipEntry(
                            ClipData.newPlainText(importExportDescription, result.text),
                        ),
                    )
                }
                SkillPlanExportResult.EmptyPlan -> {
                    messageDialogRes = R.string.character_skills_skill_plan_export_empty
                }
            }
        }
    }

    fun runImport() {
        coroutineScope.launch {
            val clipEntry = clipboard.getClipEntry()
            val raw = clipEntry?.clipData
                ?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)
                ?.coerceToText(context)
                ?.toString()
            when (planViewModel.importFromClipboardText(raw)) {
                SkillPlanImportResult.Success -> Unit
                SkillPlanImportResult.ClipboardEmpty -> {
                    messageDialogRes = R.string.character_skills_skill_plan_clipboard_empty
                }
                SkillPlanImportResult.ParseFailed -> {
                    messageDialogRes = R.string.character_skills_skill_plan_clipboard_parse_failed
                }
            }
        }
    }

    val endActions = listOf(
        PyeriteTopBarActionItem(
            onClick = {},
            icon = Icons.Default.ImportExport,
            contentDescription = importExportDescription,
            menuItems = listOf(
                PyeriteTopBarMenuItem(
                    label = importLabel,
                    icon = Icons.Default.Download,
                    onClick = ::runImport,
                ),
                PyeriteTopBarMenuItem(
                    label = exportLabel,
                    icon = Icons.Default.Upload,
                    onClick = {
                        if (plan?.entries.isNullOrEmpty()) {
                            messageDialogRes = R.string.character_skills_skill_plan_export_empty
                        } else {
                            showExportLanguageDialog = true
                        }
                    },
                ),
            ),
        ),
        PyeriteTopBarActionItem(
            onClick = {},
            icon = Icons.Default.Add,
            contentDescription = addContentDescription,
            menuItems = listOf(
                PyeriteTopBarMenuItem(
                    label = addSkillLabel,
                    icon = Icons.Default.Add,
                    onClick = { showAddSkillSheet = true },
                ),
                PyeriteTopBarMenuItem(
                    label = addItemLabel,
                    iconRes = R.drawable.ic_skill_plan_add_item,
                    tintIcon = false,
                    onClick = { showAddItemSheet = true },
                ),
            ),
        ),
    )

    LaunchedEffect(Unit) {
        skillsViewModel.ensureCatalogLoaded()
    }

    LaunchedEffect(plan) {
        if (plan == null) {
            onBack?.invoke()
        }
    }

    if (showAddSkillSheet) {
        SkillPlanAddSkillBottomSheet(
            catalogGroups = skillsUiState.catalogGroups,
            initialPlannedLevels = plan?.entries.orEmpty().associate { entry ->
                entry.skillTypeId to entry.targetLevel
            },
            onDismiss = { showAddSkillSheet = false },
            onConfirm = { levels ->
                planViewModel.addSkills(levels)
                showAddSkillSheet = false
            },
        )
    }

    if (showAddItemSheet) {
        SkillPlanAddItemBottomSheet(
            onDismiss = { showAddItemSheet = false },
            onConfirm = { levels ->
                planViewModel.addSkills(levels)
                showAddItemSheet = false
            },
        )
    }

    messageDialogRes?.let { resId ->
        SkillPlanMessageDialog(
            message = stringResource(resId),
            onDismiss = { messageDialogRes = null },
        )
    }

    if (showExportLanguageDialog) {
        SkillPlanExportLanguageDialog(
            zhLabel = exportZhLabel,
            enLabel = exportEnLabel,
            onExportZh = { runExport(SkillPlanExportLanguage.CHINESE) },
            onExportEn = { runExport(SkillPlanExportLanguage.ENGLISH) },
            onDismiss = { showExportLanguageDialog = false },
        )
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
    val visibleEntries = remember(levelSteps, skillsByTypeId, showCompleted) {
        levelSteps.filter { entry ->
            showCompleted ||
                !SkillPlanProgressCalculator.isEntryCompleted(entry, skillsByTypeId)
        }
    }

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = endActions,
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
            CharacterSkillsInjectorsSection(
                requiredSp = summary.needLearnSp,
                skillPoints = skillsUiState.skillPoints,
                localeController = localeController,
                onInjectorClick = { typeId ->
                    navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                },
            )
            Spacer(modifier = Modifier.height(sectionGap))
            SkillPlanEntriesSection(
                stepCount = levelSteps.size,
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
            if (currentPlan.entries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(sectionGap))
                SkillPlanClearQueueSection(
                    onClear = planViewModel::clearSkills,
                )
            }
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
        maxUnit = DurationDisplayFormatter.MaxUnit.DAY,
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
    stepCount: Int,
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
            stepCount,
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
            val allCompleted = stepCount > 0 && !showCompleted
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    showLeadingIcon = false,
                    itemName = stringResource(
                        if (allCompleted) {
                            R.string.character_skills_skill_plan_all_completed
                        } else {
                            R.string.character_skills_skill_plan_empty
                        },
                    ),
                    itemNameColor = if (allCompleted) {
                        colorResource(R.color.character_status_positive)
                    } else {
                        null
                    },
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = false,
            )
        } else {
            visibleEntries.forEachIndexed { index, entry ->
                val skill = skillsByTypeId[entry.skillTypeId]
                if (skill != null) {
                    val isCompleted = SkillPlanProgressCalculator.isEntryCompleted(
                        entry,
                        skillsByTypeId,
                    )
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
                        levelFooterText = if (isCompleted) {
                            stringResource(R.string.character_skills_catalog_filter_completed)
                        } else {
                            null
                        },
                        levelFooterColor = if (isCompleted) {
                            colorResource(R.color.character_status_positive)
                        } else {
                            null
                        },
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
private fun SkillPlanClearQueueSection(
    onClear: () -> Unit,
) {
    val deleteColor = colorResource(R.color.character_delete)
    BaseContainer(
        title = null,
        useSystemBarsPadding = false,
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(R.string.character_skills_skill_plan_clear_queue),
                itemNameColor = deleteColor,
                showChevron = false,
                onClick = onClear,
            ),
            showDivider = false,
            leadingContent = { iconSize ->
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(
                        R.string.character_skills_skill_plan_clear_queue,
                    ),
                    tint = deleteColor,
                    modifier = Modifier.size(iconSize),
                )
            },
        )
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

@Composable
private fun SkillPlanMessageDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorResource(R.color.main_background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = message,
                    color = colorResource(R.color.text_primary),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
                SkillPlanDialogActionButton(
                    label = stringResource(R.string.character_skills_skill_plan_dialog_ok),
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun SkillPlanExportLanguageDialog(
    zhLabel: String,
    enLabel: String,
    onExportZh: () -> Unit,
    onExportEn: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorResource(R.color.main_background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.character_skills_skill_plan_export),
                    color = colorResource(R.color.text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                SkillPlanDialogActionButton(
                    label = zhLabel,
                    onClick = onExportZh,
                    modifier = Modifier.fillMaxWidth(),
                )
                SkillPlanDialogActionButton(
                    label = enLabel,
                    onClick = onExportEn,
                    modifier = Modifier.fillMaxWidth(),
                )
                SkillPlanDialogActionButton(
                    label = stringResource(R.string.character_skills_skill_plan_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SkillPlanDialogActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(percent = 50)
    val buttonHeight = dimensionResource(R.dimen.top_bar_back_button_size)
    Box(
        modifier = modifier
            .height(buttonHeight)
            .topBarActionSurface(pillShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { role = Role.Button }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = colorResource(R.color.text_primary),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}
