package com.marshall.pyerite.databaseHierarchyModule.viewModel

import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.MetaGroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeTraitDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.SkillRequirement
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeApplicableBlueprintCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DatabaseRepository(roomProvider: RoomProvider) {

    private val categoryDao = roomProvider.getDatabase().categoryDao()
    private val metaGroupDao = roomProvider.getDatabase().metaGroupDao()

    fun getCategories(): Flow<List<CategoryEntity>> = flow {
        emit(categoryDao.getCategories())
    }.flowOn(Dispatchers.IO)


    private val groupDao = roomProvider.getDatabase().groupDao()
    fun getGroups(categoryId: Int): Flow<List<GroupEntity>> = flow {
        emit(groupDao.getGroupsByCategory(categoryId))
    }.flowOn(Dispatchers.IO)

    private val typeDao = roomProvider.getDatabase().typeDao()
    private val traitDao = roomProvider.getDatabase().traitDao()
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

    fun getSkillRequirements(typeId: Int): Flow<List<SkillRequirement>> = flow {
        emit(resolveSkillRequirements(typeId))
    }.flowOn(Dispatchers.IO)

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
                            name = skillType.zhName ?: skillType.name ?: "Unknown Skill",
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
