package com.marshall.pyerite.esiModule.http

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/** Pins ESI OpenAPI compatibility date on requests to the ESI host. */
class EsiCompatibilityInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        if (request.url.host == EsiConfig.HOST) {
            builder.header("X-Compatibility-Date", EsiConfig.COMPATIBILITY_DATE)
        }
        return chain.proceed(builder.build())
    }
}

fun createEsiOkHttpClient(base: OkHttpClient): OkHttpClient =
    base.newBuilder()
        .addInterceptor(EsiCompatibilityInterceptor())
        .build()

/** Shared ESI OkHttp so resource-domain Retrofit APIs reuse one client. */
internal class EsiHttp(val client: OkHttpClient)
