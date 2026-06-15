package com.marshall.pyerite.databaseHierarchyModule.util

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
    while (cursor < html.length) {
        val lt = html.indexOf('<', cursor)
        if (lt == -1) {
            builder.append(html.substring(cursor))
            return
        }
        if (lt > cursor) {
            builder.append(html.substring(cursor, lt))
        }
        when {
            html.startsWith("<b>", lt, ignoreCase = true) -> {
                val innerStart = lt + 3
                val close = indexOfIgnoreCase(html, "</b>", innerStart)
                if (close == -1) {
                    builder.append(html.substring(lt))
                    return
                }
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendEveTraitHtml(builder, html.substring(innerStart, close), linkColor)
                builder.pop()
                cursor = close + 4
            }
            html.startsWith("<a", lt, ignoreCase = true) -> {
                val gt = html.indexOf('>', lt)
                if (gt == -1) {
                    builder.append(html.substring(lt))
                    return
                }
                val close = indexOfIgnoreCase(html, "</a>", gt + 1)
                if (close == -1) {
                    builder.append(html.substring(lt))
                    return
                }
                val inner = html.substring(gt + 1, close)
                builder.pushStyle(SpanStyle(color = linkColor))
                appendEveTraitHtml(builder, inner, linkColor)
                builder.pop()
                cursor = close + 4
            }
            html.startsWith("</b>", lt, ignoreCase = true) ||
                    html.startsWith("</a>", lt, ignoreCase = true) -> {
                val gt = html.indexOf('>', lt)
                cursor = if (gt == -1) html.length else gt + 1
            }
            else -> {
                val gt = html.indexOf('>', lt)
                cursor = if (gt == -1) html.length else gt + 1
            }
        }
    }
}
