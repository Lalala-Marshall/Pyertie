package com.marshall.pyerite.characterSkillsModule.data

import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanItemPickerConfig
import com.marshall.pyerite.sdeModule.room.RoomProvider
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity
import com.marshall.pyerite.sdeModule.room.catalog.GroupEntity
import com.marshall.pyerite.sdeModule.room.type.TypeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads category → group → type rows for items that declare skill requirements.
 */
internal class SkillPlanItemCatalogLoader(
    private val roomProvider: RoomProvider,
) {
    suspend fun loadCategories(): List<CategoryEntity> = withContext(Dispatchers.IO) {
        roomProvider.getDatabase().skillDao().getCategoriesWithSkillRequirements(
            skillsCategoryId = SkillCatalogConfig.SKILLS_CATEGORY_ID,
        )
    }

    suspend fun loadGroups(categoryId: Int): List<GroupEntity> = withContext(Dispatchers.IO) {
        roomProvider.getDatabase().skillDao().getGroupsWithSkillRequirements(categoryId)
    }

    suspend fun loadTypes(groupId: Int): List<TypeEntity> = withContext(Dispatchers.IO) {
        roomProvider.getDatabase().skillDao().getTypesWithSkillRequirements(groupId)
    }

    suspend fun searchTypes(query: String): List<TypeEntity> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()
        roomProvider.getDatabase().skillDao().searchTypesWithSkillRequirements(
            pattern = "%$trimmed%",
            skillsCategoryId = SkillCatalogConfig.SKILLS_CATEGORY_ID,
            limit = SkillPlanItemPickerConfig.SEARCH_RESULT_LIMIT,
        )
    }
}
