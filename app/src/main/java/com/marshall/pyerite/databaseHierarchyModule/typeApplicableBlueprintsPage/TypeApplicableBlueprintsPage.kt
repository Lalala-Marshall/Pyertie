package com.marshall.pyerite.databaseHierarchyModule.typeApplicableBlueprintsPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private sealed class ApplicableBlueprintListEntry(val key: String) {
    data object Title : ApplicableBlueprintListEntry("page:title")

    data class Item(
        val blueprintTypeId: Int,
        val model: BaseLazyColumnItemModel,
        val showDivider: Boolean,
        val indexInSection: Int,
        val sectionItemCount: Int,
    ) : ApplicableBlueprintListEntry("item:$blueprintTypeId")

    data object BottomPadding : ApplicableBlueprintListEntry("page:bottom_padding")
}

@Composable
fun TypeApplicableBlueprintsPage(
    typeId: Int,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
    iconManager: IconManager = koinInject(),
) {
    val blueprints by remember(typeId) { viewModel.applicableBlueprints(typeId) }
        .collectAsState(initial = emptyList())

    val entries = remember(blueprints, iconManager, navController) {
        buildApplicableBlueprintEntries(
            blueprints = blueprints,
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
            ApplicableBlueprintListEntryContent(entry = entry)
        }
    }
}

@Composable
private fun ApplicableBlueprintListEntryContent(entry: ApplicableBlueprintListEntry) {
    val pageTitleTextSize = dimensionResource(R.dimen.list_page_title_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val titleVerticalPadding = dimensionResource(R.dimen.type_detail_page_title_vertical_padding)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    when (entry) {
        ApplicableBlueprintListEntry.Title -> {
            Text(
                text = stringResource(R.string.type_detail_applicable_to),
                fontSize = pageTitleTextSize,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(
                    start = titleStartPadding,
                    top = titleVerticalPadding,
                    bottom = titleVerticalPadding,
                ),
            )
        }

        is ApplicableBlueprintListEntry.Item -> {
            ApplicableBlueprintItemRow(
                model = entry.model,
                showDivider = entry.showDivider,
                indexInSection = entry.indexInSection,
                sectionItemCount = entry.sectionItemCount,
            )
        }

        ApplicableBlueprintListEntry.BottomPadding -> {
            Spacer(Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun ApplicableBlueprintItemRow(
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

private fun buildApplicableBlueprintEntries(
    blueprints: List<TypeBlueprintDetail>,
    iconManager: IconManager,
    navController: NavController,
): List<ApplicableBlueprintListEntry> = buildList {
    add(ApplicableBlueprintListEntry.Title)

    if (blueprints.isEmpty()) {
        add(ApplicableBlueprintListEntry.BottomPadding)
        return@buildList
    }

    blueprints.forEachIndexed { index, blueprint ->
        add(
            ApplicableBlueprintListEntry.Item(
                blueprintTypeId = blueprint.typeId,
                model = BaseLazyColumnItemModel(
                    iconFile = blueprint.iconFilename?.let { iconManager.getIconFile(it) },
                    itemName = blueprint.name ?: "",
                    onClick = {
                        navController.navigate(DatabaseRoute.TypeDetail.create(blueprint.typeId))
                    },
                ),
                showDivider = index < blueprints.lastIndex,
                indexInSection = index,
                sectionItemCount = blueprints.size,
            ),
        )
    }

    add(ApplicableBlueprintListEntry.BottomPadding)
}
