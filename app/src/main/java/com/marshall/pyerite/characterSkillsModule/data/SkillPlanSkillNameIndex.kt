package com.marshall.pyerite.characterSkillsModule.data

import com.marshall.pyerite.characterSkillsModule.model.SkillCatalogConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillPlanImportExport
import com.marshall.pyerite.localization.ContentLanguage
import com.marshall.pyerite.sdeModule.room.RoomProvider
import com.marshall.pyerite.sdeModule.room.skill.SkillNameRow
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Cached skill name ↔ typeId index for plan clipboard import / export.
 */
internal class SkillPlanSkillNameIndex(
    private val roomProvider: RoomProvider,
) {
    private val mutex = Mutex()

    @Volatile
    private var cachedRows: List<SkillNameRow> = emptyList()

    @Volatile
    private var loaded = false

    /** Loads skill names once, then returns a sync lookup snapshot. */
    suspend fun snapshot(): SkillPlanSkillNameSnapshot {
        ensureLoaded()
        return SkillPlanSkillNameSnapshot(cachedRows)
    }

    private suspend fun ensureLoaded() {
        if (loaded) return
        mutex.withLock {
            if (loaded) return
            cachedRows = withContext(Dispatchers.IO) {
                roomProvider.getDatabase().skillDao().getSkillNameRows(
                    categoryId = SkillCatalogConfig.SKILLS_CATEGORY_ID,
                )
            }
            loaded = true
        }
    }
}

/** Sync name lookups after [SkillPlanSkillNameIndex.snapshot]. */
internal class SkillPlanSkillNameSnapshot(
    private val rows: List<SkillNameRow>,
) {
    fun displayName(typeId: Int, language: ContentLanguage): String? {
        val row = rows.firstOrNull { it.typeId == typeId } ?: return null
        return SkillPlanImportExport.displayName(
            language = language,
            zhName = row.zhName,
            enName = row.enName,
            name = row.name,
        )
    }

    fun resolveTypeId(skillName: String): Int? {
        val trimmed = skillName.trim()
        if (trimmed.isEmpty()) return null
        rows.forEach { row ->
            if (row.zhName?.trim() == trimmed) return row.typeId
        }
        val lower = trimmed.lowercase(Locale.ROOT)
        rows.forEach { row ->
            val en = row.enName?.trim().orEmpty()
            if (en.isNotEmpty() && en.lowercase(Locale.ROOT) == lower) return row.typeId
            val fallback = row.name?.trim().orEmpty()
            if (fallback.isNotEmpty() && fallback.lowercase(Locale.ROOT) == lower) {
                return row.typeId
            }
        }
        return null
    }
}
