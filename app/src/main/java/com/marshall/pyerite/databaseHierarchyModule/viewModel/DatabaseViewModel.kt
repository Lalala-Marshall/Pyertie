package com.marshall.pyerite.databaseHierarchyModule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity
import com.marshall.pyerite.sdeModule.room.dogma.DogmaAttributeEntity
import com.marshall.pyerite.sdeModule.room.catalog.MetaGroupEntity
import com.marshall.pyerite.sdeModule.room.catalog.GroupEntity
import com.marshall.pyerite.sdeModule.room.skill.SkillRequirement
import com.marshall.pyerite.sdeModule.room.dogma.TypeAttributeDetail
import com.marshall.pyerite.sdeModule.room.industry.BlueprintCopyDetail
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionMaterial
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionProduct
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionSkill
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingMaterial
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingProduct
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingSkill
import com.marshall.pyerite.sdeModule.room.industry.TypeBlueprintDetail
import com.marshall.pyerite.sdeModule.room.type.TypeEntity
import com.marshall.pyerite.sdeModule.room.industry.TypeApplicableBlueprintCount
import com.marshall.pyerite.sdeModule.room.type.TypeCompatibleGroupDetail
import com.marshall.pyerite.sdeModule.room.industry.TypeRefiningOutputItem
import com.marshall.pyerite.sdeModule.room.industry.TypeRefiningOutputSummary
import com.marshall.pyerite.sdeModule.room.industry.TypeRefiningSourceCount
import com.marshall.pyerite.sdeModule.room.industry.TypeRefiningSourceItem
import com.marshall.pyerite.sdeModule.room.skill.SkillLevelSpRow
import com.marshall.pyerite.sdeModule.room.skill.SkillUnlockTypeRow
import com.marshall.pyerite.sdeModule.room.type.TypeSkillMiscRow
import com.marshall.pyerite.sdeModule.room.dogma.TypeTraitDetail
import com.marshall.pyerite.databaseHierarchyModule.search.HierarchySearchState
import com.marshall.pyerite.databaseHierarchyModule.search.TypeSearchUiState
import com.marshall.pyerite.sdeModule.update.SdeUpdateRepository
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.localization.LocaleController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
    kotlinx.coroutines.FlowPreview::class,
)
class DatabaseViewModel(
    private val repository: DatabaseRepository,
    private val localeController: LocaleController,
    private val sdeUpdateRepository: SdeUpdateRepository,
) : ViewModel() {

    companion object {
        private val TYPE_SEARCH_DEBOUNCE = 300.milliseconds
        private const val TYPE_SEARCH_LIMIT = 500
    }

    private var contentLanguage: ContentLanguage = localeController.contentLanguage

    init {
        viewModelScope.launch {
            sdeUpdateRepository.contentRefreshed.collect {
                clearCachedFlows()
            }
        }
    }

    private var categoriesFlow: StateFlow<List<CategoryEntity>>? = null

    val categories: StateFlow<List<CategoryEntity>>
        get() {
            syncContentLanguage()
            return categoriesFlow ?: repository.getCategories()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                .also { categoriesFlow = it }
        }

    private var metaGroupsFlow: StateFlow<List<MetaGroupEntity>>? = null

    val metaGroups: StateFlow<List<MetaGroupEntity>>
        get() {
            syncContentLanguage()
            return metaGroupsFlow ?: repository.getMetaGroups()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                .also { metaGroupsFlow = it }
        }

    private fun syncContentLanguage() {
        val current = localeController.contentLanguage
        if (current == contentLanguage) return
        contentLanguage = current
        clearCachedFlows()
    }

    private fun clearCachedFlows() {
        categoriesFlow = null
        metaGroupsFlow = null
        groupsFlows.clear()
        typesFlows.clear()
        typeDetailFlows.clear()
        typeAttributesFlows.clear()
        dogmaAttributesFlows.clear()
        skillRequirementsFlows.clear()
        skillMiscRowsFlows.clear()
        skillLevelSpRowsFlows.clear()
        skillUnlockLevelsFlows.clear()
        skillUnlockTypesFlows.clear()
        typeTraitsFlows.clear()
        blueprintsFlows.clear()
        refiningOutputFlows.clear()
        applicableBlueprintCountFlows.clear()
        refiningSourceCountFlows.clear()
        refiningOutputListFlows.clear()
        refiningSourceListFlows.clear()
        applicableBlueprintsFlows.clear()
        blueprintManufacturingProductFlows.clear()
        blueprintManufacturingMaterialFlows.clear()
        blueprintManufacturingSkillFlows.clear()
        blueprintManufacturingTimeFlows.clear()
        blueprintResearchMaterialTimeFlows.clear()
        blueprintResearchTimeTimeFlows.clear()
        blueprintCopyDetailFlows.clear()
        blueprintInventionProductFlows.clear()
        blueprintInventionMaterialFlows.clear()
        blueprintInventionSkillFlows.clear()
        blueprintInventionTimeFlows.clear()
        compatibleGroupsFlows.clear()
        variantCountFlows.clear()
        variantsFlows.clear()
        typeSearchStateFlows.clear()
    }

    private val typeSearchStateFlows = mutableMapOf<String, StateFlow<TypeSearchUiState>>()

    fun typeSearchState(pageKey: String): StateFlow<TypeSearchUiState> {
        syncContentLanguage()
        return typeSearchStateFlows.getOrPut(pageKey) {
            val queryFlow = searchState(pageKey)
                .map { it.query.trim() }
                .distinctUntilChanged()
            val settledFlow = queryFlow
                .debounce(TYPE_SEARCH_DEBOUNCE)
                .flatMapLatest { query ->
                    if (query.isEmpty()) {
                        flowOf(TypeSearchUiState(settledQuery = ""))
                    } else {
                        repository.searchTypes(query, TYPE_SEARCH_LIMIT).map { results ->
                            TypeSearchUiState(
                                results = results,
                                settledQuery = query,
                                isTruncated = results.size >= TYPE_SEARCH_LIMIT,
                            )
                        }
                    }
                }
            combine(queryFlow, settledFlow) { liveQuery, settled ->
                settled.copy(isLoading = liveQuery.isNotEmpty() && liveQuery != settled.settledQuery)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TypeSearchUiState())
        }
    }

    private val groupsFlows = mutableMapOf<Int, StateFlow<List<GroupEntity>>>()

    fun groups(categoryId: Int): StateFlow<List<GroupEntity>> {
        syncContentLanguage()
        return groupsFlows.getOrPut(categoryId) {
        repository.getGroups(categoryId)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val typesFlows = mutableMapOf<Int, StateFlow<List<TypeEntity>>>()

    fun types(groupId: Int): StateFlow<List<TypeEntity>> {
        syncContentLanguage()
        return typesFlows.getOrPut(groupId) {
        repository.getTypes(groupId)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val typeDetailFlows = mutableMapOf<Int, StateFlow<TypeEntity?>>()

    fun typeDetail(typeId: Int): StateFlow<TypeEntity?> {
        syncContentLanguage()
        return typeDetailFlows.getOrPut(typeId) {
        repository.getType(typeId)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val typeAttributesFlows = mutableMapOf<Int, StateFlow<List<TypeAttributeDetail>>>()

    fun typeAttributes(typeId: Int): StateFlow<List<TypeAttributeDetail>> {
        syncContentLanguage()
        return typeAttributesFlows.getOrPut(typeId) {
            repository.getTypeAttributes(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val dogmaAttributesFlows = mutableMapOf<List<String>, StateFlow<List<DogmaAttributeEntity>>>()

    fun dogmaAttributes(names: List<String>): StateFlow<List<DogmaAttributeEntity>> {
        syncContentLanguage()
        return dogmaAttributesFlows.getOrPut(names) {
            repository.getDogmaAttributesByNames(names)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val skillRequirementsFlows = mutableMapOf<Int, StateFlow<List<SkillRequirement>>>()

    fun skillRequirements(typeId: Int): StateFlow<List<SkillRequirement>> {
        syncContentLanguage()
        return skillRequirementsFlows.getOrPut(typeId) {
            repository.getSkillRequirements(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val skillMiscRowsFlows = mutableMapOf<Int, StateFlow<List<TypeSkillMiscRow>>>()

    fun skillMiscRows(typeId: Int): StateFlow<List<TypeSkillMiscRow>> {
        syncContentLanguage()
        return skillMiscRowsFlows.getOrPut(typeId) {
            repository.getSkillMiscRows(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val skillLevelSpRowsFlows = mutableMapOf<Int, StateFlow<List<SkillLevelSpRow>>>()

    fun skillLevelSpRows(typeId: Int): StateFlow<List<SkillLevelSpRow>> {
        syncContentLanguage()
        return skillLevelSpRowsFlows.getOrPut(typeId) {
            repository.getSkillLevelSpRows(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val skillUnlockLevelsFlows = mutableMapOf<Int, StateFlow<List<Int>>>()

    fun skillUnlockLevels(typeId: Int): StateFlow<List<Int>> {
        syncContentLanguage()
        return skillUnlockLevelsFlows.getOrPut(typeId) {
            repository.getSkillUnlockLevels(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val skillUnlockTypesFlows = mutableMapOf<Pair<Int, Int>, StateFlow<List<SkillUnlockTypeRow>>>()

    fun typesUnlockedBySkillAtLevel(skillTypeId: Int, level: Int): StateFlow<List<SkillUnlockTypeRow>> {
        syncContentLanguage()
        return skillUnlockTypesFlows.getOrPut(skillTypeId to level) {
            repository.getTypesUnlockedBySkillAtLevel(skillTypeId, level)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val typeTraitsFlows = mutableMapOf<Int, StateFlow<List<TypeTraitDetail>>>()

    fun typeTraits(typeId: Int): StateFlow<List<TypeTraitDetail>> {
        syncContentLanguage()
        return typeTraitsFlows.getOrPut(typeId) {
            repository.getTypeTraits(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val blueprintsFlows = mutableMapOf<Int, StateFlow<List<TypeBlueprintDetail>>>()

    fun blueprintsForProduct(typeId: Int): StateFlow<List<TypeBlueprintDetail>> {
        syncContentLanguage()
        return blueprintsFlows.getOrPut(typeId) {
            repository.getBlueprintsForProduct(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val refiningOutputFlows = mutableMapOf<Int, StateFlow<TypeRefiningOutputSummary?>>()

    fun refiningOutputSummary(typeId: Int): StateFlow<TypeRefiningOutputSummary?> {
        syncContentLanguage()
        return refiningOutputFlows.getOrPut(typeId) {
            repository.getRefiningOutputSummary(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val applicableBlueprintCountFlows =
        mutableMapOf<Int, StateFlow<TypeApplicableBlueprintCount?>>()

    fun applicableBlueprintCount(typeId: Int): StateFlow<TypeApplicableBlueprintCount?> {
        syncContentLanguage()
        return applicableBlueprintCountFlows.getOrPut(typeId) {
            repository.getApplicableBlueprintCount(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val refiningSourceCountFlows = mutableMapOf<Int, StateFlow<TypeRefiningSourceCount?>>()

    fun refiningSourceCount(typeId: Int): StateFlow<TypeRefiningSourceCount?> {
        syncContentLanguage()
        return refiningSourceCountFlows.getOrPut(typeId) {
            repository.getRefiningSourceCount(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val refiningOutputListFlows = mutableMapOf<Int, StateFlow<List<TypeRefiningOutputItem>>>()

    fun refiningOutputs(typeId: Int): StateFlow<List<TypeRefiningOutputItem>> {
        syncContentLanguage()
        return refiningOutputListFlows.getOrPut(typeId) {
            repository.getRefiningOutputs(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val refiningSourceListFlows = mutableMapOf<Int, StateFlow<List<TypeRefiningSourceItem>>>()

    fun refiningSources(typeId: Int): StateFlow<List<TypeRefiningSourceItem>> {
        syncContentLanguage()
        return refiningSourceListFlows.getOrPut(typeId) {
            repository.getRefiningSources(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val applicableBlueprintsFlows = mutableMapOf<Int, StateFlow<List<TypeBlueprintDetail>>>()

    fun applicableBlueprints(typeId: Int): StateFlow<List<TypeBlueprintDetail>> {
        syncContentLanguage()
        return applicableBlueprintsFlows.getOrPut(typeId) {
            repository.getApplicableBlueprints(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val blueprintManufacturingProductFlows =
        mutableMapOf<Int, StateFlow<List<BlueprintManufacturingProduct>>>()

    fun blueprintManufacturingProducts(typeId: Int): StateFlow<List<BlueprintManufacturingProduct>> {
        syncContentLanguage()
        return blueprintManufacturingProductFlows.getOrPut(typeId) {
            repository.getBlueprintManufacturingProducts(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val blueprintManufacturingMaterialFlows =
        mutableMapOf<Int, StateFlow<List<BlueprintManufacturingMaterial>>>()

    fun blueprintManufacturingMaterials(typeId: Int): StateFlow<List<BlueprintManufacturingMaterial>> {
        syncContentLanguage()
        return blueprintManufacturingMaterialFlows.getOrPut(typeId) {
            repository.getBlueprintManufacturingMaterials(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val blueprintManufacturingSkillFlows =
        mutableMapOf<Int, StateFlow<List<BlueprintManufacturingSkill>>>()

    fun blueprintManufacturingSkills(typeId: Int): StateFlow<List<BlueprintManufacturingSkill>> {
        syncContentLanguage()
        return blueprintManufacturingSkillFlows.getOrPut(typeId) {
            repository.getBlueprintManufacturingSkills(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val blueprintManufacturingTimeFlows = mutableMapOf<Int, StateFlow<Int?>>()

    fun blueprintManufacturingTime(typeId: Int): StateFlow<Int?> {
        syncContentLanguage()
        return blueprintManufacturingTimeFlows.getOrPut(typeId) {
            repository.getBlueprintManufacturingTime(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val blueprintResearchMaterialTimeFlows = mutableMapOf<Int, StateFlow<Int?>>()

    fun blueprintResearchMaterialTime(typeId: Int): StateFlow<Int?> {
        syncContentLanguage()
        return blueprintResearchMaterialTimeFlows.getOrPut(typeId) {
            repository.getBlueprintResearchMaterialTime(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val blueprintResearchTimeTimeFlows = mutableMapOf<Int, StateFlow<Int?>>()

    fun blueprintResearchTimeTime(typeId: Int): StateFlow<Int?> {
        syncContentLanguage()
        return blueprintResearchTimeTimeFlows.getOrPut(typeId) {
            repository.getBlueprintResearchTimeTime(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val blueprintCopyDetailFlows = mutableMapOf<Int, StateFlow<BlueprintCopyDetail?>>()

    fun blueprintCopyDetail(typeId: Int): StateFlow<BlueprintCopyDetail?> {
        syncContentLanguage()
        return blueprintCopyDetailFlows.getOrPut(typeId) {
            repository.getBlueprintCopyDetail(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val blueprintInventionProductFlows =
        mutableMapOf<Int, StateFlow<List<BlueprintInventionProduct>>>()

    fun blueprintInventionProducts(typeId: Int): StateFlow<List<BlueprintInventionProduct>> {
        syncContentLanguage()
        return blueprintInventionProductFlows.getOrPut(typeId) {
            repository.getBlueprintInventionProducts(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val blueprintInventionMaterialFlows =
        mutableMapOf<Int, StateFlow<List<BlueprintInventionMaterial>>>()

    fun blueprintInventionMaterials(typeId: Int): StateFlow<List<BlueprintInventionMaterial>> {
        syncContentLanguage()
        return blueprintInventionMaterialFlows.getOrPut(typeId) {
            repository.getBlueprintInventionMaterials(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val blueprintInventionSkillFlows =
        mutableMapOf<Int, StateFlow<List<BlueprintInventionSkill>>>()

    fun blueprintInventionSkills(typeId: Int): StateFlow<List<BlueprintInventionSkill>> {
        syncContentLanguage()
        return blueprintInventionSkillFlows.getOrPut(typeId) {
            repository.getBlueprintInventionSkills(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val blueprintInventionTimeFlows = mutableMapOf<Int, StateFlow<Int?>>()

    fun blueprintInventionTime(typeId: Int): StateFlow<Int?> {
        syncContentLanguage()
        return blueprintInventionTimeFlows.getOrPut(typeId) {
            repository.getBlueprintInventionTime(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }
    }

    private val compatibleGroupsFlows = mutableMapOf<Int, StateFlow<List<TypeCompatibleGroupDetail>>>()

    fun compatibleGroups(typeId: Int): StateFlow<List<TypeCompatibleGroupDetail>> {
        syncContentLanguage()
        return compatibleGroupsFlows.getOrPut(typeId) {
            repository.getCompatibleGroups(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val variantCountFlows = mutableMapOf<Int, StateFlow<Int>>()

    fun variantCount(typeId: Int): StateFlow<Int> {
        syncContentLanguage()
        return variantCountFlows.getOrPut(typeId) {
            repository.getVariantCount(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, 0)
        }
    }

    private val variantsFlows = mutableMapOf<Int, StateFlow<List<TypeEntity>>>()

    fun variants(typeId: Int): StateFlow<List<TypeEntity>> {
        syncContentLanguage()
        return variantsFlows.getOrPut(typeId) {
            repository.getVariants(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    private val typeDetailScrollOffsets = mutableMapOf<Int, Int>()

    fun typeDetailScrollOffset(typeId: Int): Int = typeDetailScrollOffsets[typeId] ?: 0

    fun saveTypeDetailScrollOffset(typeId: Int, offset: Int) {
        typeDetailScrollOffsets[typeId] = offset
    }

    private val hierarchyScrollPositions = mutableMapOf<String, HierarchyScrollPosition>()

    fun hierarchyScrollPosition(scrollKey: String): HierarchyScrollPosition =
        hierarchyScrollPositions[scrollKey] ?: HierarchyScrollPosition()

    fun saveHierarchyScrollPosition(scrollKey: String, index: Int, offset: Int) {
        hierarchyScrollPositions[scrollKey] = HierarchyScrollPosition(index, offset)
    }

    private val hierarchySearchStateFlows = mutableMapOf<String, MutableStateFlow<HierarchySearchState>>()

    private fun hierarchySearchStateFlow(pageKey: String): MutableStateFlow<HierarchySearchState> =
        hierarchySearchStateFlows.getOrPut(pageKey) { MutableStateFlow(HierarchySearchState()) }

    fun searchState(pageKey: String): StateFlow<HierarchySearchState> =
        hierarchySearchStateFlow(pageKey)

    fun setSearchActive(pageKey: String, active: Boolean) {
        hierarchySearchStateFlow(pageKey).value =
            hierarchySearchStateFlow(pageKey).value.copy(isActive = active)
    }

    fun setSearchQuery(pageKey: String, query: String) {
        hierarchySearchStateFlow(pageKey).value =
            hierarchySearchStateFlow(pageKey).value.copy(query = query)
    }

    fun cancelSearch(pageKey: String) {
        hierarchySearchStateFlow(pageKey).value = HierarchySearchState()
    }
}

data class HierarchyScrollPosition(
    val index: Int = 0,
    val offset: Int = 0,
)
