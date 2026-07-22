package com.marshall.pyerite.charactersListModule.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicReference
import androidx.core.net.toUri

internal class EveSsoRemoteDataSource(
    private val api: EveSsoApi,
) {
    private val cachedEndpoints = AtomicReference<SsoEndpoints?>(null)
    private val cachedJwks = AtomicReference<Pair<String, EveJWTsDto>?>(null)

    suspend fun exchangeAuthorizationCode(
        code: String,
        codeVerifier: String,
    ): EveSsoTokenResponse = withContext(Dispatchers.IO) {
        val endpoints = resolveEndpoints()
        postToken(
            endpoints.tokenEndpoint,
            mapOf(
                EveSsoConfig.OAuth.PARAM_GRANT_TYPE to EveSsoConfig.OAuth.GRANT_AUTHORIZATION_CODE,
                EveSsoConfig.OAuth.PARAM_CODE to code,
                EveSsoConfig.OAuth.PARAM_CLIENT_ID to EveSsoConfig.clientId,
                EveSsoConfig.OAuth.PARAM_CODE_VERIFIER to codeVerifier,
                EveSsoConfig.OAuth.PARAM_REDIRECT_URI to EveSsoConfig.redirectUri,
            ),
        )
    }

    suspend fun refreshAccessToken(refreshToken: String): EveSsoTokenResponse =
        withContext(Dispatchers.IO) {
            val endpoints = resolveEndpoints()
            postToken(
                endpoints.tokenEndpoint,
                mapOf(
                    EveSsoConfig.OAuth.PARAM_GRANT_TYPE to EveSsoConfig.OAuth.GRANT_REFRESH_TOKEN,
                    EveSsoConfig.OAuth.PARAM_REFRESH_TOKEN to refreshToken,
                    EveSsoConfig.OAuth.PARAM_CLIENT_ID to EveSsoConfig.clientId,
                ),
            )
        }

    suspend fun revokeRefreshToken(refreshToken: String) = withContext(Dispatchers.IO) {
        val endpoints = resolveEndpoints()
        try {
            api.revokeToken(
                endpoints.revocationEndpoint,
                mapOf(
                    EveSsoConfig.OAuth.PARAM_CLIENT_ID to EveSsoConfig.clientId,
                    EveSsoConfig.OAuth.PARAM_TOKEN to refreshToken,
                    EveSsoConfig.OAuth.PARAM_TOKEN_TYPE_HINT to
                        EveSsoConfig.OAuth.TOKEN_TYPE_HINT_REFRESH,
                ),
            )
        } catch (error: HttpException) {
            // RFC 7009: invalid tokens still return success semantics; ignore client errors.
            if (error.code() !in 200..299 && error.code() !in 400..499) {
                throw EveSsoHttpException(
                    statusCode = error.code(),
                    message = "SSO revoke HTTP ${error.code()}",
                    cause = error,
                )
            }
        }
    }

    suspend fun fetchJwks(): EveJWTsDto = withContext(Dispatchers.IO) {
        val endpoints = resolveEndpoints()
        cachedJwks.get()?.takeIf { it.first == endpoints.jwksUri }?.second?.let { return@withContext it }
        val jwks = api.fetchJWTs(endpoints.jwksUri)
        cachedJwks.set(endpoints.jwksUri to jwks)
        jwks
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
            throw EveSsoHttpException(
                statusCode = error.code(),
                message = "SSO token HTTP ${error.code()}: $raw",
                cause = error,
                errorBody = raw,
            )
        }
    }

    private suspend fun resolveEndpoints(): SsoEndpoints {
        cachedEndpoints.get()?.let { return it }
        return try {
            val metadata = api.fetchMetadata(EveSsoConfig.METADATA_URL)
            val endpoints = SsoEndpoints(
                authorizationEndpoint = requireAllowedSsoHttpsUrl(metadata.authorizationEndpoint),
                tokenEndpoint = requireAllowedSsoHttpsUrl(metadata.tokenEndpoint),
                jwksUri = requireAllowedSsoHttpsUrl(
                    metadata.jwksUri ?: EveSsoConfig.FALLBACK_JWKS_URI,
                ),
                revocationEndpoint = requireAllowedSsoHttpsUrl(
                    metadata.revocationEndpoint ?: EveSsoConfig.FALLBACK_REVOCATION_ENDPOINT,
                ),
            )
            cachedEndpoints.set(endpoints)
            endpoints
        } catch (_: Exception) {
            SsoEndpoints(
                authorizationEndpoint = EveSsoConfig.FALLBACK_AUTHORIZATION_ENDPOINT,
                tokenEndpoint = EveSsoConfig.FALLBACK_TOKEN_ENDPOINT,
                jwksUri = EveSsoConfig.FALLBACK_JWKS_URI,
                revocationEndpoint = EveSsoConfig.FALLBACK_REVOCATION_ENDPOINT,
            )
        }
    }

    private fun requireAllowedSsoHttpsUrl(raw: String): String {
        val uri = raw.toUri()
        require(uri.scheme.equals("https", ignoreCase = true)) {
            "SSO endpoint must be https: $raw"
        }
        require(uri.host.equals(EveSsoConfig.SSO_HOST, ignoreCase = true)) {
            "SSO endpoint host not allowlisted: $raw"
        }
        return raw
    }

    data class SsoEndpoints(
        val authorizationEndpoint: String,
        val tokenEndpoint: String,
        val jwksUri: String,
        val revocationEndpoint: String,
    )
}
