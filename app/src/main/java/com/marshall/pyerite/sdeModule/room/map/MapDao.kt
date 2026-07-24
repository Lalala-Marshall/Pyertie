package com.marshall.pyerite.sdeModule.room.map

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MapDao {
    @Query(
        """
        SELECT
            ss.solarSystemName AS system_name,
            ss.solarSystemName_zh AS system_zh_name,
            ss.solarSystemName_en AS system_en_name,
            COALESCE(u.system_security, ss.security_status) AS security_status,
            r.regionName AS region_name,
            r.regionName_zh AS region_zh_name,
            r.regionName_en AS region_en_name
        FROM solarsystems ss
        LEFT JOIN universe u ON u.solarsystem_id = ss.solarSystemID
        LEFT JOIN regions r ON r.regionID = u.region_id
        WHERE ss.solarSystemID = :solarSystemId
        LIMIT 1
        """,
    )
    suspend fun getSolarSystemLocation(solarSystemId: Long): SolarSystemLocationRow?

    @Query(
        """
        SELECT stationID, stationTypeID, stationName
        FROM stations
        WHERE stationID = :stationId
        LIMIT 1
        """,
    )
    suspend fun getStation(stationId: Long): StationLocationRow?
}
