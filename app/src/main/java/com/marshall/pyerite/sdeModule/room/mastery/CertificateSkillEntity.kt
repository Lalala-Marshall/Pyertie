package com.marshall.pyerite.sdeModule.room.mastery

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "certificateSkills",
    primaryKeys = ["certificateID", "skillID"],
)
data class CertificateSkillEntity(
    @ColumnInfo(name = "certificateID") val certificateId: Int,
    @ColumnInfo(name = "skillID") val skillId: Int,
    @ColumnInfo(name = "basic", defaultValue = "0") val basic: Int = 0,
    @ColumnInfo(name = "standard", defaultValue = "0") val standard: Int = 0,
    @ColumnInfo(name = "improved", defaultValue = "0") val improved: Int = 0,
    @ColumnInfo(name = "advanced", defaultValue = "0") val advanced: Int = 0,
    @ColumnInfo(name = "elite", defaultValue = "0") val elite: Int = 0,
)
