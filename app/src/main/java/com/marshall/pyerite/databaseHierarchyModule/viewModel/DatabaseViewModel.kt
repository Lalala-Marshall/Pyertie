package com.marshall.pyerite.databaseHierarchyModule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillRequirement
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeApplicableBlueprintCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeCompatibleGroupDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillLevelSpRow
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillUnlockTypeRow
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeSkillMiscRow
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeTraitDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

class DatabaseViewModel(
    private val repository: DatabaseRepository,
) : ViewModel() {

    val categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val groupsFlows = mutableMapOf<Int, StateFlow<List<GroupEntity>>>()

    fun groups(categoryId: Int) = groupsFlows.getOrPut(categoryId) {
        repository.getGroups(categoryId)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    private val typesFlows = mutableMapOf<Int, StateFlow<List<TypeEntity>>>()

    fun types(groupId: Int) = typesFlows.getOrPut(groupId) {
        repository.getTypes(groupId)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    private val typeDetailFlows = mutableMapOf<Int, StateFlow<TypeEntity?>>()

    fun typeDetail(typeId: Int): StateFlow<TypeEntity?> = typeDetailFlows.getOrPut(typeId) {
        repository.getType(typeId)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    val metaGroups = repository.getMetaGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val typeAttributesFlows = mutableMapOf<Int, StateFlow<List<TypeAttributeDetail>>>()

    fun typeAttributes(typeId: Int): StateFlow<List<TypeAttributeDetail>> =
        typeAttributesFlows.getOrPut(typeId) {
            repository.getTypeAttributes(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val dogmaAttributesFlows = mutableMapOf<List<String>, StateFlow<List<DogmaAttributeEntity>>>()

    fun dogmaAttributes(names: List<String>): StateFlow<List<DogmaAttributeEntity>> =
        dogmaAttributesFlows.getOrPut(names) {
            repository.getDogmaAttributesByNames(names)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val skillRequirementsFlows = mutableMapOf<Int, StateFlow<List<SkillRequirement>>>()

    fun skillRequirements(typeId: Int): StateFlow<List<SkillRequirement>> =
        skillRequirementsFlows.getOrPut(typeId) {
            repository.getSkillRequirements(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val skillMiscRowsFlows = mutableMapOf<Int, StateFlow<List<TypeSkillMiscRow>>>()

    fun skillMiscRows(typeId: Int): StateFlow<List<TypeSkillMiscRow>> =
        skillMiscRowsFlows.getOrPut(typeId) {
            repository.getSkillMiscRows(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val skillLevelSpRowsFlows = mutableMapOf<Int, StateFlow<List<SkillLevelSpRow>>>()

    fun skillLevelSpRows(typeId: Int): StateFlow<List<SkillLevelSpRow>> =
        skillLevelSpRowsFlows.getOrPut(typeId) {
            repository.getSkillLevelSpRows(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val skillUnlockLevelsFlows = mutableMapOf<Int, StateFlow<List<Int>>>()

    fun skillUnlockLevels(typeId: Int): StateFlow<List<Int>> =
        skillUnlockLevelsFlows.getOrPut(typeId) {
            repository.getSkillUnlockLevels(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val skillUnlockTypesFlows = mutableMapOf<Pair<Int, Int>, StateFlow<List<SkillUnlockTypeRow>>>()

    fun typesUnlockedBySkillAtLevel(skillTypeId: Int, level: Int): StateFlow<List<SkillUnlockTypeRow>> =
        skillUnlockTypesFlows.getOrPut(skillTypeId to level) {
            repository.getTypesUnlockedBySkillAtLevel(skillTypeId, level)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val typeTraitsFlows = mutableMapOf<Int, StateFlow<List<TypeTraitDetail>>>()

    fun typeTraits(typeId: Int): StateFlow<List<TypeTraitDetail>> =
        typeTraitsFlows.getOrPut(typeId) {
            repository.getTypeTraits(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val blueprintsFlows = mutableMapOf<Int, StateFlow<List<TypeBlueprintDetail>>>()

    fun blueprintsForProduct(typeId: Int): StateFlow<List<TypeBlueprintDetail>> =
        blueprintsFlows.getOrPut(typeId) {
            repository.getBlueprintsForProduct(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val refiningOutputFlows = mutableMapOf<Int, StateFlow<TypeRefiningOutputSummary?>>()

    fun refiningOutputSummary(typeId: Int): StateFlow<TypeRefiningOutputSummary?> =
        refiningOutputFlows.getOrPut(typeId) {
            repository.getRefiningOutputSummary(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }

    private val applicableBlueprintCountFlows =
        mutableMapOf<Int, StateFlow<TypeApplicableBlueprintCount?>>()

    fun applicableBlueprintCount(typeId: Int): StateFlow<TypeApplicableBlueprintCount?> =
        applicableBlueprintCountFlows.getOrPut(typeId) {
            repository.getApplicableBlueprintCount(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }

    private val refiningSourceCountFlows = mutableMapOf<Int, StateFlow<TypeRefiningSourceCount?>>()

    fun refiningSourceCount(typeId: Int): StateFlow<TypeRefiningSourceCount?> =
        refiningSourceCountFlows.getOrPut(typeId) {
            repository.getRefiningSourceCount(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
        }

    private val compatibleGroupsFlows = mutableMapOf<Int, StateFlow<List<TypeCompatibleGroupDetail>>>()

    fun compatibleGroups(typeId: Int): StateFlow<List<TypeCompatibleGroupDetail>> =
        compatibleGroupsFlows.getOrPut(typeId) {
            repository.getCompatibleGroups(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }

    private val variantCountFlows = mutableMapOf<Int, StateFlow<Int>>()

    fun variantCount(typeId: Int): StateFlow<Int> =
        variantCountFlows.getOrPut(typeId) {
            repository.getVariantCount(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, 0)
        }

    private val variantsFlows = mutableMapOf<Int, StateFlow<List<TypeEntity>>>()

    fun variants(typeId: Int): StateFlow<List<TypeEntity>> =
        variantsFlows.getOrPut(typeId) {
            repository.getVariants(typeId)
                .distinctUntilChanged()
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
}
