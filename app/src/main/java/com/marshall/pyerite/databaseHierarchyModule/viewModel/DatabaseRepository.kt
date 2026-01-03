package com.marshall.pyerite.databaseHierarchyModule.viewModel

import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DatabaseRepository(roomProvider: RoomProvider) {

    private val categoryDao = roomProvider.getDatabase().categoryDao()

    fun getCategories(): Flow<List<CategoryEntity>> = flow {
        emit(categoryDao.getCategoriesRaw())
    }.flowOn(Dispatchers.IO)
}