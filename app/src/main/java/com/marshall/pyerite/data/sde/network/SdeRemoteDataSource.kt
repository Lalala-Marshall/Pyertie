package com.marshall.pyerite.data.sde.network

import com.marshall.pyerite.data.sde.SdeRemoteConfig
import com.marshall.pyerite.data.sde.SdeRemotePackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class SdeRemoteDataSource(
    private val api: SdeRemoteApi,
) {

    suspend fun fetchLatestPackage(): SdeRemotePackage? = withContext(Dispatchers.IO) {
        try {
            api.fetchBody(SdeRemoteConfig.LATEST_JSON_URL).use { body ->
                SdeRemotePackage.fromJsonObject(JSONObject(body.string()))
            }
        } catch (_: HttpException) {
            null
        } catch (_: IOException) {
            null
        }
    }

    suspend fun downloadToFile(
        url: String,
        dest: File,
        onProgress: (Float) -> Unit,
    ) = withContext(Dispatchers.IO) {
        try {
            api.downloadBody(url).use { responseBody ->
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

    companion object {
        private const val USER_AGENT = "Pyerite-Android"

        fun createApi(): SdeRemoteApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.MINUTES)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "*/*")
                        .build()
                    chain.proceed(request)
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
