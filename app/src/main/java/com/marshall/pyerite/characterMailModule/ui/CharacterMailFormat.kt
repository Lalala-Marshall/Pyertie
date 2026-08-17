package com.marshall.pyerite.characterMailModule.ui

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.text.style.URLSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.text.HtmlCompat
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMailModule.model.CharacterMailMailbox
import com.marshall.pyerite.esiModule.model.EsiDateTimeConfig
import com.marshall.pyerite.esiModule.model.EsiMailLabelId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun mailSenderHint(senderName: String): AnnotatedString {
    val hintColor = colorResource(R.color.hint_text)
    return buildAnnotatedString {
        withStyle(SpanStyle(color = hintColor, fontWeight = FontWeight.Bold)) {
            append(stringResource(R.string.character_mail_sender_label))
        }
        withStyle(SpanStyle(color = hintColor)) {
            append(stringResource(R.string.character_mail_sender_separator))
            append(senderName)
        }
    }
}

@Composable
internal fun mailRecipientsLabel(): String {
    return stringResource(R.string.character_mail_recipients_label) +
        stringResource(R.string.character_mail_recipients_separator)
}

@Composable
internal fun mailboxDisplayName(
    mailbox: CharacterMailMailbox,
    placeholder: String,
): String {
    val localized = when (mailbox.labelId) {
        EsiMailLabelId.INBOX -> stringResource(R.string.character_mail_label_inbox)
        EsiMailLabelId.SENT -> stringResource(R.string.character_mail_label_sent)
        EsiMailLabelId.CORPORATION -> stringResource(R.string.character_mail_label_corporation)
        EsiMailLabelId.ALLIANCE -> stringResource(R.string.character_mail_label_alliance)
        else -> null
    }
    return localized ?: mailbox.name?.takeIf { it.isNotBlank() } ?: placeholder
}

/** ESI timestamps are UTC; show them in the device local timezone. */
internal fun formatMailReceivedAt(epochMs: Long): String {
    return SimpleDateFormat(
        EsiDateTimeConfig.DISPLAY_DATE_TIME_PATTERN,
        Locale.US,
    ).apply {
        timeZone = TimeZone.getDefault()
    }.format(Date(epochMs))
}

/** Parse ESI mail HTML; drop link/font color and underlines so the body matches page text. */
internal fun mailBodyWithoutLinkStyling(html: String): CharSequence {
    val spannable = SpannableString(
        HtmlCompat.fromHtml(
            htmlWithPreservedLineBreaks(html),
            HtmlCompat.FROM_HTML_MODE_COMPACT,
        ),
    )
    spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
        .forEach(spannable::removeSpan)
    spannable.getSpans(0, spannable.length, UnderlineSpan::class.java)
        .forEach(spannable::removeSpan)
    spannable.getSpans(0, spannable.length, URLSpan::class.java)
        .forEach(spannable::removeSpan)
    return spannable
}

internal fun mailBodyPlainText(html: String): String {
    if (html.isBlank()) return ""
    return HtmlCompat.fromHtml(
        htmlWithPreservedLineBreaks(html),
        HtmlCompat.FROM_HTML_MODE_COMPACT,
    )
        .toString()
        .trim()
}

/** ESI / in-game mail often uses raw newlines instead of {@code <br>}; HTML parsing would collapse them. */
private fun htmlWithPreservedLineBreaks(html: String): String {
    if (html.isEmpty()) return html
    val normalized = html
        .replace(MailBodyHtml.CRLF, MailBodyHtml.NEWLINE)
        .replace(MailBodyHtml.CARRIAGE_RETURN, MailBodyHtml.NEWLINE)
    return normalized.replace(MailBodyHtml.NEWLINE, MailBodyHtml.BREAK_TAG)
}

private object MailBodyHtml {
    const val BREAK_TAG = "<br>"
    const val NEWLINE = "\n"
    const val CARRIAGE_RETURN = "\r"
    const val CRLF = "\r\n"
}
