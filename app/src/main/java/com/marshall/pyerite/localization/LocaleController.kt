package com.marshall.pyerite.localization

import android.content.Context
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import java.util.Locale

class LocaleController(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var preference: AppLanguagePreference
        get() = AppLanguagePreference.entries.getOrNull(
            prefs.getInt(KEY_PREFERENCE, AppLanguagePreference.FOLLOW_SYSTEM.ordinal),
        ) ?: AppLanguagePreference.FOLLOW_SYSTEM
        set(value) {
            prefs.edit { putInt(KEY_PREFERENCE, value.ordinal) }
        }

    /** Resolved language for SDE database selection and bilingual entity name fields. */
    val contentLanguage: ContentLanguage
        get() = when (preference) {
            AppLanguagePreference.CHINESE -> ContentLanguage.CHINESE
            AppLanguagePreference.ENGLISH -> ContentLanguage.ENGLISH
            AppLanguagePreference.FOLLOW_SYSTEM -> ContentLanguage.fromLocale(systemLocale())
        }

    fun resolveDatabaseFileName(): String = SdeDatabase.fileName(contentLanguage)

    private fun systemLocale(): Locale =
        LocaleListCompat.getAdjustedDefault()[0] ?: Locale.getDefault()

    companion object {
        private const val PREFS_NAME = "pyerite_locale"
        private const val KEY_PREFERENCE = "language_preference"
    }
}
