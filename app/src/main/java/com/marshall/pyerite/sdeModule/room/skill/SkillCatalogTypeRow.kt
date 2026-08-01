package com.marshall.pyerite.sdeModule.room.skill

import com.marshall.pyerite.localization.LocalizableName

/**
 * Published skill type with dogma [skillTimeConstant] for catalog SP totals,
 * plus primary/secondary attribute type ids for training-time estimates.
 * Room projection — all properties in the primary constructor.
 */
data class SkillCatalogTypeRow(
    val typeId: Int,
    val groupId: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val skillTimeConstant: Double,
    /** Dogma `primaryAttribute` value (EVE attribute type id), or null if missing. */
    val primaryAttributeId: Double?,
    /** Dogma `secondaryAttribute` value (EVE attribute type id), or null if missing. */
    val secondaryAttributeId: Double?,
    val iconFilename: String?,
) : LocalizableName
