package com.marshall.pyerite.sdeModule.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marshall.pyerite.sdeModule.room.catalog.CategoryDao
import com.marshall.pyerite.sdeModule.room.catalog.CategoryEntity
import com.marshall.pyerite.sdeModule.room.catalog.GroupDao
import com.marshall.pyerite.sdeModule.room.catalog.GroupEntity
import com.marshall.pyerite.sdeModule.room.catalog.MetaGroupDao
import com.marshall.pyerite.sdeModule.room.catalog.MetaGroupEntity
import com.marshall.pyerite.sdeModule.room.dogma.DogmaAttributeEntity
import com.marshall.pyerite.sdeModule.room.dogma.DogmaCategoryEntity
import com.marshall.pyerite.sdeModule.room.dogma.DogmaDao
import com.marshall.pyerite.sdeModule.room.dogma.TraitDao
import com.marshall.pyerite.sdeModule.room.dogma.TraitEntity
import com.marshall.pyerite.sdeModule.room.dogma.TypeAttributeEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintCopyingMaterialEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionMaterialEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionProductEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionSkillEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingMaterialEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingOutputEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingSkillEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintProcessTimeEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintResearchMaterialMaterialEntity
import com.marshall.pyerite.sdeModule.room.industry.BlueprintResearchTimeMaterialEntity
import com.marshall.pyerite.sdeModule.room.industry.IndustryDao
import com.marshall.pyerite.sdeModule.room.industry.TypeMaterialEntity
import com.marshall.pyerite.sdeModule.room.map.MapDao
import com.marshall.pyerite.sdeModule.room.map.RegionEntity
import com.marshall.pyerite.sdeModule.room.map.SolarSystemEntity
import com.marshall.pyerite.sdeModule.room.map.StationEntity
import com.marshall.pyerite.sdeModule.room.map.UniverseLinkEntity
import com.marshall.pyerite.sdeModule.room.skill.SkillDao
import com.marshall.pyerite.sdeModule.room.type.SdeTypeDao
import com.marshall.pyerite.sdeModule.room.type.TypeDao
import com.marshall.pyerite.sdeModule.room.type.TypeEntity

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
        BlueprintManufacturingSkillEntity::class,
        BlueprintProcessTimeEntity::class,
        BlueprintInventionMaterialEntity::class,
        BlueprintInventionProductEntity::class,
        BlueprintInventionSkillEntity::class,
        BlueprintCopyingMaterialEntity::class,
        BlueprintResearchMaterialMaterialEntity::class,
        BlueprintResearchTimeMaterialEntity::class,
        TypeMaterialEntity::class,
        SolarSystemEntity::class,
        RegionEntity::class,
        UniverseLinkEntity::class,
        StationEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun groupDao(): GroupDao
    abstract fun metaGroupDao(): MetaGroupDao
    /** Type browser / variants / fitting group refs. */
    abstract fun typeDao(): TypeDao
    /** Lightweight type id → name/icon (shared by list, sheet, etc.). */
    abstract fun sdeTypeDao(): SdeTypeDao
    abstract fun dogmaDao(): DogmaDao
    abstract fun traitDao(): TraitDao
    abstract fun industryDao(): IndustryDao
    abstract fun skillDao(): SkillDao
    /** Map / station lookups (shared by list, sheet, etc.). */
    abstract fun mapDao(): MapDao
}
