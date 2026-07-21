package com.marshall.pyerite.databaseHierarchyModule.room.entity

import com.marshall.pyerite.localization.LocalizableName

/** Lightweight name projection for resolving type ids (e.g. skill queue). */
data class TypeDisplayNameRow(
    val id: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
) : LocalizableName
