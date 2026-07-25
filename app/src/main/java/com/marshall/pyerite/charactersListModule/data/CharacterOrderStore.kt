package com.marshall.pyerite.charactersListModule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Non-secret preference: stable display order of logged-in characters. */
internal class CharacterOrderStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrderedIds(): List<Long> {
        val raw = prefs.getString(KEY_ORDERED_IDS, null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(ID_SEPARATOR)
            .mapNotNull { token -> token.trim().toLongOrNull() }
    }

    fun setOrderedIds(characterIds: List<Long>) {
        prefs.edit(commit = true) {
            putString(
                KEY_ORDERED_IDS,
                characterIds.joinToString(separator = ID_SEPARATOR),
            )
        }
    }

    private companion object {
        const val PREFS_NAME = "pyerite_character_order"
        const val KEY_ORDERED_IDS = "ordered_character_ids"
        const val ID_SEPARATOR = ","
    }
}
