package com.marshall.pyerite.characterSkillsModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanListItem
import com.marshall.pyerite.infra.network.PyeriteJson

/**
 * App-wide skill-plan list (not keyed by character). All logged-in characters share it.
 */
internal class SkillPlanStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadPlans(): List<SkillPlanListItem> {
        val raw = prefs.getString(KEY_PLANS, null) ?: return emptyList()
        return runCatching {
            PyeriteJson.decodeFromString<List<SkillPlanListItem>>(raw)
        }.getOrElse { emptyList() }
    }

    fun savePlans(plans: List<SkillPlanListItem>) {
        val encoded = PyeriteJson.encodeToString(plans)
        prefs.edit(commit = true) {
            putString(KEY_PLANS, encoded)
        }
    }

    private companion object {
        const val PREFS_NAME = "pyerite_skill_plans"
        const val KEY_PLANS = "plans"
    }
}
