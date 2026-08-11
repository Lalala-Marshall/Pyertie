package com.marshall.pyerite.characterSkillsModule.navHost

import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogFilter

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

    object SkillPlan : CharacterSkillsRoute("character/skills/{characterId}/skill-plan") {
        fun create(characterId: Long) = "character/skills/$characterId/skill-plan"
    }

    object SkillPlanDetail : CharacterSkillsRoute(
        "character/skills/{characterId}/skill-plan/{planId}",
    ) {
        fun create(characterId: Long, planId: Long) =
            "character/skills/$characterId/skill-plan/$planId"
    }

    object CatalogGroup : CharacterSkillsRoute(
        "character/skills/{characterId}/catalog/{groupId}/{filter}",
    ) {
        fun create(
            characterId: Long,
            groupId: Int,
            filter: SkillCatalogFilter,
        ) = "character/skills/$characterId/catalog/$groupId/${filter.name}"
    }
}
