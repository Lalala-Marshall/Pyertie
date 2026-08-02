package com.marshall.pyerite.characterSkillsModule.model

import kotlinx.serialization.Serializable

/**
 * App-wide skill plan row (shared by all logged-in characters).
 * [skillCount] stays 0 until plan entries are stored.
 */
@Serializable
data class SkillPlanListItem(
    val id: Long,
    val name: String,
    val skillCount: Int = 0,
)
