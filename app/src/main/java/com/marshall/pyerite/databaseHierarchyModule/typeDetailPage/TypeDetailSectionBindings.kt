package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

internal enum class DogmaCategory(val categoryId: Int) {
    FITTING(1),
    SHIELD(2),
    ARMOR(3),
    STRUCTURE(4),
    CAPACITOR(5),
    TARGETING(6),
    SKILLS(8),
    DRONES(10),
    NAVIGATION(17),
    EWAR_RESISTANCES(36),
    BONUSES(37),
    HANGARS_BAYS(40),
}

internal fun List<TypeAttributeDetail>.inDogmaCategory(category: DogmaCategory): List<TypeAttributeDetail> =
    filter { it.categoryId == category.categoryId }

@Composable
private fun rememberDogmaCategoryAttributes(
    typeId: Int,
    category: DogmaCategory,
    viewModel: DatabaseViewModel,
): List<TypeAttributeDetail> {
    val attributes by remember(typeId) { viewModel.typeAttributes(typeId) }
        .collectAsState(initial = emptyList())
    return remember(attributes, category) { attributes.inDogmaCategory(category) }
}

@Composable
internal fun TypeSummarySectionItem(
    typeId: Int,
    entity: TypeEntity,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val traits by remember(typeId) { viewModel.typeTraits(typeId) }
        .collectAsState(initial = emptyList())
    TypeSummarySection(entity = entity, traits = traits)
}

@Composable
internal fun TypeDetailBaseInfoSectionItem(
    entity: TypeEntity,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val dogmaAttributes by remember {
        viewModel.dogmaAttributes(TypeDetailBaseInfoSectionDogmaNames)
    }.collectAsState(initial = emptyList())
    TypeDetailBaseInfoSection(entity = entity, dogmaAttributes = dogmaAttributes)
}

@Composable
internal fun TypeDetailVariantsSectionItem(
    typeId: Int,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val variantCount by remember(typeId) { viewModel.variantCount(typeId) }
        .collectAsState(initial = 0)
    TypeDetailVariantsSection(
        variantCount = variantCount,
        onBrowseVariants = {
            navController.navigate(DatabaseRoute.TypeVariants.create(typeId))
        },
    )
}

@Composable
internal fun TypeDetailFittingSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailFittingSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.FITTING, viewModel))
}

@Composable
internal fun TypeDetailShieldSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailShieldSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.SHIELD, viewModel))
}

@Composable
internal fun TypeDetailArmorSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailArmorSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.ARMOR, viewModel))
}

@Composable
internal fun TypeDetailStructureSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailStructureSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.STRUCTURE, viewModel))
}

@Composable
internal fun TypeDetailCapacitorSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailCapacitorSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.CAPACITOR, viewModel))
}

@Composable
internal fun TypeDetailTargetingSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailTargetingSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.TARGETING, viewModel))
}

@Composable
internal fun TypeDetailNavigationSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailNavigationSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.NAVIGATION, viewModel))
}

@Composable
internal fun TypeDetailDronesSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailDronesSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.DRONES, viewModel))
}

@Composable
internal fun TypeDetailHangarsBaysSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailHangarsBaysSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.HANGARS_BAYS, viewModel))
}

@Composable
internal fun TypeDetailEwarResistancesSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailEwarResistancesSection(
        rememberDogmaCategoryAttributes(typeId, DogmaCategory.EWAR_RESISTANCES, viewModel),
    )
}

@Composable
internal fun TypeDetailBonusesSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    TypeDetailBonusesSection(rememberDogmaCategoryAttributes(typeId, DogmaCategory.BONUSES, viewModel))
}

@Composable
internal fun TypeDetailMiscSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val attributes by remember(typeId) { viewModel.typeAttributes(typeId) }
        .collectAsState(initial = emptyList())
    val compatibleGroups by remember(typeId) { viewModel.compatibleGroups(typeId) }
        .collectAsState(initial = emptyList())
    val skillMiscRows by remember(typeId) { viewModel.skillMiscRows(typeId) }
        .collectAsState(initial = emptyList())
    TypeDetailMiscSection(
        attributes = attributes,
        compatibleGroups = compatibleGroups,
        skillMiscRows = skillMiscRows,
    )
}

@Composable
internal fun TypeDetailSkillLevelDetailSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val skillLevelSpRows by remember(typeId) { viewModel.skillLevelSpRows(typeId) }
        .collectAsState(initial = emptyList())
    TypeDetailSkillLevelDetailSection(rows = skillLevelSpRows)
}

@Composable
internal fun TypeDetailSkillLevelAppliesSectionItem(
    typeId: Int,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val skillUnlockLevels by remember(typeId) { viewModel.skillUnlockLevels(typeId) }
        .collectAsState(initial = emptyList())
    TypeDetailSkillLevelAppliesSection(
        levels = skillUnlockLevels,
        onLevelClick = { level ->
            navController.navigate(DatabaseRoute.SkillLevelUnlock.create(typeId, level))
        },
    )
}

@Composable
internal fun TypeDetailSkillsSectionItem(
    typeId: Int,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val skillRequirements by remember(typeId) { viewModel.skillRequirements(typeId) }
        .collectAsState(initial = emptyList())
    TypeDetailSkillsSection(
        skillRequirements = skillRequirements,
        attributes = rememberDogmaCategoryAttributes(typeId, DogmaCategory.SKILLS, viewModel),
        onSkillClick = { skillTypeId ->
            navController.navigate(DatabaseRoute.TypeDetail.create(skillTypeId))
        },
    )
}

@Composable
internal fun TypeDetailManufacturingSectionItem(
    typeId: Int,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val products by remember(typeId) { viewModel.blueprintManufacturingProducts(typeId) }
        .collectAsState(initial = emptyList())
    val materials by remember(typeId) { viewModel.blueprintManufacturingMaterials(typeId) }
        .collectAsState(initial = emptyList())
    val skills by remember(typeId) { viewModel.blueprintManufacturingSkills(typeId) }
        .collectAsState(initial = emptyList())
    val manufacturingTime by remember(typeId) { viewModel.blueprintManufacturingTime(typeId) }
        .collectAsState(initial = null)
    TypeDetailManufacturingSection(
        typeId = typeId,
        products = products,
        materials = materials,
        skills = skills,
        manufacturingTimeSeconds = manufacturingTime,
        navController = navController,
    )
}

@Composable
internal fun TypeDetailMaterialResearchSectionItem(
    typeId: Int,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val researchMaterialTime by remember(typeId) { viewModel.blueprintResearchMaterialTime(typeId) }
        .collectAsState(initial = null)
    TypeDetailMaterialResearchSection(
        typeId = typeId,
        researchMaterialTimeSeconds = researchMaterialTime,
    )
}

@Composable
internal fun TypeDetailIndustrySectionItem(
    typeId: Int,
    navController: NavController,
    viewModel: DatabaseViewModel = koinViewModel(),
) {
    val applicableBlueprintCount by remember(typeId) { viewModel.applicableBlueprintCount(typeId) }
        .collectAsState(initial = null)
    val refiningSourceCount by remember(typeId) { viewModel.refiningSourceCount(typeId) }
        .collectAsState(initial = null)
    val blueprints by remember(typeId) { viewModel.blueprintsForProduct(typeId) }
        .collectAsState(initial = emptyList())
    val refiningOutputSummary by remember(typeId) { viewModel.refiningOutputSummary(typeId) }
        .collectAsState(initial = null)
    val refiningOutputs by remember(typeId) { viewModel.refiningOutputs(typeId) }
        .collectAsState(initial = emptyList())
    val refiningSources by remember(typeId) { viewModel.refiningSources(typeId) }
        .collectAsState(initial = emptyList())
    val metaGroups by viewModel.metaGroups.collectAsState(initial = emptyList())
    TypeDetailIndustrySection(
        typeId = typeId,
        applicableBlueprintCount = applicableBlueprintCount,
        refiningSourceCount = refiningSourceCount,
        blueprints = blueprints,
        refiningOutputSummary = refiningOutputSummary,
        refiningOutputs = refiningOutputs,
        refiningSources = refiningSources,
        metaGroups = metaGroups,
        navController = navController,
    )
}

