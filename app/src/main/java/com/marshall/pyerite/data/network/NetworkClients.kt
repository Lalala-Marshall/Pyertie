package com.marshall.pyerite.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkDefaults {
    const val USER_AGENT =
        "Pyerite-Android/1.0 (EVE third-party; contact via app store listing)"
    const val CONNECT_TIMEOUT_SECONDS = 20L
    const val READ_TIMEOUT_SECONDS = 30L
    const val PLACEHOLDER_BASE_URL = "https://localhost/"
}

/** Shared headers for all Retrofit traffic. Derived clients inherit this via [OkHttpClient.newBuilder]. */
class PyeriteHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
            .header("User-Agent", NetworkDefaults.USER_AGENT)
        if (request.url.host == "api.github.com") {
            builder.header("Accept", "application/vnd.github+json")
            builder.header("X-GitHub-Api-Version", "2022-11-28")
        }
        return chain.proceed(builder.build())
    }
}

fun createPyeriteOkHttpClient(
    connectTimeoutSeconds: Long = NetworkDefaults.CONNECT_TIMEOUT_SECONDS,
    readTimeout: Long = NetworkDefaults.READ_TIMEOUT_SECONDS,
    readTimeoutUnit: TimeUnit = TimeUnit.SECONDS,
    base: OkHttpClient? = null,
): OkHttpClient {
    val builder = (base?.newBuilder() ?: OkHttpClient.Builder())
        .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(readTimeout, readTimeoutUnit)
    if (base == null) {
        builder.addInterceptor(PyeriteHeadersInterceptor())
    }
    return builder.build()
}

fun OkHttpClient.createRetrofit(baseUrl: String): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(this)
        .build()

inline fun <reified T> OkHttpClient.createApi(baseUrl: String): T =
    createRetrofit(baseUrl).create(T::class.java)
