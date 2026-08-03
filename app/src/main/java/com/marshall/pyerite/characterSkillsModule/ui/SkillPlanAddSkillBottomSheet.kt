package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.data.SkillPrerequisiteResolver
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogGroup
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogSkill
import com.marshall.pyerite.characterSkillsModule.model.SkillGroupIcons
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.search.SearchNoResultsItem
import com.marshall.pyerite.ui.golbalComponents.search.matchesSearchQuery
import com.marshall.pyerite.ui.golbalComponents.search.matchingSearch
import com.marshall.pyerite.ui.golbalComponents.topBarActionSurface
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Bottom sheet to pick a skill for a plan: skill-group list (catalog-style, no filter /
 * progress), with search. Drill into a group for individual skills.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SkillPlanAddSkillBottomSheet(
    catalogGroups: List<SkillCatalogGroup>,
    onDismiss: () -> Unit,
    onConfirm: (Map<Int, Int>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val localeController = koinInject<LocaleController>()
    val prerequisiteResolver = koinInject<SkillPrerequisiteResolver>()
    val coroutineScope = rememberCoroutineScope()
    var selectedGroupId by rememberSaveable { mutableStateOf<Int?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val plannedLevels = remember { mutableStateMapOf<Int, Int>() }
    var isConfirming by remember { mutableStateOf(false) }
    val sheetCorner = dimensionResource(R.dimen.skill_plan_add_skill_sheet_corner)
    val sheetBackground = colorResource(R.color.search_field_idle_background)
    val sheetHorizontalPadding =
        dimensionResource(R.dimen.skill_plan_add_skill_sheet_horizontal_padding)
    val selectedGroup = remember(selectedGroupId, catalogGroups) {
        selectedGroupId?.let { id -> catalogGroups.firstOrNull { it.groupId == id } }
    }

    fun plannedLevelFor(skill: SkillCatalogSkill): Int =
        plannedLevels[skill.typeId]
            ?.coerceIn(0, SkillCatalogConfig.MAX_SKILL_LEVEL)
            ?: 0

    fun applyPlannedLevel(skillTypeId: Int, level: Int) {
        plannedLevels[skillTypeId] = level.coerceIn(0, SkillCatalogConfig.MAX_SKILL_LEVEL)
    }

    fun adjustPlannedLevel(skill: SkillCatalogSkill, delta: Int) {
        val current = plannedLevelFor(skill)
        val raisingFromZero = delta > 0 && current == 0
        applyPlannedLevel(skill.typeId, current + delta)
        if (!raisingFromZero) return
        // Raise prerequisites to required levels; lowering to 0 never touches them.
        coroutineScope.launch {
            val required = prerequisiteResolver.requiredLevels(skill.typeId)
            required.forEach { (typeId, requiredLevel) ->
                val existing = plannedLevels[typeId] ?: 0
                if (requiredLevel > existing) {
                    applyPlannedLevel(typeId, requiredLevel)
                }
            }
        }
    }

    fun confirmAndDismiss() {
        if (isConfirming) return
        isConfirming = true
        coroutineScope.launch {
            val selected = plannedLevels.filterValues { it > 0 }
            if (selected.isEmpty()) {
                onDismiss()
                return@launch
            }
            // Re-resolve prereqs so a fast "Done" still includes auto-raised skills.
            val merged = selected.toMutableMap()
            selected.keys.forEach { typeId ->
                prerequisiteResolver.requiredLevels(typeId).forEach { (prereqId, requiredLevel) ->
                    val existing = merged[prereqId] ?: 0
                    if (requiredLevel > existing) {
                        merged[prereqId] = requiredLevel.coerceIn(
                            0,
                            SkillCatalogConfig.MAX_SKILL_LEVEL,
                        )
                    }
                }
            }
            onConfirm(merged.filterValues { it > 0 })
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = sheetCorner, topEnd = sheetCorner),
        containerColor = sheetBackground,
        scrimColor = colorResource(R.color.search_scrim),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(SkillPlanAddSkillSheetConfig.HEIGHT_FRACTION)
                .background(sheetBackground),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = sheetHorizontalPadding,
                    end = sheetHorizontalPadding,
                    top = dimensionResource(R.dimen.skill_plan_add_skill_header_top_padding),
                ),
            ) {
                SkillPlanAddSkillSheetHeader(
                    title = if (selectedGroup != null) {
                        selectedGroup.displayName(localeController)
                    } else {
                        stringResource(R.string.character_skills_skill_plan_add_skill)
                    },
                    showBack = selectedGroup != null,
                    onBack = {
                        selectedGroupId = null
                        searchQuery = ""
                    },
                    onDone = ::confirmAndDismiss,
                )
                SkillPlanAddSkillSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(
                        top = dimensionResource(R.dimen.skill_plan_add_skill_search_top_gap),
                        bottom = dimensionResource(R.dimen.skill_plan_add_skill_search_bottom_gap),
                    ),
                )
            }
            if (selectedGroup == null) {
                if (searchQuery.isBlank()) {
                    SkillPlanAddSkillGroupsList(
                        groups = catalogGroups,
                        localeController = localeController,
                        onGroupClick = { group ->
                            selectedGroupId = group.groupId
                            searchQuery = ""
                        },
                    )
                } else {
                    SkillPlanAddSkillSkillsSearchList(
                        groups = catalogGroups,
                        searchQuery = searchQuery,
                        localeController = localeController,
                        plannedLevelFor = ::plannedLevelFor,
                        onAdjustLevel = ::adjustPlannedLevel,
                    )
                }
            } else {
                SkillPlanAddSkillSkillsList(
                    group = selectedGroup,
                    searchQuery = searchQuery,
                    localeController = localeController,
                    plannedLevelFor = ::plannedLevelFor,
                    onAdjustLevel = ::adjustPlannedLevel,
                )
            }
        }
    }
}

@Composable
private fun SkillPlanAddSkillSheetHeader(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val buttonHeight = dimensionResource(R.dimen.top_bar_back_button_size)
    val pillShape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
    ) {
        if (showBack) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(buttonHeight)
                    .topBarActionSurface(pillShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack,
                    )
                    .semantics { role = Role.Button },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back),
                    tint = colorResource(R.color.text_primary),
                    modifier = Modifier.size(dimensionResource(R.dimen.top_bar_icon_size)),
                )
            }
        }
        Text(
            text = title,
            color = colorResource(R.color.text_primary),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = buttonHeight + 8.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(buttonHeight)
                .topBarActionSurface(pillShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDone,
                )
                .semantics { role = Role.Button }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.character_done),
                color = colorResource(R.color.text_primary),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SkillPlanAddSkillSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val hintColor = colorResource(R.color.hint_text)
    val textColor = colorResource(R.color.text_primary)

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(percent = 50))
            .background(colorResource(R.color.second_background)),
        singleLine = true,
        textStyle = TextStyle(color = textColor, fontSize = 16.sp),
        cursorBrush = SolidColor(textColor),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = hintColor,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_input_hint),
                            color = hintColor,
                            fontSize = 16.sp,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun SkillPlanAddSkillGroupsList(
    groups: List<SkillCatalogGroup>,
    localeController: LocaleController,
    onGroupClick: (SkillCatalogGroup) -> Unit,
) {
    if (groups.isEmpty()) {
        SearchNoResultsItem()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "skill_plan_add_skill_groups_card") {
            BaseContainer(
                title = null,
                useSystemBarsPadding = false,
            ) {
                groups.forEachIndexed { index, group ->
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            iconRes = SkillGroupIcons.drawableRes(group.groupId),
                            iconTint = colorResource(R.color.text_primary),
                            itemName = group.displayName(localeController),
                            itemHints = listOf(
                                BaseLazyColumnItemHint(
                                    text = stringResource(
                                        R.string.character_skills_skill_plan_picker_group_hint,
                                        group.skillCount,
                                    ),
                                ),
                            ),
                            showChevron = true,
                            onClick = { onGroupClick(group) },
                        ),
                        showDivider = index != groups.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillPlanAddSkillSkillsSearchList(
    groups: List<SkillCatalogGroup>,
    searchQuery: String,
    localeController: LocaleController,
    plannedLevelFor: (SkillCatalogSkill) -> Int,
    onAdjustLevel: (SkillCatalogSkill, Int) -> Unit,
) {
    val matchingSkills = remember(groups, searchQuery, localeController.contentLanguage) {
        groups
            .asSequence()
            .flatMap { it.skills }
            .filter { it.matchesSearchQuery(searchQuery, localeController) }
            .distinctBy { it.typeId }
            .sortedBy { it.typeId }
            .toList()
    }
    SkillPlanAddSkillSkillsRows(
        skills = matchingSkills,
        localeController = localeController,
        plannedLevelFor = plannedLevelFor,
        onAdjustLevel = onAdjustLevel,
        listKey = "skill_plan_add_skill_search_card",
    )
}

@Composable
private fun SkillPlanAddSkillSkillsList(
    group: SkillCatalogGroup,
    searchQuery: String,
    localeController: LocaleController,
    plannedLevelFor: (SkillCatalogSkill) -> Int,
    onAdjustLevel: (SkillCatalogSkill, Int) -> Unit,
) {
    val visibleSkills = remember(group.skills, searchQuery, localeController.contentLanguage) {
        group.skills.matchingSearch(searchQuery, localeController)
    }
    SkillPlanAddSkillSkillsRows(
        skills = visibleSkills,
        localeController = localeController,
        plannedLevelFor = plannedLevelFor,
        onAdjustLevel = onAdjustLevel,
        listKey = "skill_plan_add_skill_skills_card",
    )
}

@Composable
private fun SkillPlanAddSkillSkillsRows(
    skills: List<SkillCatalogSkill>,
    localeController: LocaleController,
    plannedLevelFor: (SkillCatalogSkill) -> Int,
    onAdjustLevel: (SkillCatalogSkill, Int) -> Unit,
    listKey: String,
) {
    when {
        skills.isEmpty() -> {
            SearchNoResultsItem()
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = listKey) {
                    BaseContainer(
                        title = null,
                        useSystemBarsPadding = false,
                    ) {
                        skills.forEachIndexed { index, skill ->
                            SkillPlanAddSkillPickerRow(
                                skill = skill,
                                localeController = localeController,
                                plannedLevel = plannedLevelFor(skill),
                                onMinus = { onAdjustLevel(skill, -1) },
                                onPlus = { onAdjustLevel(skill, 1) },
                                showDivider = index != skills.lastIndex,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillPlanAddSkillPickerRow(
    skill: SkillCatalogSkill,
    localeController: LocaleController,
    plannedLevel: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    showDivider: Boolean,
) {
    val selectedLevel = plannedLevel.coerceIn(0, SkillCatalogConfig.MAX_SKILL_LEVEL)
    val rank = skill.skillTimeConstant.toInt().coerceAtLeast(1)
    val title = stringResource(
        R.string.character_skills_catalog_skill_title_with_rank,
        skill.displayName(localeController),
        rank,
    )
    val rowHorizontalPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val textVerticalPadding = dimensionResource(R.dimen.detail_row_vertical_padding_multi_line)
    val hintSpacing = dimensionResource(R.dimen.detail_row_label_subtitle_spacing)
    val titleTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val trailingGap = dimensionResource(R.dimen.detail_row_trailing_gap)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = rowHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = textVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(hintSpacing),
            ) {
                Text(
                    text = title,
                    color = colorResource(R.color.text_primary),
                    fontSize = titleTextSize,
                    maxLines = 1,
                )
                SkillLevelSegments(
                    trainedLevel = selectedLevel,
                )
            }
            Spacer(modifier = Modifier.width(trailingGap))
            SkillPlanAddSkillLevelStepper(
                minusEnabled = selectedLevel > 0,
                plusEnabled = selectedLevel < SkillCatalogConfig.MAX_SKILL_LEVEL,
                onMinus = onMinus,
                onPlus = onPlus,
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = rowHorizontalPadding),
                thickness = dimensionResource(R.dimen.detail_divider_thickness),
                color = colorResource(R.color.border),
            )
        }
    }
}

@Composable
private fun SkillPlanAddSkillLevelStepper(
    minusEnabled: Boolean,
    plusEnabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    val width = dimensionResource(R.dimen.skill_plan_add_skill_stepper_width)
    val height = dimensionResource(R.dimen.skill_plan_add_skill_stepper_height)
    val dividerInset = dimensionResource(R.dimen.skill_plan_add_skill_stepper_divider_inset)
    val dividerThickness = dimensionResource(R.dimen.skill_plan_add_skill_stepper_divider_thickness)
    val shape = RoundedCornerShape(percent = 50)
    val enabledColor = colorResource(R.color.text_primary)
    val disabledColor = colorResource(R.color.text_caption)

    Row(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(colorResource(R.color.search_field_idle_background)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    enabled = minusEnabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMinus,
                )
                .semantics { role = Role.Button },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.character_skills_skill_plan_level_minus),
                color = if (minusEnabled) enabledColor else disabledColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(
            modifier = Modifier
                .width(dividerThickness)
                .fillMaxHeight()
                .padding(vertical = dividerInset)
                .background(colorResource(R.color.border)),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    enabled = plusEnabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPlus,
                )
                .semantics { role = Role.Button },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.character_skills_skill_plan_level_plus),
                color = if (plusEnabled) enabledColor else disabledColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private object SkillPlanAddSkillSheetConfig {
    const val HEIGHT_FRACTION = 0.94f
}
