package com.marshall.pyerite.characterSkillsModule.model

import kotlinx.serialization.Serializable

/** One skill target inside an app-wide skill plan. */
@Serializable
data class SkillPlanEntry(
    val skillTypeId: Int,
    val targetLevel: Int,
)

/**
 * App-wide skill plan (shared by all logged-in characters).
 * [skillCount] mirrors [entries] size for list trailing text.
 */
@Serializable
data class SkillPlanListItem(
    val id: Long,
    val name: String,
    val entries: List<SkillPlanEntry> = emptyList(),
) {
    val skillCount: Int
        get() = entries.size
}
