package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import org.koin.androidx.compose.koinViewModel

private val SectionGap = 16.dp
private val BottomPadding = 24.dp

private enum class TypeDetailSlot {
    Title,
    Summary,
    Market,
    BaseInfo,
    Variants,
    Fitting,
    Shield,
    Armor,
    Structure,
    Capacitor,
    Targeting,
    Navigation,
    Drones,
    HangarsBays,
    EwarResistances,
    Misc,
    Bonuses,
    Skills,
    Industry,
}

@Composable
fun TypeDetailPage(
    typeId: Int,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val type by remember(typeId) { viewModel.typeDetail(typeId) }
        .collectAsState(initial = null)

    type?.let { entity ->
        val visibleSlots = rememberVisibleTypeDetailSlots(typeId, viewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            items(
                items = visibleSlots,
                key = { slot -> slot.name },
            ) { slot ->
                TypeDetailSlotContent(
                    slot = slot,
                    typeId = typeId,
                    entity = entity,
                    navController = navController,
                )
                TypeDetailSlotTrailingSpacer(
                    slot = slot,
                    isLastSlot = slot == visibleSlots.last(),
                )
            }
        }
    }
}

@Composable
private fun rememberVisibleTypeDetailSlots(
    typeId: Int,
    viewModel: DatabaseViewModel,
): List<TypeDetailSlot> {
    val attributes by remember(typeId) { viewModel.typeAttributes(typeId) }
        .collectAsState(initial = emptyList())
    val applicableBlueprintCount by remember(typeId) { viewModel.applicableBlueprintCount(typeId) }
        .collectAsState(initial = null)
    val refiningSourceCount by remember(typeId) { viewModel.refiningSourceCount(typeId) }
        .collectAsState(initial = null)
    val blueprints by remember(typeId) { viewModel.blueprintsForProduct(typeId) }
        .collectAsState(initial = emptyList())
    val refiningOutputSummary by remember(typeId) { viewModel.refiningOutputSummary(typeId) }
        .collectAsState(initial = null)
    val skillRequirements by remember(typeId) { viewModel.skillRequirements(typeId) }
        .collectAsState(initial = emptyList())
    val variantCount by remember(typeId) { viewModel.variantCount(typeId) }
        .collectAsState(initial = 0)
    val skillMiscRows by remember(typeId) { viewModel.skillMiscRows(typeId) }
        .collectAsState(initial = emptyList())

    return remember(
        attributes,
        applicableBlueprintCount,
        refiningSourceCount,
        blueprints,
        refiningOutputSummary,
        skillRequirements,
        variantCount,
        skillMiscRows,
    ) {
        buildList {
            add(TypeDetailSlot.Title)
            add(TypeDetailSlot.Summary)
            add(TypeDetailSlot.Market)
            add(TypeDetailSlot.BaseInfo)
            if (variantCount > 1) {
                add(TypeDetailSlot.Variants)
            }
            if (hasFittingSectionContent(attributes.inDogmaCategory(DogmaCategory.FITTING))) {
                add(TypeDetailSlot.Fitting)
            }
            if (hasDefenseSectionContent(attributes.inDogmaCategory(DogmaCategory.SHIELD))) {
                add(TypeDetailSlot.Shield)
            }
            if (hasDefenseSectionContent(attributes.inDogmaCategory(DogmaCategory.ARMOR))) {
                add(TypeDetailSlot.Armor)
            }
            if (hasDefenseSectionContent(attributes.inDogmaCategory(DogmaCategory.STRUCTURE))) {
                add(TypeDetailSlot.Structure)
            }
            if (hasDisplayableDogmaRows(attributes.inDogmaCategory(DogmaCategory.CAPACITOR))) {
                add(TypeDetailSlot.Capacitor)
            }
            if (hasDisplayableDogmaRows(attributes.inDogmaCategory(DogmaCategory.TARGETING))) {
                add(TypeDetailSlot.Targeting)
            }
            if (hasNavigationSectionContent(attributes.inDogmaCategory(DogmaCategory.NAVIGATION))) {
                add(TypeDetailSlot.Navigation)
            }
            if (hasDisplayableDogmaRows(attributes.inDogmaCategory(DogmaCategory.DRONES))) {
                add(TypeDetailSlot.Drones)
            }
            if (hasDisplayableDogmaRows(attributes.inDogmaCategory(DogmaCategory.HANGARS_BAYS))) {
                add(TypeDetailSlot.HangarsBays)
            }
            if (hasDisplayableDogmaRows(attributes.inDogmaCategory(DogmaCategory.EWAR_RESISTANCES))) {
                add(TypeDetailSlot.EwarResistances)
            }
            if (hasMiscSectionContent(attributes, skillMiscRows = skillMiscRows)) {
                add(TypeDetailSlot.Misc)
            }
            if (hasDisplayableDogmaRows(attributes.inDogmaCategory(DogmaCategory.BONUSES))) {
                add(TypeDetailSlot.Bonuses)
            }
            if (hasSkillsSectionContent(
                    skillRequirements,
                    attributes.inDogmaCategory(DogmaCategory.SKILLS),
                )
            ) {
                add(TypeDetailSlot.Skills)
            }
            if (hasIndustrySectionContent(
                    blueprints,
                    refiningOutputSummary,
                    applicableBlueprintCount,
                    refiningSourceCount,
                )
            ) {
                add(TypeDetailSlot.Industry)
            }
        }
    }
}

@Composable
private fun TypeDetailSlotContent(
    slot: TypeDetailSlot,
    typeId: Int,
    entity: TypeEntity,
    navController: NavController,
) {
    when (slot) {
        TypeDetailSlot.Title -> {
            Text(
                text = stringResource(R.string.type_info),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp),
            )
        }

        TypeDetailSlot.Summary -> TypeSummarySectionItem(typeId = typeId, entity = entity)
        TypeDetailSlot.Market -> TypeDetailMarketSection()
        TypeDetailSlot.BaseInfo -> TypeDetailBaseInfoSectionItem(entity = entity)
        TypeDetailSlot.Variants -> TypeDetailVariantsSectionItem(typeId = typeId, navController = navController)
        TypeDetailSlot.Fitting -> TypeDetailFittingSectionItem(typeId)
        TypeDetailSlot.Shield -> TypeDetailShieldSectionItem(typeId)
        TypeDetailSlot.Armor -> TypeDetailArmorSectionItem(typeId)
        TypeDetailSlot.Structure -> TypeDetailStructureSectionItem(typeId)
        TypeDetailSlot.Capacitor -> TypeDetailCapacitorSectionItem(typeId)
        TypeDetailSlot.Targeting -> TypeDetailTargetingSectionItem(typeId)
        TypeDetailSlot.Navigation -> TypeDetailNavigationSectionItem(typeId)
        TypeDetailSlot.Drones -> TypeDetailDronesSectionItem(typeId)
        TypeDetailSlot.HangarsBays -> TypeDetailHangarsBaysSectionItem(typeId)
        TypeDetailSlot.EwarResistances -> TypeDetailEwarResistancesSectionItem(typeId)
        TypeDetailSlot.Misc -> TypeDetailMiscSectionItem(typeId)
        TypeDetailSlot.Bonuses -> TypeDetailBonusesSectionItem(typeId)
        TypeDetailSlot.Skills -> TypeDetailSkillsSectionItem(typeId)
        TypeDetailSlot.Industry -> TypeDetailIndustrySectionItem(typeId)
    }
}

@Composable
private fun TypeDetailSlotTrailingSpacer(
    slot: TypeDetailSlot,
    isLastSlot: Boolean,
) {
    when (slot) {
        TypeDetailSlot.Title -> Unit
        else -> Spacer(
            Modifier.height(
                if (isLastSlot) BottomPadding else SectionGap,
            ),
        )
    }
}
