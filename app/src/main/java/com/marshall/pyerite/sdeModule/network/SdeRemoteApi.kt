package com.marshall.pyerite.sdeModule.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface SdeRemoteApi {

    @GET
    suspend fun fetchLatestPackage(@Url url: String): SdeLatestJsonDto

    @GET
    suspend fun fetchGitHubRelease(@Url url: String): GitHubReleaseDto

    @Streaming
    @GET
    suspend fun downloadBody(@Url url: String): ResponseBody
}
