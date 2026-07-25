package com.marshall.pyerite.sdeModule.room.dogma

/**
 * Row from [TraitDao.getTraitsForType]
 * — `traits` joined with `types` for skill name (when [skill] != -1).
 */
data class TypeTraitDetail(
    val content: String,
    val skill: Int,
    val importance: Int?,
    val bonusType: String?,
    val zhName: String?,
    val enName: String?,
)
