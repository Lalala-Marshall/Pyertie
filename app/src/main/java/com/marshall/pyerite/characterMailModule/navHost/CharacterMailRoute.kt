package com.marshall.pyerite.characterMailModule.navHost

sealed class CharacterMailRoute(val route: String) {

    object Root : CharacterMailRoute("character/mail/{characterId}") {
        fun create(characterId: Long) = "character/mail/$characterId"
    }

    object List : CharacterMailRoute(
        "character/mail/{characterId}/list?labelId={labelId}",
    ) {
        fun create(characterId: Long, labelId: Int? = null): String {
            val path = "character/mail/$characterId/list"
            return if (labelId == null) {
                path
            } else {
                "$path?labelId=$labelId"
            }
        }
    }

    object Detail : CharacterMailRoute("character/mail/{characterId}/mail/{mailId}") {
        fun create(characterId: Long, mailId: Long) = "character/mail/$characterId/mail/$mailId"
    }
}