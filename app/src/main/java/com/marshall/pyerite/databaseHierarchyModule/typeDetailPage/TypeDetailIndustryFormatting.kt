package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import java.text.NumberFormat
import java.util.Locale

internal fun formatRefiningSourceLabel(
    name: String?,
    metaGroupId: Int?,
    metaGroupNameById: Map<Int, String?>,
): String {
    val baseName = name.orEmpty()
    if (metaGroupId == null) return baseName
    val metaLabel = metaGroupNameById[metaGroupId]?.takeIf { it.isNotBlank() }
        ?: "$metaGroupId"
    return "$baseName ($metaLabel)"
}

internal fun formatIndustryCount(count: Int): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
