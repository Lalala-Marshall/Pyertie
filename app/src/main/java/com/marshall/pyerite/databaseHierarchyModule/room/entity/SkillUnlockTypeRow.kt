package com.marshall.pyerite.databaseHierarchyModule.room.entity

import com.marshall.pyerite.localization.LocalizableName

data class SkillUnlockTypeRow(
    val typeId: Int,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val iconFilename: String?,
    val categoryId: Int?,
    val categoryName: String?,
) : LocalizableName
