package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Normalizes paragraph breaks: any run of 2+ newlines becomes exactly **one** blank line (`\n\n`).
 * Single `\n` is kept (in-paragraph line breaks). Leading/trailing blank lines trimmed.
 */
internal fun normalizeDescriptionParagraphSpacing(text: String): String =
    text.replace("\r\n", "\n")
        .replace('\r', '\n')
        .trimStart()
        .replace(Regex("\n{2,}"), "\n\n")
        .trimEnd()

private fun stripSimpleHtmlEntities(s: String): String =
    s.replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")

private fun stripNonItalicTags(s: String): String =
    stripSimpleHtmlEntities(s.replace(Regex("<[^>]+>"), ""))

private val italicBlock = Regex(
    pattern = "<i>(.*?)</i>",
    options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
)

fun formatTypeDescriptionAnnotated(raw: String): AnnotatedString {
    val normalized = normalizeDescriptionParagraphSpacing(raw)
    return buildAnnotatedString {
        var pos = 0
        for (m in italicBlock.findAll(normalized)) {
            if (m.range.first > pos) {
                append(stripNonItalicTags(normalized.substring(pos, m.range.first)))
            }
            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            append(stripNonItalicTags(m.groupValues[1]))
            pop()
            pos = m.range.last + 1
        }
        if (pos < normalized.length) {
            append(stripNonItalicTags(normalized.substring(pos)))
        }
    }
}

private fun indexOfIgnoreCase(s: String, needle: String, start: Int): Int =
    s.indexOf(needle, start, ignoreCase = true)

/**
 * Renders EVE trait HTML: `<b>` bold, `<a href=showinfo:…>` colored (links / tap TBD).
 */
fun formatTraitBonusAnnotated(html: String, linkColor: Color): AnnotatedString =
    buildAnnotatedString { appendEveTraitHtml(this, html, linkColor) }

private fun appendEveTraitHtml(
    builder: AnnotatedString.Builder,
    html: String,
    linkColor: Color
) {
    var cursor = 0
    val text = html
    while (cursor < text.length) {
        val lt = text.indexOf('<', cursor)
        if (lt == -1) {
            builder.append(text.substring(cursor))
            return
        }
        if (lt > cursor) {
            builder.append(text.substring(cursor, lt))
        }
        when {
            text.startsWith("<b>", lt, ignoreCase = true) -> {
                val innerStart = lt + 3
                val close = indexOfIgnoreCase(text, "</b>", innerStart)
                if (close == -1) {
                    builder.append(text.substring(lt))
                    return
                }
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendEveTraitHtml(builder, text.substring(innerStart, close), linkColor)
                builder.pop()
                cursor = close + 4
            }
            text.startsWith("<a", lt, ignoreCase = true) -> {
                val gt = text.indexOf('>', lt)
                if (gt == -1) {
                    builder.append(text.substring(lt))
                    return
                }
                val close = indexOfIgnoreCase(text, "</a>", gt + 1)
                if (close == -1) {
                    builder.append(text.substring(lt))
                    return
                }
                val inner = text.substring(gt + 1, close)
                builder.pushStyle(SpanStyle(color = linkColor))
                appendEveTraitHtml(builder, inner, linkColor)
                builder.pop()
                cursor = close + 4
            }
            text.startsWith("</b>", lt, ignoreCase = true) ||
                text.startsWith("</a>", lt, ignoreCase = true) -> {
                val gt = text.indexOf('>', lt)
                cursor = if (gt == -1) text.length else gt + 1
            }
            else -> {
                val gt = text.indexOf('>', lt)
                cursor = if (gt == -1) text.length else gt + 1
            }
        }
    }
}
