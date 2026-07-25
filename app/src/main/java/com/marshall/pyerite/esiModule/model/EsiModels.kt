package com.marshall.pyerite.esiModule.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EsiCharacterDto(
    val name: String,
    @SerialName("corporation_id") val corporationId: Long? = null,
    @SerialName("alliance_id") val allianceId: Long? = null,
    val birthday: String? = null,
    @SerialName("security_status") val securityStatus: Double? = null,
)

@Serializable
internal data class EsiOrganizationDto(
    val name: String,
    val ticker: String? = null,
)

@Serializable
internal data class EsiCharacterSkillsDto(
    @SerialName("total_sp") val totalSp: Long = 0L,
    @SerialName("unallocated_sp") val unallocatedSp: Long = 0L,
)

@Serializable
internal data class EsiSkillQueueEntryDto(
    @SerialName("skill_id") val skillId: Int,
    @SerialName("finished_level") val finishedLevel: Int,
    @SerialName("queue_position") val queuePosition: Int = 0,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("finish_date") val finishDate: String? = null,
    @SerialName("training_start_sp") val trainingStartSp: Long? = null,
    @SerialName("level_end_sp") val levelEndSp: Long? = null,
    @SerialName("level_start_sp") val levelStartSp: Long? = null,
)

@Serializable
internal data class EsiCharacterLocationDto(
    @SerialName("solar_system_id") val solarSystemId: Long,
    @SerialName("station_id") val stationId: Long? = null,
    @SerialName("structure_id") val structureId: Long? = null,
)

@Serializable
internal data class EsiUniverseSystemDto(
    val name: String,
    @SerialName("security_status") val securityStatus: Double? = null,
    @SerialName("star_id") val starId: Long? = null,
)

@Serializable
internal data class EsiUniverseStarDto(
    @SerialName("type_id") val typeId: Int,
    @SerialName("solar_system_id") val solarSystemId: Long? = null,
)

@Serializable
internal data class EsiUniverseTypeDto(
    val name: String,
)

@Serializable
internal data class EsiCharacterShipDto(
    @SerialName("ship_item_id") val shipItemId: Long,
    @SerialName("ship_type_id") val shipTypeId: Int,
    @SerialName("ship_name") val shipName: String? = null,
)

@Serializable
internal data class EsiCharacterFatigueDto(
    @SerialName("jump_fatigue_expire_date") val jumpFatigueExpireDate: String? = null,
    @SerialName("last_jump_date") val lastJumpDate: String? = null,
    @SerialName("last_update_date") val lastUpdateDate: String? = null,
)

@Serializable
internal data class EsiCharacterMedalDto(
    @SerialName("medal_id") val medalId: Int,
    val title: String = "",
    val description: String = "",
    val reason: String = "",
    val date: String? = null,
    @SerialName("corporation_id") val corporationId: Long? = null,
    @SerialName("issuer_id") val issuerId: Long? = null,
)

@Serializable
internal data class EsiCharacterOnlineDto(
    val online: Boolean = false,
)

@Serializable
internal data class EsiUniverseStructureDto(
    val name: String,
    @SerialName("solar_system_id") val solarSystemId: Long? = null,
    @SerialName("type_id") val typeId: Int? = null,
)

@Serializable
internal data class EsiUniverseStationDto(
    val name: String,
    @SerialName("type_id") val typeId: Int,
    @SerialName("system_id") val systemId: Long? = null,
)

internal data class EsiCharacterPublic(
    val characterId: Long,
    val name: String,
    val corporationId: Long?,
    val allianceId: Long?,
    val birthday: String? = null,
    val securityStatus: Double? = null,
)

internal data class EsiOrganization(
    val id: Long,
    val name: String,
    val ticker: String?,
)
