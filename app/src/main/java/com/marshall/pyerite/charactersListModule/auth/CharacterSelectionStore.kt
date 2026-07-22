package com.marshall.pyerite.charactersListModule.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Non-secret preference: which character is currently selected. */
internal class CharacterSelectionStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCurrentCharacterId(): Long? {
        if (!prefs.contains(KEY_CURRENT_ID)) return null
        val id = prefs.getLong(KEY_CURRENT_ID, NO_ID)
        return id.takeUnless { it == NO_ID }
    }

    fun setCurrentCharacterId(characterId: Long?) {
        prefs.edit {
            if (characterId == null) {
                remove(KEY_CURRENT_ID)
            } else {
                putLong(KEY_CURRENT_ID, characterId)
            }
        }
    }

    private companion object {
        const val PREFS_NAME = "pyerite_character_selection"
        const val KEY_CURRENT_ID = "current_character_id"
        const val NO_ID = -1L
    }
}
