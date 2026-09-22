package com.marshall.pyerite.corporationModule.members.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/** Local watched member ids, keyed by the logged-in character who marked them. */
internal class CorporationMemberWatchStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val flows = ConcurrentHashMap<Long, MutableStateFlow<Set<Long>>>()

    fun watchIds(viewerCharacterId: Long): StateFlow<Set<Long>> =
        flows.getOrPut(viewerCharacterId) {
            MutableStateFlow(read(viewerCharacterId))
        }.asStateFlow()

    fun toggle(viewerCharacterId: Long, memberId: Long) {
        val flow = flows.getOrPut(viewerCharacterId) {
            MutableStateFlow(read(viewerCharacterId))
        }
        val current = flow.value
        val next = if (memberId in current) current - memberId else current + memberId
        prefs.edit(commit = true) {
            putString(key(viewerCharacterId), next.joinToString(separator = ID_SEPARATOR))
        }
        flow.value = next
    }

    private fun read(viewerCharacterId: Long): Set<Long> {
        val raw = prefs.getString(key(viewerCharacterId), null) ?: return emptySet()
        if (raw.isBlank()) return emptySet()
        return raw.split(ID_SEPARATOR)
            .mapNotNull { token -> token.trim().toLongOrNull() }
            .toSet()
    }

    private fun key(viewerCharacterId: Long): String = "$KEY_PREFIX$viewerCharacterId"

    private companion object {
        const val PREFS_NAME = "pyerite_corporation_member_watch"
        const val KEY_PREFIX = "watched_"
        const val ID_SEPARATOR = ","
    }
}
