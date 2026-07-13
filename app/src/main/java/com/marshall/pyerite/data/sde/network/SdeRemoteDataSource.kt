package com.marshall.pyerite.data.sde.network

import com.marshall.pyerite.data.sde.SdeRemoteConfig
import com.marshall.pyerite.data.sde.SdeRemotePackage
import com.marshall.pyerite.data.sde.SdeUpdateLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
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
        private const val USER_AGENT = "Pyerite-Android"
        private const val METADATA_CONNECT_TIMEOUT_SECONDS = 8L
        private const val METADATA_READ_TIMEOUT_SECONDS = 12L
        private val METADATA_TOTAL_TIMEOUT = 15_000.milliseconds
        private const val DOWNLOAD_CONNECT_TIMEOUT_SECONDS = 30L
        private const val DOWNLOAD_READ_TIMEOUT_MINUTES = 30L

        fun create(): SdeRemoteDataSource {
            return SdeRemoteDataSource(
                metadataApi = createApi(
                    connectTimeoutSeconds = METADATA_CONNECT_TIMEOUT_SECONDS,
                    readTimeoutSeconds = METADATA_READ_TIMEOUT_SECONDS,
                ),
                downloadApi = createApi(
                    connectTimeoutSeconds = DOWNLOAD_CONNECT_TIMEOUT_SECONDS,
                    readTimeoutMinutes = DOWNLOAD_READ_TIMEOUT_MINUTES,
                ),
            )
        }

        private fun createApi(
            connectTimeoutSeconds: Long,
            readTimeoutSeconds: Long = 0,
            readTimeoutMinutes: Long = 0,
        ): SdeRemoteApi {
            val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
            if (readTimeoutMinutes > 0) {
                clientBuilder.readTimeout(readTimeoutMinutes, TimeUnit.MINUTES)
            } else {
                clientBuilder.readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            }
            val client = clientBuilder
                .addInterceptor { chain ->
                    val requestUrl = chain.request().url
                    val builder = chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                    if (requestUrl.host == "api.github.com") {
                        builder.header("Accept", "application/vnd.github+json")
                        builder.header("X-GitHub-Api-Version", "2022-11-28")
                    } else {
                        builder.header("Accept", "*/*")
                    }
                    chain.proceed(builder.build())
                }
                .build()

            return Retrofit.Builder()
                .baseUrl("https://github.com/")
                .client(client)
                .build()
                .create(SdeRemoteApi::class.java)
        }
    }
}
