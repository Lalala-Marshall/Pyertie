package com.marshall.pyerite.sdeModule.network

import com.marshall.pyerite.sdeModule.update.SdeRemoteConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit

/** GitHub API headers for SDE release metadata / download. */
class SdeGitHubHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        if (request.url.host == SdeRemoteConfig.GITHUB_API_HOST) {
            builder.header("Accept", "application/vnd.github+json")
            builder.header("X-GitHub-Api-Version", "2022-11-28")
        }
        return chain.proceed(builder.build())
    }
}

fun createSdeOkHttpClient(
    connectTimeoutSeconds: Long,
    readTimeout: Long,
    readTimeoutUnit: TimeUnit = TimeUnit.SECONDS,
    base: OkHttpClient,
): OkHttpClient =
    base.newBuilder()
        .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(readTimeout, readTimeoutUnit)
        .addInterceptor(SdeGitHubHeadersInterceptor())
        .build()
