package com.marshall.pyerite.databaseHierarchyModule.room.entity

/**
 * Row from [com.marshall.pyerite.databaseHierarchyModule.room.dao.TraitDao.getTraitsForType]
 * — `traits` joined with `types` for skill name (when [skill] != -1).
 */
data class TypeTraitDetail(
    val content: String,
    val skill: Int,
    val importance: Int?,
    val bonusType: String?,
    val skillZhName: String?,
    val skillEnName: String?,
)
