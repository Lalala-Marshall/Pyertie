package com.marshall.pyerite.databaseHierarchyModule.viewModel

import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.MetaGroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintCopyDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintInventionMaterial
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintInventionProduct
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintInventionSkill
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingMaterial
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingProduct
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingSkill
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeTraitDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillRequirement
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeApplicableBlueprintCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeCompatibleGroupDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputItem
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceItem
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillLevelSpRow
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillUnlockTypeRow
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeSkillMiscRow
import com.marshall.pyerite.databaseHierarchyModule.util.DogmaAttributeFormatting
import com.marshall.pyerite.databaseHierarchyModule.util.SkillSpCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DatabaseRepository(
    private val roomProvider: RoomProvider,
    private val localeController: LocaleController,
) {

    companion object {
        /** EVE SDE anchor used to discover skill metadata attributes from the DB. */
        private const val SKILL_MISC_ANCHOR_NAME = "skillTimeConstant"
        private const val SKILL_LEVEL_DOGMA_NAME = "skillLevel"
    }

    private val categoryDao get() = roomProvider.getDatabase().categoryDao()
    private val metaGroupDao get() = roomProvider.getDatabase().metaGroupDao()
    private val groupDao get() = roomProvider.getDatabase().groupDao()
    private val typeDao get() = roomProvider.getDatabase().typeDao()
    private val traitDao get() = roomProvider.getDatabase().traitDao()

    fun getCategories(): Flow<List<CategoryEntity>> = flow {
        emit(categoryDao.getCategories())
    }.flowOn(Dispatchers.IO)

    fun getGroups(categoryId: Int): Flow<List<GroupEntity>> = flow {
        emit(groupDao.getGroupsByCategory(categoryId))
    }.flowOn(Dispatchers.IO)
    fun getTypes(groupId: Int): Flow<List<TypeEntity>> = flow {
        emit(typeDao.getTypesByGroup(groupId))
    }.flowOn(Dispatchers.IO)

    fun getType(typeId: Int): Flow<TypeEntity?> = flow {
        emit(typeDao.getTypeById(typeId))
    }.flowOn(Dispatchers.IO)

    fun getMetaGroups(): Flow<List<MetaGroupEntity>> = flow {
        emit(metaGroupDao.getAllMetaGroups())
    }.flowOn(Dispatchers.IO)

    fun getTypeAttributes(typeId: Int): Flow<List<TypeAttributeDetail>> = flow {
        emit(typeDao.getTypeAttributeDetails(typeId))
    }.flowOn(Dispatchers.IO)

    fun getDogmaAttributesByNames(names: List<String>): Flow<List<DogmaAttributeEntity>> = flow {
        emit(typeDao.getDogmaAttributesByNames(names))
    }.flowOn(Dispatchers.IO)

    fun getTypeTraits(typeId: Int): Flow<List<TypeTraitDetail>> = flow {
        emit(traitDao.getTraitsForType(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintsForProduct(typeId: Int): Flow<List<TypeBlueprintDetail>> = flow {
        emit(typeDao.getBlueprintsForProduct(typeId))
    }.flowOn(Dispatchers.IO)

    fun getRefiningOutputSummary(typeId: Int): Flow<TypeRefiningOutputSummary?> = flow {
        emit(typeDao.getRefiningOutputSummary(typeId))
    }.flowOn(Dispatchers.IO)

    fun getApplicableBlueprintCount(typeId: Int): Flow<TypeApplicableBlueprintCount?> = flow {
        emit(typeDao.getApplicableBlueprintCount(typeId))
    }.flowOn(Dispatchers.IO)

    fun getRefiningSourceCount(typeId: Int): Flow<TypeRefiningSourceCount?> = flow {
        emit(typeDao.getRefiningSourceCount(typeId))
    }.flowOn(Dispatchers.IO)

    fun getRefiningOutputs(typeId: Int): Flow<List<TypeRefiningOutputItem>> = flow {
        emit(typeDao.getRefiningOutputs(typeId))
    }.flowOn(Dispatchers.IO)

    fun getRefiningSources(typeId: Int): Flow<List<TypeRefiningSourceItem>> = flow {
        emit(typeDao.getRefiningSources(typeId))
    }.flowOn(Dispatchers.IO)

    fun getApplicableBlueprints(typeId: Int): Flow<List<TypeBlueprintDetail>> = flow {
        emit(typeDao.getApplicableBlueprints(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintManufacturingProducts(typeId: Int): Flow<List<BlueprintManufacturingProduct>> = flow {
        emit(typeDao.getBlueprintManufacturingProducts(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintManufacturingMaterials(typeId: Int): Flow<List<BlueprintManufacturingMaterial>> = flow {
        emit(typeDao.getBlueprintManufacturingMaterials(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintManufacturingSkills(typeId: Int): Flow<List<BlueprintManufacturingSkill>> = flow {
        emit(typeDao.getBlueprintManufacturingSkills(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintManufacturingTime(typeId: Int): Flow<Int?> = flow {
        emit(typeDao.getBlueprintManufacturingTime(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintResearchMaterialTime(typeId: Int): Flow<Int?> = flow {
        emit(typeDao.getBlueprintResearchMaterialTime(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintResearchTimeTime(typeId: Int): Flow<Int?> = flow {
        emit(typeDao.getBlueprintResearchTimeTime(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintCopyDetail(typeId: Int): Flow<BlueprintCopyDetail?> = flow {
        emit(typeDao.getBlueprintCopyDetail(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintInventionProducts(typeId: Int): Flow<List<BlueprintInventionProduct>> = flow {
        emit(typeDao.getBlueprintInventionProducts(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintInventionMaterials(typeId: Int): Flow<List<BlueprintInventionMaterial>> = flow {
        emit(typeDao.getBlueprintInventionMaterials(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintInventionSkills(typeId: Int): Flow<List<BlueprintInventionSkill>> = flow {
        emit(typeDao.getBlueprintInventionSkills(typeId))
    }.flowOn(Dispatchers.IO)

    fun getBlueprintInventionTime(typeId: Int): Flow<Int?> = flow {
        emit(typeDao.getBlueprintInventionTime(typeId))
    }.flowOn(Dispatchers.IO)

    fun getCompatibleGroups(typeId: Int): Flow<List<TypeCompatibleGroupDetail>> = flow {
        emit(resolveCompatibleGroups(typeId))
    }.flowOn(Dispatchers.IO)

    private suspend fun resolveCompatibleGroups(typeId: Int): List<TypeCompatibleGroupDetail> {
        val refs = typeDao.getCompatibleGroupRefs(typeId)
        if (refs.isEmpty()) return emptyList()
        val groupsById = groupDao.getGroupsByIds(refs.map { it.groupId }.distinct())
            .associateBy { it.id }
        return refs.mapNotNull { ref ->
            val group = groupsById[ref.groupId] ?: return@mapNotNull null
            TypeCompatibleGroupDetail(
                attributeId = ref.attributeId,
                attributeName = ref.attributeName,
                attributeDisplayName = ref.attributeDisplayName,
                attributeIconFilename = ref.attributeIconFilename,
                groupId = ref.groupId,
                zhName = group.zhName,
                enName = group.enName,
                name = group.name,
                groupIconFilename = group.iconFilename,
            )
        }
    }

    fun getSkillRequirements(typeId: Int): Flow<List<SkillRequirement>> = flow {
        emit(resolveSkillRequirements(typeId))
    }.flowOn(Dispatchers.IO)

    fun getSkillMiscRows(typeId: Int): Flow<List<TypeSkillMiscRow>> = flow {
        emit(resolveSkillMiscRows(typeId))
    }.flowOn(Dispatchers.IO)

    fun getSkillLevelSpRows(typeId: Int): Flow<List<SkillLevelSpRow>> = flow {
        emit(resolveSkillLevelSpRows(typeId))
    }.flowOn(Dispatchers.IO)

    fun getSkillUnlockLevels(typeId: Int): Flow<List<Int>> = flow {
        emit(resolveSkillUnlockLevels(typeId))
    }.flowOn(Dispatchers.IO)

    fun getTypesUnlockedBySkillAtLevel(skillTypeId: Int, level: Int): Flow<List<SkillUnlockTypeRow>> = flow {
        emit(typeDao.getTypesUnlockedBySkillAtLevel(skillTypeId, level))
    }.flowOn(Dispatchers.IO)

    private suspend fun resolveSkillUnlockLevels(typeId: Int): List<Int> {
        val attrs = typeDao.getTypeAttributeDetails(typeId)
        val hasSkillAnchor = attrs.any { it.name == SKILL_MISC_ANCHOR_NAME }
        if (!hasSkillAnchor) return emptyList()

        val maxLevel = SkillSpCalculator.resolveMaxTrainableLevel(
            attrs.find { it.name == SKILL_LEVEL_DOGMA_NAME }?.value,
        )
        return typeDao.getSkillUnlockLevels(typeId, maxLevel)
    }

    private suspend fun resolveSkillLevelSpRows(typeId: Int): List<SkillLevelSpRow> {
        val attrs = typeDao.getTypeAttributeDetails(typeId)
        val skillTimeConstant = attrs.find { it.name == SKILL_MISC_ANCHOR_NAME }?.value
            ?: return emptyList()
        if (skillTimeConstant <= 0) return emptyList()

        val maxLevel = SkillSpCalculator.resolveMaxTrainableLevel(
            attrs.find { it.name == SKILL_LEVEL_DOGMA_NAME }?.value,
        )

        return (1..maxLevel).map { level ->
            SkillLevelSpRow(
                targetLevel = level,
                spTotal = SkillSpCalculator.cumulativeSpFromZero(skillTimeConstant, level),
            )
        }
    }

    private var cachedSkillMiscDefinitions: List<DogmaAttributeEntity>? = null
    private var cachedSkillBonusDefinitions: List<DogmaAttributeEntity>? = null
    private var cachedDefinitionsLanguage: ContentLanguage = localeController.contentLanguage

    private fun syncDefinitionsLanguage() {
        val current = localeController.contentLanguage
        if (current == cachedDefinitionsLanguage) return
        cachedDefinitionsLanguage = current
        cachedSkillMiscDefinitions = null
        cachedSkillBonusDefinitions = null
    }

    private suspend fun getSkillMiscDefinitions(): List<DogmaAttributeEntity> {
        syncDefinitionsLanguage()
        cachedSkillMiscDefinitions?.let { return it }
        return typeDao.getSkillMiscDogmaDefinitions(SKILL_MISC_ANCHOR_NAME)
            .also { cachedSkillMiscDefinitions = it }
    }

    private suspend fun getSkillBonusDefinitions(): List<DogmaAttributeEntity> {
        syncDefinitionsLanguage()
        cachedSkillBonusDefinitions?.let { return it }
        return typeDao.getSkillBonusDogmaDefinitions(SKILL_MISC_ANCHOR_NAME)
            .also { cachedSkillBonusDefinitions = it }
    }

    private suspend fun resolveSkillMiscRows(typeId: Int): List<TypeSkillMiscRow> {
        val metadataDefinitions = getSkillMiscDefinitions()
        val bonusDefinitions = getSkillBonusDefinitions()
        if (metadataDefinitions.isEmpty() && bonusDefinitions.isEmpty()) return emptyList()

        val typeAttrs = typeDao.getTypeAttributeDetails(typeId)
        val metadataRows = buildSkillMetadataRows(typeAttrs, metadataDefinitions)
        val bonusRows = buildSkillBonusRows(typeAttrs, bonusDefinitions)
        return metadataRows + bonusRows
    }

    private suspend fun buildSkillMetadataRows(
        typeAttrs: List<TypeAttributeDetail>,
        definitions: List<DogmaAttributeEntity>,
    ): List<TypeSkillMiscRow> {
        if (definitions.isEmpty()) return emptyList()

        val definitionById = definitions.associateBy { it.id }
        val attributeRefUnitNames = definitions
            .mapNotNull { def ->
                val name = def.name ?: return@mapNotNull null
                if (name.endsWith("Attribute") && !name.startsWith("required")) def.unitName else null
            }
            .toSet()

        val metadataAttrs = typeAttrs
            .filter { definitionById.containsKey(it.attributeId) }
            .sortedBy { it.attributeId }

        val referencedAttributeIds = metadataAttrs.mapNotNull { attr ->
            val def = definitionById[attr.attributeId] ?: return@mapNotNull null
            val raw = attr.value ?: return@mapNotNull null
            if (def.unitName in attributeRefUnitNames && raw > 0) raw.toInt() else null
        }.distinct()

        val referencedDisplayById = if (referencedAttributeIds.isEmpty()) {
            emptyMap()
        } else {
            typeDao.getDogmaAttributesByIds(referencedAttributeIds).associate { ref ->
                ref.id to (ref.displayName?.takeIf { it.isNotBlank() } ?: ref.name.orEmpty())
            }
        }

        return metadataAttrs.mapNotNull { attr ->
            val def = definitionById[attr.attributeId] ?: return@mapNotNull null
            val displayValue = formatSkillMiscDisplayValue(
                attr = attr,
                def = def,
                attributeRefUnitNames = attributeRefUnitNames,
                referencedDisplayById = referencedDisplayById,
            ) ?: return@mapNotNull null
            toSkillMiscRow(attr, def, displayValue)
        }
    }

    private fun buildSkillBonusRows(
        typeAttrs: List<TypeAttributeDetail>,
        definitions: List<DogmaAttributeEntity>,
    ): List<TypeSkillMiscRow> {
        if (definitions.isEmpty()) return emptyList()

        val definitionById = definitions.associateBy { it.id }
        return typeAttrs
            .filter { definitionById.containsKey(it.attributeId) && it.value != null }
            .sortedBy { it.attributeId }
            .mapNotNull { attr ->
                val def = definitionById[attr.attributeId] ?: return@mapNotNull null
                val displayValue = formatSkillBonusDisplayValue(attr, def) ?: return@mapNotNull null
                toSkillMiscRow(attr, def, displayValue)
            }
    }

    private fun toSkillMiscRow(
        attr: TypeAttributeDetail,
        def: DogmaAttributeEntity,
        displayValue: String,
    ): TypeSkillMiscRow = TypeSkillMiscRow(
        attributeId = attr.attributeId,
        label = attr.displayName ?: def.displayName ?: attr.name.orEmpty(),
        value = displayValue,
        iconFilename = attr.iconFilename ?: def.iconFilename,
    )

    private fun formatSkillBonusDisplayValue(
        attr: TypeAttributeDetail,
        def: DogmaAttributeEntity,
    ): String? {
        val raw = attr.value ?: return null
        val unit = attr.unitName ?: def.unitName
        return DogmaAttributeFormatting.format(raw, unit)
    }

    private fun formatSkillMiscDisplayValue(
        attr: TypeAttributeDetail,
        def: DogmaAttributeEntity,
        attributeRefUnitNames: Set<String?>,
        referencedDisplayById: Map<Int, String>,
    ): String? {
        val raw = attr.value ?: return null
        if (def.unitName in attributeRefUnitNames && raw > 0) {
            return referencedDisplayById[raw.toInt()]?.takeIf { it.isNotBlank() }
        }
        return DogmaAttributeFormatting.format(raw, attr.unitName ?: def.unitName)
    }

    fun getVariantCount(typeId: Int): Flow<Int> = flow {
        emit(resolveVariantCount(typeId))
    }.flowOn(Dispatchers.IO)

    fun getVariants(typeId: Int): Flow<List<TypeEntity>> = flow {
        emit(resolveVariants(typeId))
    }.flowOn(Dispatchers.IO)

    private suspend fun resolveVariationRootTypeId(typeId: Int): Int? {
        val type = typeDao.getTypeById(typeId) ?: return null
        return type.variationParentTypeID?.takeIf { it > 0 } ?: type.id
    }

    private suspend fun resolveVariantCount(typeId: Int): Int {
        val rootTypeId = resolveVariationRootTypeId(typeId) ?: return 0
        return typeDao.getVariantCount(rootTypeId)
    }

    private suspend fun resolveVariants(typeId: Int): List<TypeEntity> {
        val rootTypeId = resolveVariationRootTypeId(typeId) ?: return emptyList()
        return typeDao.getVariantsByRoot(rootTypeId)
    }

    private suspend fun resolveSkillRequirements(
        typeId: Int,
        flattenedMap: MutableMap<Int, SkillRequirement> = mutableMapOf()
    ): List<SkillRequirement> {
        val skillAttrs = typeDao.getTypeAttributeDetails(typeId).filter { it.categoryId == 8 }

        for (i in 1..6) {
            val skillAttr = skillAttrs.find { it.name == "requiredSkill$i" }
            val levelAttr = skillAttrs.find { it.name == "requiredSkill${i}Level" }

            if (skillAttr != null && skillAttr.value != null && skillAttr.value > 0) {
                val skillTypeId = skillAttr.value.toInt()
                val skillLevel = levelAttr?.value?.toInt() ?: 1

                val existing = flattenedMap[skillTypeId]
                if (existing == null || skillLevel > existing.level) {
                    val skillType = typeDao.getTypeById(skillTypeId)
                    if (skillType != null) {
                        flattenedMap[skillTypeId] = SkillRequirement(
                            typeId = skillTypeId,
                            name = skillType.displayName(localeController).ifBlank { "Unknown Skill" },
                            level = skillLevel,
                            iconFilename = skillType.iconFilename
                        )
                        // Recursively resolve prerequisites for this skill
                        resolveSkillRequirements(skillTypeId, flattenedMap)
                    }
                }
            }
        }
        return flattenedMap.values.toList().sortedByDescending { it.level }
    }
}
