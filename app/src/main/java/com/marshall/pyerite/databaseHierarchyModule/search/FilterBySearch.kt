package com.marshall.pyerite.databaseHierarchyModule.search

import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.LocalizableName
import com.marshall.pyerite.localization.displayName

fun LocalizableName.matchesSearchQuery(
    query: String,
    localeController: LocaleController,
): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    return displayName(localeController).contains(trimmed, ignoreCase = true) ||
        zhName?.contains(trimmed, ignoreCase = true) == true ||
        enName?.contains(trimmed, ignoreCase = true) == true ||
        name?.contains(trimmed, ignoreCase = true) == true
}

fun String?.matchesSearchQuery(query: String): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    return this?.contains(trimmed, ignoreCase = true) == true
}

fun <T : LocalizableName> List<T>.matchingSearch(
    query: String,
    localeController: LocaleController,
): List<T> = filter { it.matchesSearchQuery(query, localeController) }
