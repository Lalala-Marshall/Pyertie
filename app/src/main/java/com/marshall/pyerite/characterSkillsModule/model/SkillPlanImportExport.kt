package com.marshall.pyerite.characterSkillsModule.model

import com.marshall.pyerite.localization.ContentLanguage

/**
 * Formats / parses skill-plan clipboard text:
 * one `Name Level` line per finished level (1–5), zh or en skill names.
 */
internal object SkillPlanImportExport {

    private val LINE_PATTERN = Regex("""^(.*) ([1-5])$""")

    fun format(
        steps: List<SkillPlanEntry>,
        displayNameFor: (skillTypeId: Int) -> String?,
    ): String {
        if (steps.isEmpty()) return ""
        return buildString {
            for (step in steps) {
                val name = displayNameFor(step.skillTypeId)?.trim().orEmpty()
                if (name.isEmpty()) continue
                val level = step.targetLevel.coerceIn(1, SkillCatalogConfig.MAX_SKILL_LEVEL)
                append(name)
                append(' ')
                append(level)
                append('\n')
            }
        }.trimEnd()
    }

    /**
     * Parses clipboard text into compact plan entries (max level per skill, first-seen order).
     * Accepts Chinese and/or English skill names mixed in one paste.
     *
     * @return null when text is blank after trim, empty list is not used — caller checks blank first.
     * @throws SkillPlanImportFormatException when any non-blank line is invalid or unknown.
     */
    fun parse(
        text: String,
        resolveTypeId: (skillName: String) -> Int?,
    ): List<SkillPlanEntry> {
        val lines = text
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            throw SkillPlanImportFormatException()
        }

        val maxByTypeId = linkedMapOf<Int, Int>()
        for (line in lines) {
            val match = LINE_PATTERN.matchEntire(line) ?: throw SkillPlanImportFormatException()
            val skillName = match.groupValues[1].trim()
            val level = match.groupValues[2].toInt()
            if (skillName.isEmpty()) throw SkillPlanImportFormatException()
            val typeId = resolveTypeId(skillName) ?: throw SkillPlanImportFormatException()
            maxByTypeId[typeId] = maxOf(maxByTypeId[typeId] ?: 0, level)
        }
        return maxByTypeId.map { (typeId, level) ->
            SkillPlanEntry(skillTypeId = typeId, targetLevel = level)
        }
    }

    fun displayName(
        language: ContentLanguage,
        zhName: String?,
        enName: String?,
        name: String?,
    ): String? {
        val primary = when (language) {
            ContentLanguage.CHINESE -> zhName?.trim().orEmpty().ifEmpty { null }
                ?: name?.trim().orEmpty().ifEmpty { null }
                ?: enName?.trim().orEmpty().ifEmpty { null }
            ContentLanguage.ENGLISH -> enName?.trim().orEmpty().ifEmpty { null }
                ?: name?.trim().orEmpty().ifEmpty { null }
                ?: zhName?.trim().orEmpty().ifEmpty { null }
        }
        return primary
    }
}

internal class SkillPlanImportFormatException : Exception()
