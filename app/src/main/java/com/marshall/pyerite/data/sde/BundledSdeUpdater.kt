package com.marshall.pyerite.data.sde

import android.content.Context
import com.marshall.pyerite.localization.SdeDatabase

object BundledSdeUpdater {

    fun ensureUpToDate(context: Context) {
        val bundled = SdeReleaseMeta.fromAssets(context)
        if (bundled == null) {
            SdeAssetFiles.ensureDatabasesPresent(context)
            return
        }

        val installed = SdeVersionStore(context).load()
        if (SdeVersionComparator.needsUpgrade(bundled, installed)) {
            applyBundledAssets(context, bundled)
        } else {
            SdeAssetFiles.ensureDatabasesPresent(context)
        }
    }

    private fun applyBundledAssets(context: Context, bundled: SdeReleaseMeta) {
        SdeAssetFiles.deleteAllDatabases(context)
        SdeAssetFiles.copyDatabaseFromAssets(context, SdeDatabase.ZH_FILE_NAME)
        SdeAssetFiles.copyDatabaseFromAssets(context, SdeDatabase.EN_FILE_NAME)
        SdeAssetFiles.deleteIcons(context)
        SdeVersionStore(context).save(bundled)
    }
}
