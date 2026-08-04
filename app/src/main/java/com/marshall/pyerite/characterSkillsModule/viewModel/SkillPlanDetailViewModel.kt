package com.marshall.pyerite.characterSkillsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterSkillsModule.data.SkillPrerequisiteResolver
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanEntry
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanLevelStepExpander
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanListItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val PLAN_FLOW_STOP_TIMEOUT_MS = 5_000L

/** Detail for one skill plan; [showCompleted] filters finished entries for the selected character. */
internal class SkillPlanDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SkillPlanRepository,
    private val prerequisiteResolver: SkillPrerequisiteResolver,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[SkillPlanViewModel.NAV_ARG_CHARACTER_ID]) {
        "SkillPlanDetail route requires ${SkillPlanViewModel.NAV_ARG_CHARACTER_ID}"
    }

    val planId: Long = checkNotNull(savedStateHandle[SkillPlanViewModel.NAV_ARG_PLAN_ID]) {
        "SkillPlanDetail route requires ${SkillPlanViewModel.NAV_ARG_PLAN_ID}"
    }

    val plan: StateFlow<SkillPlanListItem?> = repository.plans
        .map { plans -> plans.firstOrNull { it.id == planId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(PLAN_FLOW_STOP_TIMEOUT_MS),
            initialValue = repository.plan(planId),
        )

    /**
     * Plan entries expanded to one row per level step (0→1, 1→2, …),
     * in add-time order with prerequisites before each added skill.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val levelSteps: StateFlow<List<SkillPlanEntry>> = plan
        .mapLatest { current -> expandToLevelSteps(current?.entries.orEmpty()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(PLAN_FLOW_STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    val showCompleted: StateFlow<Boolean> = repository.showCompleted

    fun setShowCompleted(show: Boolean) {
        repository.setShowCompleted(show)
    }

    /** Adds or raises planned skill levels for this plan (levels ≤ 0 are ignored). */
    fun addSkills(levelsBySkillTypeId: Map<Int, Int>) {
        if (levelsBySkillTypeId.isEmpty()) return
        viewModelScope.launch {
            val ordered = orderCompactForMerge(levelsBySkillTypeId)
            repository.mergePlanEntries(planId, ordered)
        }
    }

    /** Removes every skill entry from this plan. */
    fun clearSkills() {
        repository.clearPlanEntries(planId)
    }

    private suspend fun orderCompactForMerge(
        levelsBySkillTypeId: Map<Int, Int>,
    ): List<SkillPlanEntry> {
        val flattenedBySkill = mutableMapOf<Int, Map<Int, Int>>()
        val directBySkill = mutableMapOf<Int, Map<Int, Int>>()
        loadPrerequisiteCaches(
            skillTypeIds = levelsBySkillTypeId.keys,
            flattenedBySkill = flattenedBySkill,
            directBySkill = directBySkill,
        )
        return SkillPlanLevelStepExpander.orderCompactTargets(
            levelsBySkillTypeId = levelsBySkillTypeId,
            flattenedPrerequisitesFor = { flattenedBySkill[it].orEmpty() },
            directPrerequisitesFor = { directBySkill[it].orEmpty() },
        )
    }

    private suspend fun expandToLevelSteps(entries: List<SkillPlanEntry>): List<SkillPlanEntry> {
        if (entries.isEmpty()) return emptyList()
        val flattenedBySkill = mutableMapOf<Int, Map<Int, Int>>()
        val directBySkill = mutableMapOf<Int, Map<Int, Int>>()
        loadPrerequisiteCaches(
            skillTypeIds = entries.map { it.skillTypeId },
            flattenedBySkill = flattenedBySkill,
            directBySkill = directBySkill,
        )
        return SkillPlanLevelStepExpander.expand(
            entries = entries,
            flattenedPrerequisitesFor = { flattenedBySkill[it].orEmpty() },
            directPrerequisitesFor = { directBySkill[it].orEmpty() },
        )
    }

    private suspend fun loadPrerequisiteCaches(
        skillTypeIds: Collection<Int>,
        flattenedBySkill: MutableMap<Int, Map<Int, Int>>,
        directBySkill: MutableMap<Int, Map<Int, Int>>,
    ) {
        suspend fun loadFlattened(skillTypeId: Int): Map<Int, Int> {
            flattenedBySkill[skillTypeId]?.let { return it }
            return prerequisiteResolver.requiredLevels(skillTypeId).also {
                flattenedBySkill[skillTypeId] = it
            }
        }

        suspend fun loadDirect(skillTypeId: Int): Map<Int, Int> {
            directBySkill[skillTypeId]?.let { return it }
            return prerequisiteResolver.directRequiredLevels(skillTypeId).also {
                directBySkill[skillTypeId] = it
            }
        }

        for (skillTypeId in skillTypeIds) {
            loadFlattened(skillTypeId)
        }
        val relatedSkillIds = buildSet {
            addAll(skillTypeIds)
            flattenedBySkill.values.forEach { addAll(it.keys) }
        }
        for (skillTypeId in relatedSkillIds) {
            loadFlattened(skillTypeId)
            loadDirect(skillTypeId)
        }
    }
}
