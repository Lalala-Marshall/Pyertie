package com.marshall.pyerite.databaseHierarchyModule.viewModel

import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DatabaseRepository(roomProvider: RoomProvider) {

    private val categoryDao = roomProvider.getDatabase().categoryDao()
    fun getCategories(): Flow<List<CategoryEntity>> = flow {
        emit(categoryDao.getCategories())
    }.flowOn(Dispatchers.IO)


    private val groupDao = roomProvider.getDatabase().groupDao()
    fun getGroups(categoryId: Int): Flow<List<GroupEntity>> = flow {
        emit(groupDao.getGroupsByCategory(categoryId))
    }.flowOn(Dispatchers.IO)

    private val typeDao = roomProvider.getDatabase().typeDao()
    fun getTypes(groupId: Int): Flow<List<TypeEntity>> = flow {
        emit(typeDao.getTypesByGroup(groupId))
    }.flowOn(Dispatchers.IO)

    fun getType(typeId: Int): Flow<TypeEntity?> = flow {
        emit(typeDao.getTypeById(typeId))
    }.flowOn(Dispatchers.IO)
}
