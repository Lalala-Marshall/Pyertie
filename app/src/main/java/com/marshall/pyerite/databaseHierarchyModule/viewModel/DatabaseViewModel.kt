package com.marshall.pyerite.databaseHierarchyModule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DatabaseViewModel(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    init {
        viewModelScope.launch {
            repository.getCategories()
                .collect { list ->
                    _categories.value = list
                }
        }
    }

    // TODO: 后续再做 groups / types
}