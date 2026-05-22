package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.Entity

@Entity(
    tableName = "blueprint_process_time",
    primaryKeys = ["blueprintTypeID"],
)
data class BlueprintProcessTimeEntity(
    val blueprintTypeID: Int,
    val blueprintTypeName: String?,
    val blueprintTypeIcon: String?,
    val manufacturing_time: Int?,
    val research_material_time: Int?,
    val research_time_time: Int?,
    val copying_time: Int?,
    val invention_time: Int?,
    val maxRunsPerCopy: Int?,
)
