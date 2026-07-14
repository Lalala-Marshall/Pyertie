package com.marshall.pyerite.characterModule.model

data class CharacterSummary(
    val characterId: Long,
    val name: String,
    val portraitUrl: String?,
    val corporationName: String?,
    val corporationIconUrl: String?,
    val allianceName: String?,
    val allianceIconUrl: String?,
)

data class SkillQueueProgress(
    val skillName: String,
    val level: Int,
    val progress: Float,
    val timeRemaining: String,
)

data class LoggedInCharacter(
    val characterId: Long,
    val name: String,
    val portraitUrl: String?,
    val securityStatus: Double?,
    val location: String?,
    val locationStatus: String?,
    val walletBalance: String?,
    val totalSkillPoints: String?,
    val unallocatedSkillPoints: String?,
    val corporationName: String?,
    val corporationIconUrl: String?,
    val allianceName: String?,
    val allianceIconUrl: String?,
    val skillQueue: SkillQueueProgress?,
)
