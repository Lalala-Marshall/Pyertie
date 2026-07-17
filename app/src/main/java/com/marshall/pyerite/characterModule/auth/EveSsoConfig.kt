package com.marshall.pyerite.characterModule.auth

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
     * Scopes requested at login. Must match (or be a subset of) Enabled Scopes
     * on the EVE developers portal application.
     */
    val requestedScopes: List<String> = listOf(
        "publicData",
        "esi-calendar.respond_calendar_events.v1",
        "esi-calendar.read_calendar_events.v1",
        "esi-location.read_location.v1",
        "esi-location.read_ship_type.v1",
        "esi-mail.organize_mail.v1",
        "esi-mail.read_mail.v1",
        "esi-mail.send_mail.v1",
        "esi-skills.read_skills.v1",
        "esi-skills.read_skillqueue.v1",
        "esi-wallet.read_character_wallet.v1",
        "esi-wallet.read_corporation_wallet.v1",
        "esi-search.search_structures.v1",
        "esi-clones.read_clones.v1",
        "esi-characters.read_contacts.v1",
        "esi-universe.read_structures.v1",
        "esi-killmails.read_killmails.v1",
        "esi-corporations.read_corporation_membership.v1",
        "esi-assets.read_assets.v1",
        "esi-planets.manage_planets.v1",
        "esi-fleets.read_fleet.v1",
        "esi-fleets.write_fleet.v1",
        "esi-ui.open_window.v1",
        "esi-ui.write_waypoint.v1",
        "esi-characters.write_contacts.v1",
        "esi-fittings.read_fittings.v1",
        "esi-fittings.write_fittings.v1",
        "esi-markets.structure_markets.v1",
        "esi-corporations.read_structures.v1",
        "esi-characters.read_loyalty.v1",
        "esi-characters.read_chat_channels.v1",
        "esi-characters.read_medals.v1",
        "esi-characters.read_standings.v1",
        "esi-characters.read_agents_research.v1",
        "esi-industry.read_character_jobs.v1",
        "esi-markets.read_character_orders.v1",
        "esi-characters.read_blueprints.v1",
        "esi-characters.read_corporation_roles.v1",
        "esi-location.read_online.v1",
        "esi-contracts.read_character_contracts.v1",
        "esi-clones.read_implants.v1",
        "esi-characters.read_fatigue.v1",
        "esi-killmails.read_corporation_killmails.v1",
        "esi-corporations.track_members.v1",
        "esi-wallet.read_corporation_wallets.v1",
        "esi-characters.read_notifications.v1",
        "esi-corporations.read_divisions.v1",
        "esi-corporations.read_contacts.v1",
        "esi-assets.read_corporation_assets.v1",
        "esi-corporations.read_titles.v1",
        "esi-corporations.read_blueprints.v1",
        "esi-contracts.read_corporation_contracts.v1",
        "esi-corporations.read_standings.v1",
        "esi-corporations.read_starbases.v1",
        "esi-industry.read_corporation_jobs.v1",
        "esi-markets.read_corporation_orders.v1",
        "esi-corporations.read_container_logs.v1",
        "esi-industry.read_character_mining.v1",
        "esi-industry.read_corporation_mining.v1",
        "esi-planets.read_customs_offices.v1",
        "esi-corporations.read_facilities.v1",
        "esi-corporations.read_medals.v1",
        "esi-characters.read_titles.v1",
        "esi-alliances.read_contacts.v1",
        "esi-characters.read_fw_stats.v1",
        "esi-corporations.read_fw_stats.v1",
        "esi-corporations.read_projects.v1",
        "esi-corporations.read_freelance_jobs.v1",
        "esi-characters.read_freelance_jobs.v1",
    )
}
