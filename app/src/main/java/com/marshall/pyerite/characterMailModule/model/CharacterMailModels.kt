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

internal data class CharacterMailDetail(
    val mailId: Long,
    val subject: String,
    val bodyHtml: String,
    val senderName: String?,
    val senderPortraitUrl: String?,
    val receivedAtEpochMs: Long?,
    val recipients: List<CharacterMailParticipant>,
) {
    companion object {
        fun seed(
            mailId: Long,
            header: CharacterMailHeader?,
        ) = CharacterMailDetail(
            mailId = mailId,
            subject = header?.subject.orEmpty(),
            bodyHtml = "",
            senderName = header?.senderName,
            senderPortraitUrl = header?.senderPortraitUrl,
            receivedAtEpochMs = header?.receivedAtEpochMs,
            recipients = emptyList(),
        )
    }
}

internal data class CharacterMailParticipant(
    val id: Long,
    val name: String?,
    val portraitUrl: String?,
)
