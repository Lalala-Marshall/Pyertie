package com.marshall.pyerite.sdeModule.room.dogma

/**
 * A Data Transfer Object for carrying detailed information about a Type's attributes,
 * including names, units, and categories.
 */
data class TypeAttributeDetail(
    val attributeId: Int,
    val value: Double?,
    val name: String?,
    val displayName: String?,
    val unitName: String?,
    val iconFilename: String?,
    val categoryId: Int,
    val categoryName: String?
)
