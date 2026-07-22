package com.marshall.pyerite.charactersListModule.auth

import com.marshall.pyerite.BuildConfig
import com.marshall.pyerite.data.network.EsiNetworkConfig

/**
 * Public EVE Online endpoint configuration (not secrets).
 * Client ID / redirect URI come from [BuildConfig] via gitignored local.properties.
 */
object EveSsoConfig {
    const val SSO_HOST = "login.eveonline.com"
    const val SSO_BASE_URL = "https://$SSO_HOST"
    const val ESI_BASE_URL = EsiNetworkConfig.BASE_URL
    const val IMAGE_BASE_URL = "https://images.evetech.net/"

    const val METADATA_URL =
        "$SSO_BASE_URL/.well-known/oauth-authorization-server"
    const val FALLBACK_AUTHORIZATION_ENDPOINT =
        "$SSO_BASE_URL/v2/oauth/authorize"
    const val FALLBACK_TOKEN_ENDPOINT =
        "$SSO_BASE_URL/v2/oauth/token"
    const val FALLBACK_JWKS_URI = "$SSO_BASE_URL/oauth/jwks"
    const val FALLBACK_REVOCATION_ENDPOINT = "$SSO_BASE_URL/v2/oauth/revoke"

    /** JWT `iss` values CCP has been observed to emit. */
    val acceptedIssuers: Set<String> = setOf(
        SSO_HOST,
        SSO_BASE_URL,
        "$SSO_BASE_URL/",
    )

    const val JWT_AUDIENCE_EVE_ONLINE = "EVE Online"
    const val JWT_ALGORITHM_RS256 = "RS256"

    const val MILLIS_PER_SECOND = 1_000L
    const val ACCESS_TOKEN_REFRESH_BUFFER_MS = 5 * 60 * MILLIS_PER_SECOND

    /** Pending PKCE login survives process death for this long. */
    const val PENDING_LOGIN_TTL_MS = 15 * 60 * MILLIS_PER_SECOND

    const val TOKEN_TYPE_BEARER = "Bearer"
    const val DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS = 1_200L

    /** HTTP statuses that mean the refresh token is permanently unusable. */
    val permanentRefreshHttpStatuses: Set<Int> = setOf(400, 401, 403)

    /** OAuth 2.0 / PKCE wire values (CCP SSO). */
    object OAuth {
        const val RESPONSE_TYPE_CODE = "code"
        const val GRANT_AUTHORIZATION_CODE = "authorization_code"
        const val GRANT_REFRESH_TOKEN = "refresh_token"
        const val CODE_CHALLENGE_METHOD_S256 = "S256"
        const val TOKEN_TYPE_HINT_REFRESH = "refresh_token"

        const val PARAM_RESPONSE_TYPE = "response_type"
        const val PARAM_REDIRECT_URI = "redirect_uri"
        const val PARAM_CLIENT_ID = "client_id"
        const val PARAM_SCOPE = "scope"
        const val PARAM_STATE = "state"
        const val PARAM_CODE = "code"
        const val PARAM_CODE_CHALLENGE = "code_challenge"
        const val PARAM_CODE_CHALLENGE_METHOD = "code_challenge_method"
        const val PARAM_CODE_VERIFIER = "code_verifier"
        const val PARAM_GRANT_TYPE = "grant_type"
        const val PARAM_REFRESH_TOKEN = "refresh_token"
        const val PARAM_TOKEN = "token"
        const val PARAM_TOKEN_TYPE_HINT = "token_type_hint"

        const val QUERY_ERROR = "error"
        const val QUERY_ERROR_DESCRIPTION = "error_description"
        const val QUERY_CODE = "code"
        const val QUERY_STATE = "state"

        const val ERROR_INVALID_GRANT = "invalid_grant"
        const val ERROR_INVALID_TOKEN = "invalid_token"
        const val ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client"
    }

    object Image {
        const val PORTRAIT_SIZE = 128
        const val LOGO_SIZE = 64
    }

    val clientId: String get() = BuildConfig.EVE_SSO_CLIENT_ID
    val redirectUri: String get() = BuildConfig.EVE_SSO_REDIRECT_URI

    /**
     * Scopes requested at login (typed). Must match Enabled Scopes on the
     * EVE developers portal. Wire strings: [requestedScopeApiValues].
     */
    val requestedScopes: List<EveSsoScope> = EveSsoScope.entries

    /** Space-joined OAuth `scope` query values. */
    val requestedScopeApiValues: List<String> =
        requestedScopes.map { it.apiValue }
}
