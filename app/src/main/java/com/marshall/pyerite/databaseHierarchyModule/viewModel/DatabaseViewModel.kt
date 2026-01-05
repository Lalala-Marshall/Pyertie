package com.marshall.pyerite.databaseHierarchyModule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class DatabaseViewModel(
    private val repository: DatabaseRepository
) : ViewModel() {

    val categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun groups(categoryId: Int) =
        repository.getGroups(categoryId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun types(groupId: Int) =
        repository.getTypes(groupId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

}