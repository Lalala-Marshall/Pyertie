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
    val skills: List<EsiCharacterSkillDto> = emptyList(),
)

@Serializable
internal data class EsiCharacterSkillDto(
    @SerialName("skill_id") val skillId: Int,
    @SerialName("active_skill_level") val activeSkillLevel: Int = 0,
    @SerialName("trained_skill_level") val trainedSkillLevel: Int = 0,
    @SerialName("skillpoints_in_skill") val skillpointsInSkill: Long = 0L,
)

@Serializable
internal data class EsiCharacterClonesDto(
    @SerialName("home_location") val homeLocation: EsiCloneLocationDto? = null,
    @SerialName("jump_clones") val jumpClones: List<EsiJumpCloneDto> = emptyList(),
    @SerialName("last_clone_jump_date") val lastCloneJumpDate: String? = null,
    @SerialName("last_station_change_date") val lastStationChangeDate: String? = null,
)

@Serializable
internal data class EsiCloneLocationDto(
    @SerialName("location_id") val locationId: Long,
    @SerialName("location_type") val locationType: String,
)

@Serializable
internal data class EsiJumpCloneDto(
    @SerialName("jump_clone_id") val jumpCloneId: Int,
    @SerialName("location_id") val locationId: Long,
    @SerialName("location_type") val locationType: String,
    val implants: List<Int> = emptyList(),
    val name: String? = null,
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
internal data class EsiCharacterAttributesDto(
    val perception: Int = 0,
    val memory: Int = 0,
    val willpower: Int = 0,
    val intelligence: Int = 0,
    val charisma: Int = 0,
    @SerialName("bonus_remaps") val bonusRemaps: Int = 0,
    @SerialName("last_remap_date") val lastRemapDate: String? = null,
    @SerialName("accrued_remap_cooldown_date") val accruedRemapCooldownDate: String? = null,
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

@Serializable
internal data class EsiMailHeaderDto(
    @SerialName("mail_id") val mailId: Long,
    val subject: String? = null,
    val from: Long? = null,
    val timestamp: String? = null,
    @SerialName("is_read") val isRead: Boolean? = null,
    val labels: List<Int> = emptyList(),
    val recipients: List<EsiMailRecipientDto> = emptyList(),
)

@Serializable
internal data class EsiMailRecipientDto(
    @SerialName("recipient_id") val recipientId: Long,
    @SerialName("recipient_type") val recipientType: String,
)

@Serializable
internal data class EsiMailingListDto(
    @SerialName("mailing_list_id") val mailingListId: Long,
    val name: String,
)

@Serializable
internal data class EsiUniverseNameDto(
    val id: Long,
    val name: String,
    val category: String,
)

/** Wire `category` values from POST `/universe/names`. */
internal object EsiUniverseNameCategory {
    const val CHARACTER = "character"
    const val CORPORATION = "corporation"
    const val ALLIANCE = "alliance"
}

internal data class EsiOrganization(
    val id: Long,
    val name: String,
    val ticker: String?,
)
