package com.marshall.pyerite.sdeModule.room.npc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "agents",
    indices = [
        Index(value = ["corporationID"], name = "idx_agents_corporationID"),
        Index(value = ["locationID"], name = "idx_agents_locationID"),
        Index(value = ["solarSystemID"], name = "idx_agents_solarSystemID"),
    ],
)
data class AgentEntity(
    @PrimaryKey
    @ColumnInfo(name = "agent_id")
    val id: Int,
    @ColumnInfo(name = "agent_type") val agentType: Int? = null,
    val corporationID: Long? = null,
    val divisionID: Int? = null,
    val isLocator: Int? = null,
    val level: Int? = null,
    val locationID: Long? = null,
    val solarSystemID: Int? = null,
    @ColumnInfo(name = "agent_name") val agentName: String? = null,
)
