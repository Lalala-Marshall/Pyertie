package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.viewModel.SkillPlanViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeriteTopBarActionItem
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.search.ListSearchState
import com.marshall.pyerite.ui.golbalComponents.search.PyeriteListSearchHost
import com.marshall.pyerite.ui.golbalComponents.search.SearchNoResultsItem
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CharacterSkillsSkillPlanPage(
    navController: NavController,
    viewModel: SkillPlanViewModel = koinViewModel(),
) {
    val listState = rememberLazyListState()
    val pageTitle = stringResource(R.string.character_skills_skill_plan)
    val onBack = navController.rememberNavigateUpAction()
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val plans by viewModel.plans.collectAsState()
    val searchState = ListSearchState(
        isActive = searchActive,
        query = searchQuery,
    )
    val addContentDescription = stringResource(R.string.character_skills_skill_plan_add)
    val endActions = listOf(
        PyeriteTopBarActionItem(
            onClick = { showAddDialog = true },
            icon = Icons.Default.Add,
            contentDescription = addContentDescription,
        ),
    )

    if (showAddDialog) {
        SkillPlanAddDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addPlan(name)
                showAddDialog = false
            },
        )
    }

    PyeriteListSearchHost(
        searchState = searchState,
        onActivateSearch = { searchActive = true },
        onQueryChange = { searchQuery = it },
        onCancelSearch = {
            searchActive = false
            searchQuery = ""
        },
        listState = listState,
        navTitle = pageTitle,
        modifier = Modifier.fillMaxSize(),
        onBack = onBack,
        endActions = endActions,
        title = {
            PageTitle(text = pageTitle)
        },
    ) { query ->
        val visiblePlans = if (query.isBlank()) {
            plans
        } else {
            plans.filter { it.name.contains(query, ignoreCase = true) }
        }
        when {
            query.isNotBlank() && visiblePlans.isEmpty() -> {
                item(key = "skill_plan_search_no_results") {
                    SearchNoResultsItem()
                }
            }
            visiblePlans.isEmpty() -> {
                item(key = "skill_plan_empty") {
                    BaseContainer(
                        title = null,
                        useSystemBarsPadding = false,
                    ) {
                        BaseLazyColumnItem(
                            model = BaseLazyColumnItemModel(
                                showLeadingIcon = false,
                                itemName = stringResource(R.string.character_skills_skill_plan_empty),
                                showChevron = false,
                                onClick = null,
                            ),
                            showDivider = false,
                        )
                    }
                }
            }
            else -> {
                item(key = "skill_plan_list") {
                    BaseContainer(
                        title = null,
                        useSystemBarsPadding = false,
                    ) {
                        visiblePlans.forEachIndexed { index, plan ->
                            BaseLazyColumnItem(
                                model = BaseLazyColumnItemModel(
                                    showLeadingIcon = false,
                                    itemName = plan.name,
                                    trailingValue = stringResource(
                                        R.string.character_skills_skill_plan_skill_count,
                                        plan.skillCount,
                                    ),
                                    showChevron = true,
                                    onClick = {},
                                ),
                                showDivider = index != visiblePlans.lastIndex,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillPlanAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
) {
    var planName by rememberSaveable { mutableStateOf("") }
    val canConfirm = planName.isNotBlank()
    val nameLabel = stringResource(R.string.character_skills_skill_plan_name)
    val primaryText = colorResource(R.color.text_primary)
    val hintText = colorResource(R.color.hint_text)
    val disabledColor = colorResource(R.color.text_caption)
    val controlBackground = colorResource(R.color.skill_plan_dialog_button_background)
    val cursorColor = colorResource(R.color.hyperlink_text)
    val contentPadding = dimensionResource(R.dimen.skill_plan_dialog_content_padding)
    val labelFieldGap = dimensionResource(R.dimen.skill_plan_dialog_label_field_gap)
    val buttonGap = dimensionResource(R.dimen.skill_plan_dialog_button_gap)
    val fieldHeight = dimensionResource(R.dimen.skill_plan_dialog_field_height)
    val fieldCorner = dimensionResource(R.dimen.skill_plan_dialog_field_corner)
    val fieldHorizontalPadding =
        dimensionResource(R.dimen.skill_plan_dialog_field_horizontal_padding)
    val buttonCorner = dimensionResource(R.dimen.skill_plan_dialog_button_corner)
    val buttonHorizontalPadding =
        dimensionResource(R.dimen.skill_plan_dialog_button_horizontal_padding)
    val dialogCorner = dimensionResource(R.dimen.skill_plan_dialog_corner)
    val fieldShape = RoundedCornerShape(fieldCorner)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(SkillPlanAddDialogConfig.WIDTH_FRACTION),
            shape = RoundedCornerShape(dialogCorner),
            color = colorResource(R.color.main_background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
            ) {
                Text(
                    text = stringResource(R.string.character_skills_skill_plan_dialog_title),
                    color = primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = nameLabel,
                    color = primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = contentPadding),
                )
                BasicTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = labelFieldGap)
                        .height(fieldHeight)
                        .clip(fieldShape)
                        .background(controlBackground)
                        .padding(horizontal = fieldHorizontalPadding),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = primaryText,
                        fontSize = 14.sp,
                    ),
                    cursorBrush = SolidColor(cursorColor),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (planName.isEmpty()) {
                                Text(
                                    text = nameLabel,
                                    color = hintText,
                                    fontSize = 14.sp,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(buttonGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkillPlanDialogActionButton(
                        label = stringResource(R.string.character_skills_skill_plan_cancel),
                        labelColor = primaryText,
                        backgroundColor = controlBackground,
                        corner = buttonCorner,
                        horizontalPadding = buttonHorizontalPadding,
                        enabled = true,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    SkillPlanDialogActionButton(
                        label = stringResource(R.string.character_skills_skill_plan_confirm_add),
                        labelColor = if (canConfirm) primaryText else disabledColor,
                        backgroundColor = controlBackground,
                        corner = buttonCorner,
                        horizontalPadding = buttonHorizontalPadding,
                        enabled = canConfirm,
                        onClick = {
                            val trimmed = planName.trim()
                            if (trimmed.isNotEmpty()) {
                                planName = ""
                                onConfirm(trimmed)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillPlanDialogActionButton(
    label: String,
    labelColor: Color,
    backgroundColor: Color,
    corner: Dp,
    horizontalPadding: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(corner)
    val buttonHeight = dimensionResource(R.dimen.top_bar_back_button_size)

    Box(
        modifier = modifier
            .height(buttonHeight)
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .semantics { role = Role.Button }
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = labelColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

private object SkillPlanAddDialogConfig {
    /** Wider than platform default while keeping content padding to the edges. */
    const val WIDTH_FRACTION = 0.82f
}
