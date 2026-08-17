package com.marshall.pyerite.characterMailModule.model

internal data class CharacterMailInbox(
    val characterId: Long,
    val labelId: Int? = null,
    val mails: List<CharacterMailHeader>,
) {
    companion object {
        fun empty(characterId: Long, labelId: Int? = null) = CharacterMailInbox(
            characterId = characterId,
            labelId = labelId,
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
    val senderId: Long?,
    val senderName: String?,
    val senderPortraitUrl: String?,
    val senderKind: MailRecipientKind?,
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
            senderId = null,
            senderName = header?.senderName,
            senderPortraitUrl = header?.senderPortraitUrl,
            senderKind = null,
            receivedAtEpochMs = header?.receivedAtEpochMs,
            recipients = emptyList(),
        )
    }
}

internal data class CharacterMailParticipant(
    val id: Long,
    val name: String?,
    val portraitUrl: String?,
    val kind: MailRecipientKind,
)

internal data class CharacterMailMailboxes(
    val characterId: Long,
    val mailboxes: List<CharacterMailMailbox>,
) {
    companion object {
        fun empty(characterId: Long) = CharacterMailMailboxes(
            characterId = characterId,
            mailboxes = emptyList(),
        )
    }
}

internal data class CharacterMailMailbox(
    val labelId: Int,
    val name: String?,
)

internal enum class MailRecipientKind {
    CHARACTER,
    CORPORATION,
    ALLIANCE,
    MAILING_LIST,
}

internal data class MailComposeRecipient(
    val id: Long,
    val name: String?,
    val portraitUrl: String?,
    val kind: MailRecipientKind,
)

internal enum class MailComposeAction {
    REPLY,
    REPLY_ALL,
    FORWARD,
}

internal data class CharacterMailComposeDraft(
    val recipients: List<MailComposeRecipient> = emptyList(),
    val subject: String = "",
    val body: String = "",
)
