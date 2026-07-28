package com.marshall.pyerite.characterSkillsModule.navHost

sealed class CharacterSkillsRoute(val route: String) {

    object Queue : CharacterSkillsRoute("character/skills/{characterId}") {
        fun create(characterId: Long) = "character/skills/$characterId"
    }

    object Attributes : CharacterSkillsRoute("character/skills/{characterId}/attributes") {
        fun create(characterId: Long) = "character/skills/$characterId/attributes"
    }

    object CatalogDetails : CharacterSkillsRoute("character/skills/{characterId}/catalog") {
        fun create(characterId: Long) = "character/skills/$characterId/catalog"
    }
}
