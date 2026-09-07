package com.marshall.pyerite.sdeModule.room.type

import com.marshall.pyerite.localization.LocalizableName

/** Name + icon projection for resolving a batch of type ids. */
data class TypeDisplayIconRow(
    val id: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
) : LocalizableName
