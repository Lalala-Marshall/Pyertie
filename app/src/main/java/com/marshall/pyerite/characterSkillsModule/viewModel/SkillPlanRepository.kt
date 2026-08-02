package com.marshall.pyerite.characterSkillsModule.viewModel

import com.marshall.pyerite.characterSkillsModule.data.SkillPlanStore
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

    private fun nextId(current: List<SkillPlanListItem>): Long =
        (current.maxOfOrNull { it.id } ?: 0L) + 1L
}
