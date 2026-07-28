package com.marshall.pyerite.characterSkillsModule.navHost

sealed class CharacterSkillsRoute(val route: String) {

    object Queue : CharacterSkillsRoute("character/skills/{characterId}") {
        fun create(characterId: Long) = "character/skills/$characterId"
    }
}
