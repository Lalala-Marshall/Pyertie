package com.marshall.pyerite.characterMailModule.ui

import com.marshall.pyerite.characterMailModule.model.CharacterMailComposeDraft
import com.marshall.pyerite.characterMailModule.model.CharacterMailDetail
import com.marshall.pyerite.characterMailModule.model.CharacterMailParticipant
import com.marshall.pyerite.characterMailModule.model.MailComposeAction
import com.marshall.pyerite.characterMailModule.model.MailComposeRecipient
import com.marshall.pyerite.characterMailModule.model.MailRecipientKind

internal fun CharacterMailDetail.toComposeDraft(
    action: MailComposeAction,
    selfCharacterId: Long,
): CharacterMailComposeDraft {
    val quotedBody = quotedOriginalBody(bodyHtml)
    return when (action) {
        MailComposeAction.REPLY -> CharacterMailComposeDraft(
            recipients = replyRecipients(selfCharacterId),
            subject = withMailSubjectPrefix(
                subject,
                CharacterMailComposeDraftConfig.REPLY_SUBJECT_PREFIX,
            ),
            body = quotedBody,
        )
        MailComposeAction.REPLY_ALL -> CharacterMailComposeDraft(
            recipients = replyAllRecipients(selfCharacterId),
            subject = withMailSubjectPrefix(
                subject,
                CharacterMailComposeDraftConfig.REPLY_SUBJECT_PREFIX,
            ),
            body = quotedBody,
        )
        MailComposeAction.FORWARD -> CharacterMailComposeDraft(
            recipients = emptyList(),
            subject = withMailSubjectPrefix(
                subject,
                CharacterMailComposeDraftConfig.FORWARD_SUBJECT_PREFIX,
            ),
            body = quotedBody,
        )
    }
}

private fun CharacterMailDetail.replyRecipients(
    selfCharacterId: Long,
): List<MailComposeRecipient> {
    senderAsRecipient()
        ?.takeUnless { it.id == selfCharacterId }
        ?.let { return listOf(it) }
    return recipients
        .asSequence()
        .filter { it.id != selfCharacterId }
        .map { it.toComposeRecipient() }
        .distinctBy { it.id }
        .take(CharacterMailComposeDraftConfig.REPLY_FALLBACK_RECIPIENT_LIMIT)
        .toList()
}

private fun CharacterMailDetail.replyAllRecipients(
    selfCharacterId: Long,
): List<MailComposeRecipient> {
    val byId = linkedMapOf<Long, MailComposeRecipient>()
    senderAsRecipient()
        ?.takeUnless { it.id == selfCharacterId }
        ?.let { sender -> byId[sender.id] = sender }
    recipients.forEach { party ->
        if (party.id == selfCharacterId) return@forEach
        if (party.id in byId) return@forEach
        byId[party.id] = party.toComposeRecipient()
    }
    return byId.values.toList()
}

private fun CharacterMailDetail.senderAsRecipient(): MailComposeRecipient? {
    val id = senderId ?: return null
    return MailComposeRecipient(
        id = id,
        name = senderName,
        portraitUrl = senderPortraitUrl,
        kind = senderKind ?: MailRecipientKind.CHARACTER,
    )
}

private fun CharacterMailParticipant.toComposeRecipient() = MailComposeRecipient(
    id = id,
    name = name,
    portraitUrl = portraitUrl,
    kind = kind,
)

private fun withMailSubjectPrefix(subject: String, prefix: String): String {
    val trimmed = subject.trim()
    if (trimmed.startsWith(prefix, ignoreCase = true)) return trimmed
    return prefix + trimmed
}

private fun quotedOriginalBody(html: String): String {
    val original = mailBodyPlainText(html)
    if (original.isEmpty()) return ""
    return buildString {
        append(CharacterMailComposeDraftConfig.LEADING_BLANK)
        append(CharacterMailComposeDraftConfig.QUOTED_SEPARATOR)
        append(CharacterMailComposeDraftConfig.NEWLINE)
        append(original)
    }
}

private object CharacterMailComposeDraftConfig {
    const val REPLY_FALLBACK_RECIPIENT_LIMIT = 1
    const val REPLY_SUBJECT_PREFIX = "Re: "
    const val FORWARD_SUBJECT_PREFIX = "Fwd: "
    const val QUOTED_SEPARATOR = "--------------------------------"
    const val LEADING_BLANK = "\n\n"
    const val NEWLINE = '\n'
}
