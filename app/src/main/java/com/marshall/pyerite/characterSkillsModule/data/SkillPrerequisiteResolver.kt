package com.marshall.pyerite.characterSkillsModule.data

import com.marshall.pyerite.characterSkillsModule.model.SkillPrerequisiteConfig
import com.marshall.pyerite.sdeModule.room.RoomProvider
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Resolves recursive skill prerequisites from SDE dogma into
 * `skillTypeId → max required level` (excludes the root skill itself).
 */
internal class SkillPrerequisiteResolver(
    private val roomProvider: RoomProvider,
) {
    private val flattenedCache = ConcurrentHashMap<Int, Map<Int, Int>>()
    private val directCache = ConcurrentHashMap<Int, Map<Int, Int>>()

    /**
     * Flattened prerequisites for [skillTypeId]: each entry is a skill that must be
     * trained to at least the given level. Does not include [skillTypeId].
     */
    suspend fun requiredLevels(skillTypeId: Int): Map<Int, Int> {
        flattenedCache[skillTypeId]?.let { return it }
        return withContext(Dispatchers.IO) {
            val flattened = mutableMapOf<Int, Int>()
            resolveInto(skillTypeId, flattened)
            flattened.toMap().also { flattenedCache[skillTypeId] = it }
        }
    }

    /**
     * Immediate (non-recursive) prerequisite skills and levels for [skillTypeId].
     */
    suspend fun directRequiredLevels(skillTypeId: Int): Map<Int, Int> {
        directCache[skillTypeId]?.let { return it }
        return withContext(Dispatchers.IO) {
            readDirectRequirements(skillTypeId).also { directCache[skillTypeId] = it }
        }
    }

    private suspend fun resolveInto(
        typeId: Int,
        flattened: MutableMap<Int, Int>,
    ) {
        readDirectRequirements(typeId).forEach { (requiredSkillTypeId, requiredLevel) ->
            val existing = flattened[requiredSkillTypeId]
            if (existing != null && requiredLevel <= existing) return@forEach
            flattened[requiredSkillTypeId] = requiredLevel
            resolveInto(requiredSkillTypeId, flattened)
        }
    }

    private suspend fun readDirectRequirements(typeId: Int): Map<Int, Int> {
        val db = roomProvider.getDatabase()
        val dogmaDao = db.dogmaDao()
        val typeDao = db.typeDao()
        val skillAttrs = dogmaDao.getTypeAttributeDetails(typeId)
            .filter {
                it.categoryId == SkillPrerequisiteConfig.SKILL_REQUIREMENT_ATTRIBUTE_CATEGORY_ID
            }

        val direct = linkedMapOf<Int, Int>()
        for (slot in 1..SkillPrerequisiteConfig.REQUIRED_SKILL_SLOT_COUNT) {
            val skillAttrName = SkillPrerequisiteConfig.requiredSkillAttributeName(slot)
            val levelAttrName = SkillPrerequisiteConfig.requiredSkillLevelAttributeName(slot)
            val skillAttr = skillAttrs.find { it.name == skillAttrName }
            val levelAttr = skillAttrs.find { it.name == levelAttrName }
            val skillValue = skillAttr?.value ?: continue
            if (skillValue <= 0) continue

            val requiredSkillTypeId = skillValue.toInt()
            val requiredLevel = levelAttr?.value?.toInt()
                ?: SkillPrerequisiteConfig.DEFAULT_REQUIRED_LEVEL
            if (typeDao.getTypeById(requiredSkillTypeId) == null) continue
            val existing = direct[requiredSkillTypeId]
            if (existing == null || requiredLevel > existing) {
                direct[requiredSkillTypeId] = requiredLevel
            }
        }
        return direct
    }
}
