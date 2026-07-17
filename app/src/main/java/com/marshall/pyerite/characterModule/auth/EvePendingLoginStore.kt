package com.marshall.pyerite.characterModule.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Survives process death while the user is in the browser Custom Tab. */
internal class EvePendingLoginStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(state: String, codeVerifier: String, createdAtEpochMs: Long = System.currentTimeMillis()) {
        prefs.edit {
            putString(KEY_STATE, state)
            putString(KEY_CODE_VERIFIER, codeVerifier)
            putLong(KEY_CREATED_AT, createdAtEpochMs)
        }
    }

    /** Returns code verifier when state matches and TTL is valid; clears storage either way. */
    fun consume(expectedState: String): String? {
        val storedState = prefs.getString(KEY_STATE, null)
        val verifier = prefs.getString(KEY_CODE_VERIFIER, null)
        val createdAt = prefs.getLong(KEY_CREATED_AT, 0L)
        clear()
        if (storedState.isNullOrBlank() || verifier.isNullOrBlank()) return null
        if (storedState != expectedState) return null
        if (System.currentTimeMillis() - createdAt > EveSsoConfig.PENDING_LOGIN_TTL_MS) return null
        return verifier
    }

    fun clear() {
        prefs.edit { clear() }
    }

    fun hasPending(): Boolean {
        val createdAt = prefs.getLong(KEY_CREATED_AT, 0L)
        if (createdAt == 0L) return false
        if (System.currentTimeMillis() - createdAt > EveSsoConfig.PENDING_LOGIN_TTL_MS) {
            clear()
            return false
        }
        return !prefs.getString(KEY_STATE, null).isNullOrBlank()
    }

    private companion object {
        const val PREFS_NAME = "pyerite_eve_sso_pending_login"
        const val KEY_STATE = "state"
        const val KEY_CODE_VERIFIER = "code_verifier"
        const val KEY_CREATED_AT = "created_at"
    }
}
