package com.marshall.pyerite.sdeModule.room.mastery

import com.marshall.pyerite.localization.LocalizableName

/** Ship type with mastery data (Room projection). */
data class MasteryShipTypeRow(
    val typeId: Int,
    val groupId: Int?,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
    val metaGroupId: Int?,
    val published: Boolean?,
) : LocalizableName
