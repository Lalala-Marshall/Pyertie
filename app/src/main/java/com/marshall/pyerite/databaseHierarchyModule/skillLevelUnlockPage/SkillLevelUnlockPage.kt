package com.marshall.pyerite.databaseHierarchyModule.skillLevelUnlockPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyModule.util.certificateLevelDrawable
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillUnlockTypeRow
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private sealed class UnlockListEntry(val key: String) {
    data object Title : UnlockListEntry("page:title")

    data class SectionHeader(
        val categoryKey: String,
        val title: String,
        val addTopGap: Boolean,
    ) : UnlockListEntry("header:$categoryKey")

    data class Item(
        val categoryKey: String,
        val typeId: Int,
        val model: BaseLazyColumnItemModel,
        val showDivider: Boolean,
        val indexInSection: Int,
        val sectionItemCount: Int,
    ) : UnlockListEntry("item:$categoryKey:$typeId")

    data object BottomPadding : UnlockListEntry("page:bottom_padding")
}

@Composable
fun SkillLevelUnlockPage(
    skillTypeId: Int,
    level: Int,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
    iconManager: IconManager = koinInject(),
) {
    val unlockTypes by remember(skillTypeId, level) {
        viewModel.typesUnlockedBySkillAtLevel(skillTypeId, level)
    }.collectAsState(initial = emptyList())

    val uncategorizedLabel = stringResource(R.string.category_uncategorized)
    val entries = remember(unlockTypes, uncategorizedLabel, iconManager, navController) {
        buildUnlockEntries(
            unlockTypes = unlockTypes,
            uncategorizedLabel = uncategorizedLabel,
            iconManager = iconManager,
            navController = navController,
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        items(
            items = entries,
            key = { entry -> entry.key },
        ) { entry ->
            UnlockListEntryContent(entry = entry, level = level)
        }
    }
}

@Composable
private fun UnlockListEntryContent(
    entry: UnlockListEntry,
    level: Int,
) {
    val pageTitleTextSize = dimensionResource(R.dimen.list_page_title_text_size).value.sp
    val sectionSubheaderTextSize = dimensionResource(R.dimen.list_section_subheader_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val titleVerticalPadding = dimensionResource(R.dimen.type_detail_page_title_vertical_padding)
    val titleIconSize = dimensionResource(R.dimen.skill_unlock_title_icon_size)
    val titleIconGap = dimensionResource(R.dimen.skill_unlock_title_icon_gap)
    val sectionHeaderBottomPadding = dimensionResource(R.dimen.list_section_header_bottom_padding)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    when (entry) {
        UnlockListEntry.Title -> {
            Row(
                modifier = Modifier.padding(
                    start = titleStartPadding,
                    top = titleVerticalPadding,
                    bottom = titleVerticalPadding,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(titleIconSize),
                    painter = painterResource(certificateLevelDrawable(level)),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
                Spacer(modifier = Modifier.width(titleIconGap))
                Text(
                    text = stringResource(R.string.skill_level, level),
                    fontSize = pageTitleTextSize,
                    fontWeight = FontWeight.Black,
                    color = colorResource(R.color.text_primary),
                )
            }
        }

        is UnlockListEntry.SectionHeader -> {
            Text(
                text = entry.title,
                fontSize = sectionSubheaderTextSize,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.hint_text),
                modifier = Modifier.padding(
                    start = titleStartPadding,
                    bottom = sectionHeaderBottomPadding,
                    top = if (entry.addTopGap) sectionGap else 0.dp,
                ),
            )
        }

        is UnlockListEntry.Item -> {
            UnlockSectionItemRow(
                model = entry.model,
                showDivider = entry.showDivider,
                indexInSection = entry.indexInSection,
                sectionItemCount = entry.sectionItemCount,
            )
        }

        UnlockListEntry.BottomPadding -> {
            Spacer(Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun UnlockSectionItemRow(
    model: BaseLazyColumnItemModel,
    showDivider: Boolean,
    indexInSection: Int,
    sectionItemCount: Int,
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
        BaseLazyColumnItem(model = model, showDivider = showDivider)
    }
}

private fun sectionItemShape(indexInSection: Int, sectionItemCount: Int, corner: Dp): Shape {
    return when {
        sectionItemCount == 1 -> RoundedCornerShape(corner)
        indexInSection == 0 -> RoundedCornerShape(topStart = corner, topEnd = corner)
        indexInSection == sectionItemCount - 1 -> RoundedCornerShape(bottomStart = corner, bottomEnd = corner)
        else -> RectangleShape
    }
}

private fun buildUnlockEntries(
    unlockTypes: List<SkillUnlockTypeRow>,
    uncategorizedLabel: String,
    iconManager: IconManager,
    navController: NavController,
): List<UnlockListEntry> = buildList {
    add(UnlockListEntry.Title)

    if (unlockTypes.isEmpty()) {
        add(UnlockListEntry.BottomPadding)
        return@buildList
    }

    val grouped = unlockTypes.groupBy { row ->
        row.categoryId?.toString() ?: "none"
    }
    var addTopGap = false

    grouped.keys.sortedWith(compareBy { key ->
        val first = grouped[key]?.firstOrNull()
        first?.categoryId ?: Int.MAX_VALUE
    }).forEach { categoryKey ->
        val itemsInCategory = grouped[categoryKey].orEmpty()
        val sectionTitle = itemsInCategory.firstOrNull()?.categoryName?.takeIf { it.isNotBlank() }
            ?: uncategorizedLabel
        addTopGap = appendUnlockSection(
            builder = this,
            categoryKey = categoryKey,
            title = sectionTitle,
            addTopGap = addTopGap,
            types = itemsInCategory,
            iconManager = iconManager,
            navController = navController,
        )
    }

    add(UnlockListEntry.BottomPadding)
}

private fun appendUnlockSection(
    builder: MutableList<UnlockListEntry>,
    categoryKey: String,
    title: String,
    addTopGap: Boolean,
    types: List<SkillUnlockTypeRow>,
    iconManager: IconManager,
    navController: NavController,
): Boolean {
    if (types.isEmpty()) return addTopGap

    builder.add(
        UnlockListEntry.SectionHeader(
            categoryKey = categoryKey,
            title = title,
            addTopGap = addTopGap,
        ),
    )
    types.forEachIndexed { index, type ->
        builder.add(
            UnlockListEntry.Item(
                categoryKey = categoryKey,
                typeId = type.typeId,
                model = BaseLazyColumnItemModel(
                    iconFile = iconManager.getIconFile(type.iconFilename),
                    itemName = type.zhName ?: type.name.orEmpty(),
                    onClick = {
                        navController.navigate(DatabaseRoute.TypeDetail.create(type.typeId))
                    },
                ),
                showDivider = index < types.lastIndex,
                indexInSection = index,
                sectionItemCount = types.size,
            ),
        )
    }
    return true
}
