package com.marshall.pyerite.infra.network

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Shared HTTP primitives — no ESI / SDE / GitHub domain headers. */
object NetworkDefaults {
    const val USER_AGENT =
        "Pyerite-Android/1.0 (EVE third-party; contact via app store listing)"
    const val CONNECT_TIMEOUT_SECONDS = 20L
    const val READ_TIMEOUT_SECONDS = 30L
    const val PLACEHOLDER_BASE_URL = "https://localhost/"
}

/** Shared JSON config for Retrofit responses and local persistence. */
val PyeriteJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    coerceInputValues = true
}

/** App-wide User-Agent only. Domain clients add their own interceptors. */
class AppUserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", NetworkDefaults.USER_AGENT)
            .build()
        return chain.proceed(request)
    }
}

fun createOkHttpClient(
    connectTimeoutSeconds: Long = NetworkDefaults.CONNECT_TIMEOUT_SECONDS,
    readTimeout: Long = NetworkDefaults.READ_TIMEOUT_SECONDS,
    readTimeoutUnit: TimeUnit = TimeUnit.SECONDS,
    base: OkHttpClient? = null,
): OkHttpClient {
    val builder = (base?.newBuilder() ?: OkHttpClient.Builder())
        .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(readTimeout, readTimeoutUnit)
    if (base == null) {
        builder.addInterceptor(AppUserAgentInterceptor())
    }
    return builder.build()
}

fun OkHttpClient.createRetrofit(baseUrl: String): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(this)
        .addConverterFactory(PyeriteJson.asConverterFactory(JSON_MEDIA_TYPE))
        .build()

inline fun <reified T> OkHttpClient.createApi(baseUrl: String): T =
    createRetrofit(baseUrl).create(T::class.java)

private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
