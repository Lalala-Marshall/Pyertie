package com.marshall.pyerite.corporationModule.members.navHost

sealed class CorporationMembersRoute(val route: String) {
    object Members : CorporationMembersRoute("corporation/members/{characterId}") {
        fun create(characterId: Long) = "corporation/members/$characterId"
    }

    object Watched : CorporationMembersRoute("corporation/members/{characterId}/watched") {
        fun create(characterId: Long) = "corporation/members/$characterId/watched"
    }
}
