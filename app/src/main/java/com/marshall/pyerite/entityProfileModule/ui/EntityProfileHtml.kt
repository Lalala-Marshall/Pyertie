package com.marshall.pyerite.entityProfileModule.ui

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.text.style.URLSpan
import androidx.core.text.HtmlCompat

internal fun entityProfileHtmlToCharSequence(html: String): CharSequence {
    if (html.isBlank()) return ""
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

private fun htmlWithPreservedLineBreaks(html: String): String {
    val normalized = html
        .replace(EntityProfileHtml.CRLF, EntityProfileHtml.NEWLINE)
        .replace(EntityProfileHtml.CARRIAGE_RETURN, EntityProfileHtml.NEWLINE)
    return normalized.replace(EntityProfileHtml.NEWLINE, EntityProfileHtml.BREAK_TAG)
}

private object EntityProfileHtml {
    const val BREAK_TAG = "<br>"
    const val NEWLINE = "\n"
    const val CRLF = "\r\n"
    const val CARRIAGE_RETURN = "\r"
}
