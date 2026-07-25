package com.marshall.pyerite.sdeModule.room.type

import com.marshall.pyerite.localization.LocalizableName

/** chargeGroup* / launcherGroup* dogma row before group lookup. */
data class TypeCompatibleGroupRef(
    val attributeId: Int,
    val attributeName: String?,
    val attributeDisplayName: String?,
    val attributeIconFilename: String?,
    val groupId: Int,
)

/** A launcher/charge group this type is compatible with (from chargeGroup* / launcherGroup* dogma). */
data class TypeCompatibleGroupDetail(
    val attributeId: Int,
    val attributeName: String?,
    val attributeDisplayName: String?,
    val attributeIconFilename: String?,
    val groupId: Int,
    override val zhName: String?,
    override val enName: String?,
    override val name: String?,
    val groupIconFilename: String?,
) : LocalizableName
