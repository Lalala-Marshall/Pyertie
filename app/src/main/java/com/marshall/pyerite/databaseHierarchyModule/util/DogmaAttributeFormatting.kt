package com.marshall.pyerite.databaseHierarchyModule.util

import java.text.NumberFormat
import java.util.Locale

internal object DogmaAttributeFormatting {
    private val formatter = NumberFormat.getNumberInstance(Locale.US)

    fun format(rawValue: Double?, unitName: String?): String {
        val value = rawValue ?: 0.0
        if (unitName != null && unitName.contains("=")) {
            val mapping = unitName.split(Regex("\\s+"))
                .mapNotNull { it.trim().split("=").takeIf { parts -> parts.size == 2 } }
                .associate { it[0].trim() to it[1].trim() }

            val key = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
            mapping[key]?.let { return it }
        }
        return "${formatter.format(value)} ${unitName ?: ""}".trim()
    }
}
