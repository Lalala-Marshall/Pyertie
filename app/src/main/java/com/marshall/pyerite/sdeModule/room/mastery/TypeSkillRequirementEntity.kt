package com.marshall.pyerite.sdeModule.room.mastery

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "typeSkillRequirement",
    primaryKeys = ["typeid", "required_skill_id"],
)
data class TypeSkillRequirementEntity(
    @ColumnInfo(name = "typeid") val typeId: Int,
    @ColumnInfo(name = "typename") val typeName: String? = null,
    @ColumnInfo(name = "typeicon") val typeIcon: String? = null,
    val published: Int? = null,
    @ColumnInfo(name = "categoryID") val categoryId: Int? = null,
    @ColumnInfo(name = "category_name") val categoryName: String? = null,
    @ColumnInfo(name = "required_skill_id") val requiredSkillId: Int,
    @ColumnInfo(name = "required_skill_level") val requiredSkillLevel: Int? = null,
)
