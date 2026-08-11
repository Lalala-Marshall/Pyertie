package com.marshall.pyerite.characterSkillsModule.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanListItem
import kotlinx.coroutines.flow.StateFlow

/** Skill-plan list UI; plans are app-wide, not bound to the selected character. */
class SkillPlanViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SkillPlanRepository,
) : ViewModel() {

    val characterId: Long = checkNotNull(savedStateHandle[NAV_ARG_CHARACTER_ID]) {
        "SkillPlan route requires $NAV_ARG_CHARACTER_ID"
    }

    val plans: StateFlow<List<SkillPlanListItem>> = repository.plans

    fun addPlan(name: String) {
        repository.addPlan(name)
    }

    companion object {
        const val NAV_ARG_CHARACTER_ID = CharacterSkillsViewModel.NAV_ARG_CHARACTER_ID
        const val NAV_ARG_PLAN_ID = "planId"
    }
}
