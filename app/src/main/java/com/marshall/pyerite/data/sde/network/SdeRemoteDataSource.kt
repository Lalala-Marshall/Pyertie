package com.marshall.pyerite.data.sde.network

import com.marshall.pyerite.data.sde.SdeRemoteConfig
import com.marshall.pyerite.data.sde.SdeRemotePackage
import com.marshall.pyerite.data.sde.SdeUpdateLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

class SdeRemoteDataSource(
    private val metadataApi: SdeRemoteApi,
    private val downloadApi: SdeRemoteApi,
) {

    suspend fun fetchLatestPackage(): SdeRemotePackage = withContext(Dispatchers.IO) {
        SdeUpdateLog.d("Remote", "fetchLatestPackage started")
        coroutineScope {
            val winner = CompletableDeferred<SdeRemotePackage>()
            launch {
                fetchFromLatestDownload()?.let {
                    SdeUpdateLog.d("Remote", "metadata from latest.json build=${it.meta.buildNumber}")
                    winner.complete(it)
                }
            }
            launch {
                fetchFromGitHubApi()?.let {
                    SdeUpdateLog.d("Remote", "metadata from GitHub API build=${it.meta.buildNumber}")
                    winner.complete(it)
                }
            }
            try {
                withTimeout(METADATA_TOTAL_TIMEOUT) {
                    winner.await()
                }
            } catch (error: CancellationException) {
                SdeUpdateLog.w("Remote", "fetchLatestPackage cancelled: ${error.javaClass.simpleName}")
                throw error
            } catch (error: Exception) {
                SdeUpdateLog.w("Remote", "fetchLatestPackage failed", error)
                throw IOException("Unable to fetch remote SDE metadata")
            }
        }
    }

    suspend fun downloadToFile(
        url: String,
        dest: File,
        onProgress: (Float) -> Unit,
    ) = withContext(Dispatchers.IO) {
        try {
            downloadApi.downloadBody(url).use { responseBody ->
                val total = responseBody.contentLength().takeIf { it > 0L }
                dest.parentFile?.mkdirs()
                responseBody.byteStream().use { input ->
                    FileOutputStream(dest).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloaded = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (total != null) {
                                onProgress((downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f))
                            }
                        }
                    }
                }
                if (total == null) onProgress(1f)
            }
        } catch (e: HttpException) {
            error("HTTP ${e.code()} for $url")
        }
    }

    private suspend fun fetchFromLatestDownload(): SdeRemotePackage? {
        return try {
            metadataApi.fetchBody(SdeRemoteConfig.LATEST_JSON_URL).use { body ->
                parseLatestPackage(body.string())
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchFromGitHubApi(): SdeRemotePackage? {
        return try {
            val releaseJson = metadataApi.fetchBody(SdeRemoteConfig.GITHUB_API_LATEST_RELEASE_URL).use { body ->
                JSONObject(body.string())
            }
            val assets = releaseJson.optJSONArray("assets") ?: return null
            for (index in 0 until assets.length()) {
                val asset = assets.optJSONObject(index) ?: continue
                if (asset.optString("name") != SdeRemoteConfig.LATEST_JSON_ASSET) continue
                val downloadUrl = asset.optString("browser_download_url").trim()
                if (downloadUrl.isEmpty()) continue
                return metadataApi.fetchBody(downloadUrl).use { body ->
                    parseLatestPackage(body.string())
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseLatestPackage(body: String): SdeRemotePackage {
        val pkg = SdeRemotePackage.fromJsonObject(JSONObject(body))
        val buildNumber = pkg.meta.buildNumber.toIntOrNull()
        require(buildNumber != null && buildNumber > 0) {
            "Invalid remote SDE build_number"
        }
        return pkg
    }

    companion object {
        private val METADATA_TOTAL_TIMEOUT = 15_000.milliseconds
        const val METADATA_CONNECT_TIMEOUT_SECONDS = 8L
        const val METADATA_READ_TIMEOUT_SECONDS = 12L
        const val DOWNLOAD_CONNECT_TIMEOUT_SECONDS = 30L
        const val DOWNLOAD_READ_TIMEOUT_MINUTES = 30L
    }
}
