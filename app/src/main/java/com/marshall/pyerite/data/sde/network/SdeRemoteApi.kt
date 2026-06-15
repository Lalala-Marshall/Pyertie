package com.marshall.pyerite.data.sde.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface SdeRemoteApi {

    @GET
    suspend fun fetchBody(@Url url: String): ResponseBody

    @Streaming
    @GET
    suspend fun downloadBody(@Url url: String): ResponseBody
}
