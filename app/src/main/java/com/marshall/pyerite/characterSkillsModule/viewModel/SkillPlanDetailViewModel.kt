package com.marshall.pyerite.characterSkillsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val PLAN_FLOW_STOP_TIMEOUT_MS = 5_000L

/** Detail for one skill plan; [showCompleted] filters finished entries for the selected character. */
class SkillPlanDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SkillPlanRepository,
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

    val showCompleted: StateFlow<Boolean> = repository.showCompleted

    fun setShowCompleted(show: Boolean) {
        repository.setShowCompleted(show)
    }
}
