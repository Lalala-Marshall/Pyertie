package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solarsystems")
data class SolarSystemEntity(
    @PrimaryKey
    @ColumnInfo(name = "solarSystemID")
    val id: Int,
    @ColumnInfo(name = "solarSystemName") val name: String? = null,
    @ColumnInfo(name = "solarSystemName_zh") val zhName: String? = null,
    @ColumnInfo(name = "solarSystemName_en") val enName: String? = null,
    @ColumnInfo(name = "security_status") val securityStatus: Double? = null,
)

@Entity(tableName = "regions")
data class RegionEntity(
    @PrimaryKey
    @ColumnInfo(name = "regionID")
    val id: Int,
    @ColumnInfo(name = "regionName") val name: String? = null,
    @ColumnInfo(name = "regionName_zh") val zhName: String? = null,
    @ColumnInfo(name = "regionName_en") val enName: String? = null,
)

@Entity(
    tableName = "universe",
    primaryKeys = ["region_id", "constellation_id", "solarsystem_id"],
)
data class UniverseLinkEntity(
    @ColumnInfo(name = "region_id") val regionId: Int,
    @ColumnInfo(name = "constellation_id") val constellationId: Int,
    @ColumnInfo(name = "solarsystem_id") val solarSystemId: Int,
    @ColumnInfo(name = "system_security") val systemSecurity: Double? = null,
)

/**
 * Joined solar-system + region row for character location display.
 * Not a table entity — Room @Query projection only.
 */
data class SolarSystemLocationRow(
    @ColumnInfo(name = "system_name") val systemName: String?,
    @ColumnInfo(name = "system_zh_name") val systemZhName: String?,
    @ColumnInfo(name = "system_en_name") val systemEnName: String?,
    @ColumnInfo(name = "security_status") val securityStatus: Double?,
    @ColumnInfo(name = "region_name") val regionName: String?,
    @ColumnInfo(name = "region_zh_name") val regionZhName: String?,
    @ColumnInfo(name = "region_en_name") val regionEnName: String?,
)
