package com.marshall.pyerite.databaseHierarchyModule.search

import com.marshall.pyerite.sdeModule.room.type.TypeEntity

data class TypeSearchUiState(
    val results: List<TypeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isTruncated: Boolean = false,
    val settledQuery: String = "",
)
