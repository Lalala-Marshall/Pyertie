package com.marshall.pyerite.data.sde

import android.content.Context
import com.marshall.pyerite.localization.SdeDatabase
import java.io.File
import java.io.FileOutputStream

object BundledSdeUpdater {

    fun ensureUpToDate(context: Context) {
        val bundled = SdeReleaseMeta.fromAssets(context)
        if (bundled == null) {
            ensureDatabasesPresent(context)
            return
        }

        val installed = SdeVersionStore(context).load()
        if (needsUpgrade(bundled, installed)) {
            applyBundledAssets(context, bundled)
        } else {
            ensureDatabasesPresent(context)
        }
    }

    private fun needsUpgrade(bundled: SdeReleaseMeta, installed: SdeReleaseMeta?): Boolean {
        if (installed == null) return true

        val bundledKey = bundled.versionKey()
        val installedKey = installed.versionKey()
        if (bundledKey > installedKey) return true
        if (bundledKey < installedKey) return false

        if (bundled.sdeSha256 != null && bundled.sdeSha256 != installed.sdeSha256) return true
        if (bundled.iconSha256 != null && bundled.iconSha256 != installed.iconSha256) return true
        return false
    }

    private fun applyBundledAssets(context: Context, bundled: SdeReleaseMeta) {
        deleteDatabase(context, SdeDatabase.ZH_FILE_NAME)
        deleteDatabase(context, SdeDatabase.EN_FILE_NAME)
        copyDatabaseFromAssets(context, SdeDatabase.ZH_FILE_NAME)
        copyDatabaseFromAssets(context, SdeDatabase.EN_FILE_NAME)
        deleteIcons(context)
        SdeVersionStore(context).save(bundled)
    }

    private fun ensureDatabasesPresent(context: Context) {
        copyDatabaseFromAssets(context, SdeDatabase.ZH_FILE_NAME)
        copyDatabaseFromAssets(context, SdeDatabase.EN_FILE_NAME)
    }

    private fun copyDatabaseFromAssets(context: Context, dbName: String) {
        val dbFile = context.getDatabasePath(dbName)
        if (dbFile.exists()) return

        dbFile.parentFile?.mkdirs()
        context.assets.open("db/$dbName").use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun deleteDatabase(context: Context, dbName: String) {
        val dbFile = context.getDatabasePath(dbName)
        listOf(
            dbFile,
            File("${dbFile.absolutePath}-wal"),
            File("${dbFile.absolutePath}-shm"),
        ).forEach { file ->
            if (file.exists()) file.delete()
        }
    }

    private fun deleteIcons(context: Context) {
        val iconDir = File(context.filesDir, ICONS_DIR_NAME)
        if (iconDir.exists()) iconDir.deleteRecursively()
    }

    private const val ICONS_DIR_NAME = "icons"
}
