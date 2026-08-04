package com.marshall.pyerite.characterSkillsModule.viewModel

import com.marshall.pyerite.characterSkillsModule.data.SkillPlanStore
import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanEntry
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Shared skill plans for every logged-in character (not keyed by characterId).
 */
class SkillPlanRepository internal constructor(
    private val store: SkillPlanStore,
) {
    private val _plans = MutableStateFlow(store.loadPlans())
    val plans: StateFlow<List<SkillPlanListItem>> = _plans.asStateFlow()

    private val _showCompleted = MutableStateFlow(store.loadShowCompleted())
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()

    fun plan(planId: Long): SkillPlanListItem? =
        _plans.value.firstOrNull { it.id == planId }

    fun addPlan(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        _plans.update { current ->
            val next = current + SkillPlanListItem(
                id = nextId(current),
                name = trimmed,
            )
            store.savePlans(next)
            next
        }
    }

    /**
     * Merges [orderedIncoming] into the plan: existing skills keep position and the
     * higher target level; new skills are appended in [orderedIncoming] order.
     */
    fun mergePlanEntries(planId: Long, orderedIncoming: List<SkillPlanEntry>) {
        if (orderedIncoming.isEmpty()) return
        _plans.update { current ->
            val index = current.indexOfFirst { it.id == planId }
            if (index < 0) return@update current
            val plan = current[index]
            val incomingById = linkedMapOf<Int, Int>()
            for (entry in orderedIncoming) {
                if (entry.targetLevel <= 0) continue
                val target = entry.targetLevel.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
                incomingById[entry.skillTypeId] = maxOf(
                    incomingById[entry.skillTypeId] ?: 0,
                    target,
                )
            }
            if (incomingById.isEmpty()) return@update current

            val existingIdSet = plan.entries.map { it.skillTypeId }.toSet()
            val updatedExisting = plan.entries.map { entry ->
                val raised = incomingById[entry.skillTypeId] ?: return@map entry
                if (raised > entry.targetLevel) {
                    entry.copy(targetLevel = raised)
                } else {
                    entry
                }
            }
            val appended = orderedIncoming.mapNotNull { entry ->
                val typeId = entry.skillTypeId
                if (typeId in existingIdSet) return@mapNotNull null
                val target = incomingById[typeId] ?: return@mapNotNull null
                SkillPlanEntry(skillTypeId = typeId, targetLevel = target)
            }.distinctBy { it.skillTypeId }
            val next = current.toMutableList()
            next[index] = plan.copy(entries = updatedExisting + appended)
            store.savePlans(next)
            next
        }
    }

    /** Clears all skill entries from the plan; keeps the plan itself. */
    fun clearPlanEntries(planId: Long) {
        _plans.update { current ->
            val index = current.indexOfFirst { it.id == planId }
            if (index < 0) return@update current
            val plan = current[index]
            if (plan.entries.isEmpty()) return@update current
            val next = current.toMutableList()
            next[index] = plan.copy(entries = emptyList())
            store.savePlans(next)
            next
        }
    }

    fun setShowCompleted(showCompleted: Boolean) {
        if (_showCompleted.value == showCompleted) return
        _showCompleted.value = showCompleted
        store.saveShowCompleted(showCompleted)
    }

    private fun nextId(current: List<SkillPlanListItem>): Long =
        (current.maxOfOrNull { it.id } ?: 0L) + 1L
}
