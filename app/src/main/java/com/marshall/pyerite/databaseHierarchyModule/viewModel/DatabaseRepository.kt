package com.marshall.pyerite.databaseHierarchyModule.viewModel

import androidx.sqlite.db.SimpleSQLiteQuery
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
        val sql = """
            SELECT
                category_id AS id,
                name,
                zh_name AS zhName,
                icon_filename AS iconFilename,
                CAST(published AS INTEGER) AS publishedInt
            FROM categories
            WHERE published != 0
            ORDER BY category_id
        """
        emit(
            categoryDao.getCategoriesRaw(
                query = SimpleSQLiteQuery(sql)
            )
        )
    }.flowOn(Dispatchers.IO)


    private val groupDao = roomProvider.getDatabase().groupDao()
    fun getGroups(categoryId: Int): Flow<List<GroupEntity>> = flow {
        val sql = """
        SELECT
            group_id AS id,
            categoryID AS categoryId,
            name,
            zh_name AS zhName,
            iconID,
            icon_filename AS iconFilename,
            CAST(published AS INTEGER) AS publishedInt
        FROM groups
        WHERE categoryID = ?
          AND published != 0
        ORDER BY group_id
    """
        emit(
            groupDao.getGroupsByCategory(
                SimpleSQLiteQuery(sql, arrayOf(categoryId))
            )
        )
    }.flowOn(Dispatchers.IO)

    private val typeDao = roomProvider.getDatabase().typeDao()
    fun getTypes(groupId: Int): Flow<List<TypeEntity>> = flow {
        val sql = """
        SELECT
            type_id AS id,
            groupID AS groupId,
            name,
            zh_name AS zhName,
            icon_filename AS iconFilename,
            CAST(published AS INTEGER) AS publishedInt
        FROM types
        WHERE groupID = ?
          AND published != 0
        ORDER BY type_id
    """
        emit(typeDao.getTypesByGroup(SimpleSQLiteQuery(sql, arrayOf(groupId))))
    }.flowOn(Dispatchers.IO)


}