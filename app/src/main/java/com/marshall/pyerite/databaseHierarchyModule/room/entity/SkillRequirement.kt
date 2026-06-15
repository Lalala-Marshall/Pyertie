package com.marshall.pyerite.databaseHierarchyModule.room.entity

/**
 * Represents a skill requirement and its nested prerequisites.
 */
data class SkillRequirement(
    val typeId: Int,
    val name: String,
    val level: Int,
    val iconFilename: String?
)
