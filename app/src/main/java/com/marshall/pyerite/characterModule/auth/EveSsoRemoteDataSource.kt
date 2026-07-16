package com.marshall.pyerite.characterModule.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

class EveSsoRemoteDataSource(
    private val api: EveSsoApi,
) {
    private val cachedEndpoints = AtomicReference<SsoEndpoints?>(null)

    suspend fun exchangeAuthorizationCode(
        code: String,
        codeVerifier: String,
    ): EveSsoTokenResponse = withContext(Dispatchers.IO) {
        val endpoints = resolveEndpoints()
        postToken(
            endpoints.tokenEndpoint,
            mapOf(
                "grant_type" to "authorization_code",
                "code" to code,
                "client_id" to EveSsoConfig.clientId,
                "code_verifier" to codeVerifier,
                "redirect_uri" to EveSsoConfig.redirectUri,
            ),
        )
    }

    suspend fun refreshAccessToken(refreshToken: String): EveSsoTokenResponse =
        withContext(Dispatchers.IO) {
            val endpoints = resolveEndpoints()
            postToken(
                endpoints.tokenEndpoint,
                mapOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to refreshToken,
                    "client_id" to EveSsoConfig.clientId,
                ),
            )
        }

    suspend fun resolveAuthorizationEndpoint(): String = withContext(Dispatchers.IO) {
        resolveEndpoints().authorizationEndpoint
    }

    private suspend fun postToken(
        tokenEndpoint: String,
        fields: Map<String, String>,
    ): EveSsoTokenResponse {
        return try {
            api.postToken(tokenEndpoint, fields).toDomain()
        } catch (error: HttpException) {
            val raw = error.response()?.errorBody()?.string().orEmpty()
            throw IOException("SSO token HTTP ${error.code()}: $raw", error)
        }
    }

    private suspend fun resolveEndpoints(): SsoEndpoints {
        cachedEndpoints.get()?.let { return it }
        return try {
            val metadata = api.fetchMetadata(EveSsoConfig.METADATA_URL)
            val endpoints = SsoEndpoints(
                authorizationEndpoint = metadata.authorizationEndpoint,
                tokenEndpoint = metadata.tokenEndpoint,
            )
            cachedEndpoints.set(endpoints)
            endpoints
        } catch (_: Exception) {
            SsoEndpoints(
                authorizationEndpoint = EveSsoConfig.FALLBACK_AUTHORIZATION_ENDPOINT,
                tokenEndpoint = EveSsoConfig.FALLBACK_TOKEN_ENDPOINT,
            )
        }
    }

    data class SsoEndpoints(
        val authorizationEndpoint: String,
        val tokenEndpoint: String,
    )
}
