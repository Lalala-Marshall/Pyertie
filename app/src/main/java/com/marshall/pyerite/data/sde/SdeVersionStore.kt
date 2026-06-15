package com.marshall.pyerite.data.sde

import android.content.Context
import androidx.core.content.edit

class SdeVersionStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): SdeReleaseMeta? {
        if (!prefs.contains(KEY_BUILD_NUMBER)) return null
        return SdeReleaseMeta(
            buildNumber = prefs.getString(KEY_BUILD_NUMBER, "0").orEmpty(),
            releaseDate = prefs.getString(KEY_RELEASE_DATE, "").orEmpty(),
            completionTime = prefs.getString(KEY_COMPLETION_TIME, "").orEmpty(),
            iconSha256 = prefs.getString(KEY_ICON_SHA256, null)?.ifBlank { null },
            sdeSha256 = prefs.getString(KEY_SDE_SHA256, null)?.ifBlank { null },
        )
    }

    fun save(meta: SdeReleaseMeta) {
        prefs.edit {
            putString(KEY_BUILD_NUMBER, meta.buildNumber)
            putString(KEY_RELEASE_DATE, meta.releaseDate)
            putString(KEY_COMPLETION_TIME, meta.completionTime)
            if (meta.iconSha256 != null) {
                putString(KEY_ICON_SHA256, meta.iconSha256)
            } else {
                remove(KEY_ICON_SHA256)
            }
            if (meta.sdeSha256 != null) {
                putString(KEY_SDE_SHA256, meta.sdeSha256)
            } else {
                remove(KEY_SDE_SHA256)
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "pyerite_sde_version"
        private const val KEY_BUILD_NUMBER = "build_number"
        private const val KEY_RELEASE_DATE = "release_date"
        private const val KEY_COMPLETION_TIME = "completion_time"
        private const val KEY_ICON_SHA256 = "icon_sha256"
        private const val KEY_SDE_SHA256 = "sde_sha256"
    }
}
