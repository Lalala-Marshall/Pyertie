package com.marshall.pyerite.data.sde

import android.content.Context
import com.marshall.pyerite.localization.SdeDatabase
import java.io.File

object BundledSdeUpdater {

    fun ensureUpToDate(context: Context) {
        val bundled = SdeReleaseMeta.fromAssets(context)
        if (bundled == null) {
            SdeUpdateLog.d("BundledSde", "no bundled metadata, ensuring databases and icons only")
            SdeAssetFiles.ensureDatabasesPresent(context)
            SdeAssetFiles.ensureIconsPresent(context)
            return
        }

        val versionStore = SdeVersionStore(context)
        val installed = versionStore.load()
        val databasesPresent = SdeAssetFiles.areDatabasesPresent(context)
        val iconsPresent = File(context.filesDir, SdeAssetFiles.ICONS_DIR_NAME).exists()

        when {
            !databasesPresent -> {
                SdeUpdateLog.d("BundledSde", "databases missing, applying bundled ${bundled.buildNumber}")
                applyBundledAssets(context, bundled)
            }
            installed == null -> {
                SdeUpdateLog.d("BundledSde", "no installed version, applying bundled ${bundled.buildNumber}")
                applyBundledAssets(context, bundled)
            }
            SdeVersionComparator.needsUpgrade(bundled, installed) -> {
                SdeUpdateLog.d(
                    "BundledSde",
                    "bundled newer than installed (${bundled.buildNumber} > ${installed.buildNumber}), applying",
                )
                applyBundledAssets(context, bundled)
            }
            SdeVersionComparator.needsUpgrade(installed, bundled) &&
                !iconsPresent -> {
                SdeUpdateLog.d("BundledSde", "icons missing with stale prefs, re-applying bundled ${bundled.buildNumber}")
                applyBundledAssets(context, bundled)
            }
            else -> {
                SdeUpdateLog.d(
                    "BundledSde",
                    "local up to date build=${installed.buildNumber} icons=${installed.iconVersion}",
                )
                SdeAssetFiles.ensureDatabasesPresent(context)
                SdeAssetFiles.ensureIconsPresent(context)
            }
        }
    }

    private fun applyBundledAssets(context: Context, bundled: SdeReleaseMeta) {
        SdeAssetFiles.deleteAllDatabases(context)
        SdeAssetFiles.copyDatabaseFromAssets(context, SdeDatabase.ZH_FILE_NAME)
        SdeAssetFiles.copyDatabaseFromAssets(context, SdeDatabase.EN_FILE_NAME)
        SdeAssetFiles.extractIconsFromAssets(context)
        SdeVersionStore(context).save(bundled)
    }
}
