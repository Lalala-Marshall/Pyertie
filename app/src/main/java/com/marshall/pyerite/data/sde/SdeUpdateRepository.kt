package com.marshall.pyerite.data.sde

import android.content.Context
import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.data.sde.network.SdeRemoteDataSource
import com.marshall.pyerite.localization.SdeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class SdeUpdateRepository(
    private val context: Context,
    private val remoteDataSource: SdeRemoteDataSource,
    private val versionStore: SdeVersionStore,
    private val roomProvider: RoomProvider,
    private val iconManager: IconManager,
) {

    private val _contentRefreshed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val contentRefreshed: SharedFlow<Unit> = _contentRefreshed.asSharedFlow()

    suspend fun checkForRemoteUpdate(): SdeRemotePackage? = withContext(Dispatchers.IO) {
        val remote = remoteDataSource.fetchLatestPackage() ?: return@withContext null
        val installed = versionStore.load()
        if (SdeVersionComparator.needsUpgrade(remote.meta, installed)) remote else null
    }

    suspend fun downloadAndApply(
        remote: SdeRemotePackage,
        onProgress: (Int) -> Unit,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val downloadDir = File(context.cacheDir, "sde_download").apply {
                if (exists()) deleteRecursively()
                mkdirs()
            }

            val dbZhUrl = remote.urlFor(SdeDatabase.ZH_FILE_NAME)
                ?: error("Missing download URL for ${SdeDatabase.ZH_FILE_NAME}")
            val dbEnUrl = remote.urlFor(SdeDatabase.EN_FILE_NAME)
                ?: error("Missing download URL for ${SdeDatabase.EN_FILE_NAME}")
            val iconsUrl = remote.urlFor("icons.zip")
                ?: error("Missing download URL for icons.zip")

            val dbZhFile = File(downloadDir, SdeDatabase.ZH_FILE_NAME)
            val dbEnFile = File(downloadDir, SdeDatabase.EN_FILE_NAME)
            val iconsFile = File(downloadDir, "icons.zip")

            val steps = listOf(
                dbZhUrl to dbZhFile,
                dbEnUrl to dbEnFile,
                iconsUrl to iconsFile,
            )
            val perStep = 100f / steps.size
            steps.forEachIndexed { index, (url, dest) ->
                remoteDataSource.downloadToFile(url, dest) { stepProgress ->
                    val total = ((index * perStep) + (stepProgress * perStep)).roundToInt().coerceIn(0, 100)
                    onProgress(total)
                }
            }

            roomProvider.closeAndInvalidate()
            SdeAssetFiles.deleteAllDatabases(context)
            SdeAssetFiles.installDatabaseFromFile(context, SdeDatabase.ZH_FILE_NAME, dbZhFile)
            SdeAssetFiles.installDatabaseFromFile(context, SdeDatabase.EN_FILE_NAME, dbEnFile)
            SdeAssetFiles.extractIconsFromFile(context, iconsFile)
            iconManager.reload()
            versionStore.save(remote.meta)
            _contentRefreshed.tryEmit(Unit)
            onProgress(100)
            downloadDir.deleteRecursively()
            Unit
        }
    }
}
