package com.marshall.pyerite.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marshall.pyerite.databaseHierarchyModule.room.dao.CategoryDao
import com.marshall.pyerite.databaseHierarchyModule.room.dao.GroupDao
import com.marshall.pyerite.databaseHierarchyModule.room.dao.MetaGroupDao
import com.marshall.pyerite.databaseHierarchyModule.room.dao.TraitDao
import com.marshall.pyerite.databaseHierarchyModule.room.dao.TypeDao
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintCopyingMaterialEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintInventionMaterialEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingMaterialEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingOutputEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintResearchMaterialMaterialEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintResearchTimeMaterialEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.CategoryEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaCategoryEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.GroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.MetaGroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TraitEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeMaterialEntity

@Database(
    entities = [
        CategoryEntity::class,
        GroupEntity::class,
        TypeEntity::class,
        MetaGroupEntity::class,
        DogmaAttributeEntity::class,
        DogmaCategoryEntity::class,
        TypeAttributeEntity::class,
        TraitEntity::class,
        BlueprintManufacturingOutputEntity::class,
        BlueprintManufacturingMaterialEntity::class,
        BlueprintInventionMaterialEntity::class,
        BlueprintCopyingMaterialEntity::class,
        BlueprintResearchMaterialMaterialEntity::class,
        BlueprintResearchTimeMaterialEntity::class,
        TypeMaterialEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun groupDao(): GroupDao
    abstract fun typeDao(): TypeDao
    abstract fun metaGroupDao(): MetaGroupDao
    abstract fun traitDao(): TraitDao
}
