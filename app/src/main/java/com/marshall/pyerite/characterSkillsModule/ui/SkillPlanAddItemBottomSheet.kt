package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.marshall.pyerite.characterSkillsModule.data.SkillPlanItemCatalogLoader
import com.marshall.pyerite.characterSkillsModule.data.SkillPrerequisiteResolver
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanItemPickerConfig
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity
import com.marshall.pyerite.sdeModule.room.catalog.GroupEntity
import com.marshall.pyerite.sdeModule.room.type.TypeEntity
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.search.SearchNoResultsItem
import com.marshall.pyerite.ui.golbalComponents.search.SearchResultsTruncatedItem
import com.marshall.pyerite.ui.golbalComponents.topBarActionSurface
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Bottom sheet to pick an item for a skill plan: category → group → type for
 * published types that declare skill requirements (including ones already trained).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SkillPlanAddItemBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (Map<Int, Int>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val localeController = koinInject<LocaleController>()
    val catalogLoader = koinInject<SkillPlanItemCatalogLoader>()
    val prerequisiteResolver = koinInject<SkillPrerequisiteResolver>()
    val coroutineScope = rememberCoroutineScope()

    var selectedCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedCategoryTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGroupId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedGroupTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }

    var categories by remember { mutableStateOf<List<CategoryEntity>>(emptyList()) }
    var groups by remember { mutableStateOf<List<GroupEntity>>(emptyList()) }
    var types by remember { mutableStateOf<List<TypeEntity>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<TypeEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        categories = catalogLoader.loadCategories()
        isLoading = false
    }

    LaunchedEffect(selectedCategoryId) {
        val categoryId = selectedCategoryId
        if (categoryId == null) {
            groups = emptyList()
            return@LaunchedEffect
        }
        isLoading = true
        groups = catalogLoader.loadGroups(categoryId)
        isLoading = false
    }

    LaunchedEffect(selectedGroupId) {
        val groupId = selectedGroupId
        if (groupId == null) {
            types = emptyList()
            return@LaunchedEffect
        }
        isLoading = true
        types = catalogLoader.loadTypes(groupId)
        isLoading = false
    }

    LaunchedEffect(searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            searchResults = emptyList()
            return@LaunchedEffect
        }
        isLoading = true
        searchResults = catalogLoader.searchTypes(query)
        isLoading = false
    }

    fun confirmType(typeId: Int) {
        if (isConfirming) return
        isConfirming = true
        coroutineScope.launch {
            val required = prerequisiteResolver.requiredLevels(typeId)
            if (required.isEmpty()) {
                onDismiss()
                return@launch
            }
            val capped = required.mapValues { (_, level) ->
                level.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
            }
            onConfirm(capped)
        }
    }

    val sheetCorner = dimensionResource(R.dimen.skill_plan_add_skill_sheet_corner)
    val sheetBackground = colorResource(R.color.search_field_idle_background)
    val sheetHorizontalPadding =
        dimensionResource(R.dimen.skill_plan_add_skill_sheet_horizontal_padding)
    val title = when {
        selectedGroupTitle != null -> selectedGroupTitle!!
        selectedCategoryTitle != null -> selectedCategoryTitle!!
        else -> stringResource(R.string.character_skills_skill_plan_add_item)
    }
    val showBack = selectedCategoryId != null || selectedGroupId != null
    val searching = searchQuery.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
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
                SkillPlanAddItemSheetHeader(
                    title = title,
                    showBack = showBack && !searching,
                    onBack = {
                        when {
                            selectedGroupId != null -> {
                                selectedGroupId = null
                                selectedGroupTitle = null
                            }
                            selectedCategoryId != null -> {
                                selectedCategoryId = null
                                selectedCategoryTitle = null
                            }
                        }
                    },
                    onDone = onDismiss,
                )
                SkillPlanAddItemSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(
                        top = dimensionResource(R.dimen.skill_plan_add_skill_search_top_gap),
                        bottom = dimensionResource(R.dimen.skill_plan_add_skill_search_bottom_gap),
                    ),
                )
            }

            when {
                searching -> {
                    SkillPlanAddItemTypesList(
                        types = searchResults,
                        localeController = localeController,
                        isLoading = isLoading,
                        showTruncatedHint = searchResults.size >=
                            SkillPlanItemPickerConfig.SEARCH_RESULT_LIMIT,
                        onTypeClick = ::confirmType,
                    )
                }
                selectedGroupId != null -> {
                    SkillPlanAddItemTypesList(
                        types = types,
                        localeController = localeController,
                        isLoading = isLoading,
                        showTruncatedHint = false,
                        onTypeClick = ::confirmType,
                    )
                }
                selectedCategoryId != null -> {
                    SkillPlanAddItemGroupsList(
                        groups = groups,
                        localeController = localeController,
                        isLoading = isLoading,
                        onGroupClick = { group ->
                            selectedGroupId = group.id
                            selectedGroupTitle = group.displayName(localeController)
                            searchQuery = ""
                        },
                    )
                }
                else -> {
                    SkillPlanAddItemCategoriesList(
                        categories = categories,
                        localeController = localeController,
                        isLoading = isLoading,
                        onCategoryClick = { category ->
                            selectedCategoryId = category.id
                            selectedCategoryTitle = category.displayName(localeController)
                            searchQuery = ""
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillPlanAddItemSheetHeader(
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
private fun SkillPlanAddItemSearchField(
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
private fun SkillPlanAddItemCategoriesList(
    categories: List<CategoryEntity>,
    localeController: LocaleController,
    isLoading: Boolean,
    onCategoryClick: (CategoryEntity) -> Unit,
) {
    if (!isLoading && categories.isEmpty()) {
        SearchNoResultsItem()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "skill_plan_add_item_categories") {
            BaseContainer(
                title = null,
                useSystemBarsPadding = false,
            ) {
                categories.forEachIndexed { index, category ->
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            iconFileName = category.iconFilename,
                            showLeadingIcon = !category.iconFilename.isNullOrBlank(),
                            itemName = category.displayName(localeController),
                            showChevron = true,
                            onClick = { onCategoryClick(category) },
                        ),
                        showDivider = index != categories.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillPlanAddItemGroupsList(
    groups: List<GroupEntity>,
    localeController: LocaleController,
    isLoading: Boolean,
    onGroupClick: (GroupEntity) -> Unit,
) {
    if (!isLoading && groups.isEmpty()) {
        SearchNoResultsItem()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "skill_plan_add_item_groups") {
            BaseContainer(
                title = null,
                useSystemBarsPadding = false,
            ) {
                groups.forEachIndexed { index, group ->
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            iconFileName = group.iconFilename,
                            showLeadingIcon = !group.iconFilename.isNullOrBlank(),
                            itemName = group.displayName(localeController),
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
private fun SkillPlanAddItemTypesList(
    types: List<TypeEntity>,
    localeController: LocaleController,
    isLoading: Boolean,
    showTruncatedHint: Boolean,
    onTypeClick: (Int) -> Unit,
) {
    if (!isLoading && types.isEmpty()) {
        SearchNoResultsItem()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        if (showTruncatedHint) {
            item(key = "skill_plan_add_item_search_truncated") {
                SearchResultsTruncatedItem(
                    message = stringResource(R.string.search_results_truncated),
                )
            }
        }
        item(key = "skill_plan_add_item_types") {
            BaseContainer(
                title = null,
                useSystemBarsPadding = false,
            ) {
                types.forEachIndexed { index, type ->
                    BaseLazyColumnItem(
                        model = BaseLazyColumnItemModel(
                            iconFileName = type.iconFilename,
                            showLeadingIcon = !type.iconFilename.isNullOrBlank(),
                            iconOnLightPlate = true,
                            itemName = type.displayName(localeController),
                            showChevron = false,
                            onClick = { onTypeClick(type.id) },
                        ),
                        showDivider = index != types.lastIndex,
                    )
                }
            }
        }
    }
}
