package com.marshall.pyerite.characterMailModule.model

internal data class CharacterMailInbox(
    val characterId: Long,
    val mails: List<CharacterMailHeader>,
) {
    companion object {
        fun empty(characterId: Long) = CharacterMailInbox(
            characterId = characterId,
            mails = emptyList(),
        )
    }
}

internal data class CharacterMailHeader(
    val mailId: Long,
    val subject: String,
    val senderName: String?,
    val senderPortraitUrl: String?,
    val receivedAtEpochMs: Long?,
)
