package com.marshall.pyerite.sdeModule.room.skill

/**
 * Minimal skill type name row for plan import / export matching.
 */
data class SkillNameRow(
    val typeId: Int,
    val zhName: String?,
    val enName: String?,
    val name: String?,
)
