package com.marshall.pyerite.characterSkillsModule.viewModel

import androidx.lifecycle.ViewModel
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanListItem
import kotlinx.coroutines.flow.StateFlow

/** Skill-plan list UI; plans are app-wide, not bound to the selected character. */
class SkillPlanViewModel(
    private val repository: SkillPlanRepository,
) : ViewModel() {

    val plans: StateFlow<List<SkillPlanListItem>> = repository.plans

    fun addPlan(name: String) {
        repository.addPlan(name)
    }
}
