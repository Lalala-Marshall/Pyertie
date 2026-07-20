package com.marshall.pyerite.characterModule.auth

import android.net.Uri
import androidx.annotation.StringRes
import com.marshall.pyerite.R
import com.marshall.pyerite.characterModule.viewModel.CharacterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

sealed interface EveSsoUiStatus {
    data object Idle : EveSsoUiStatus
    data object AwaitingBrowser : EveSsoUiStatus
    data object ExchangingToken : EveSsoUiStatus
    data class Failed(
        @param:StringRes val messageRes: Int,
        val formatArgs: List<Any> = emptyList(),
    ) : EveSsoUiStatus
    data class Succeeded(val characterName: String) : EveSsoUiStatus
}

class EveSsoAuthRepository internal constructor(
    private val remote: EveSsoRemoteDataSource,
    private val tokenManager: EveTokenManager,
    private val profileLoader: CharacterProfileLoader,
    private val characterRepository: CharacterRepository,
    private val callbackBus: EveSsoCallbackBus,
    private val pendingLoginStore: EvePendingLoginStore,
    private val profileCache: CharacterProfileCache,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    /** Cold start already refreshed tokens in [restoreSessions]; skip one ProcessLifecycle onStart. */
    private val skipNextForegroundRefresh = AtomicBoolean(false)

    private val _status = MutableStateFlow<EveSsoUiStatus>(EveSsoUiStatus.Idle)
    val status: StateFlow<EveSsoUiStatus> = _status.asStateFlow()

    init {
        scope.launch {
            restoreSessions()
            if (pendingLoginStore.hasPending()) {
                _status.value = EveSsoUiStatus.AwaitingBrowser
            }
            callbackBus.callbacks.collect { uri ->
                handleCallback(uri)
            }
        }
    }

    fun clearStatus() {
        _status.value = EveSsoUiStatus.Idle
    }

    fun cancelLogin() {
        pendingLoginStore.clear()
        _status.value = EveSsoUiStatus.Idle
    }

    /** Returns the authorization URL for the UI to open, or null if preparation failed. */
    suspend fun prepareLogin(): String? = mutex.withLock {
        if (EveSsoConfig.clientId.isBlank()) {
            _status.value = EveSsoUiStatus.Failed(R.string.character_sso_error_missing_client_id)
            return@withLock null
        }
        val verifier = EveSsoPkce.generateVerifier()
        val challenge = EveSsoPkce.challengeS256(verifier)
        val state = EveSsoPkce.generateState()
        pendingLoginStore.save(state = state, codeVerifier = verifier)

        val authorizationEndpoint = remote.resolveAuthorizationEndpoint()
        val url = buildAuthorizationUrl(
            authorizationEndpoint = authorizationEndpoint,
            state = state,
            codeChallenge = challenge,
        )
        _status.value = EveSsoUiStatus.AwaitingBrowser
        url
    }

    suspend fun refreshStoredSessionsOnForeground() {
        if (skipNextForegroundRefresh.compareAndSet(true, false)) return
        withContext(Dispatchers.IO) {
            applyRefreshResults(tokenManager.refreshAllIfNeeded())
        }
    }

    fun removeCharacterSession(characterId: Long) {
        scope.launch(Dispatchers.IO) {
            val refreshToken = tokenManager.peekRefreshToken(characterId)
            tokenManager.remove(characterId)
            profileCache.remove(characterId)
            characterRepository.removeLoggedInCharacter(characterId)
            if (!refreshToken.isNullOrBlank()) {
                runCatching { remote.revokeRefreshToken(refreshToken) }
            }
        }
    }

    private suspend fun handleCallback(uri: Uri) {
        mutex.withLock {
            val error = uri.queryParamOrNull(EveSsoConfig.OAuth.QUERY_ERROR)
            if (error != null) {
                pendingLoginStore.clear()
                _status.value = EveSsoUiStatus.Failed(R.string.character_sso_error_provider_denied)
                return
            }

            val code = uri.queryParamOrNull(EveSsoConfig.OAuth.QUERY_CODE)
            val state = uri.queryParamOrNull(EveSsoConfig.OAuth.QUERY_STATE)
            if (code.isNullOrBlank() || state.isNullOrBlank()) {
                pendingLoginStore.clear()
                _status.value = EveSsoUiStatus.Failed(R.string.character_sso_error_invalid_callback)
                return
            }

            val verifier = pendingLoginStore.consume(state)
            if (verifier.isNullOrBlank()) {
                _status.value = EveSsoUiStatus.Failed(R.string.character_sso_error_state_mismatch)
                return
            }

            _status.value = EveSsoUiStatus.ExchangingToken
            try {
                val tokenResponse = remote.exchangeAuthorizationCode(code, verifier)
                val claims = EveJwtDecoder.decodeAndVerify(tokenResponse.accessToken, remote.fetchJwks())
                EveJwtDecoder.assertBasicClaims(claims, EveSsoConfig.clientId)

                val fromExpiresIn = System.currentTimeMillis() +
                    tokenResponse.expiresInSeconds * EveSsoConfig.MILLIS_PER_SECOND
                val session = tokenManager.save(
                    EveTokenSet(
                        characterId = claims.characterId,
                        characterName = claims.characterName,
                        accessToken = tokenResponse.accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        tokenType = normalizeTokenType(tokenResponse.tokenType),
                        expiresAtEpochMs = minOf(fromExpiresIn, claims.expiresAtEpochMs),
                        scopes = tokenResponse.scopes.ifEmpty { claims.scopes },
                    ),
                )

                val loggedIn = profileLoader.load(session)
                profileCache.save(loggedIn)
                characterRepository.upsertLoggedInCharacter(loggedIn)
                if (characterRepository.currentCharacter.value == null) {
                    characterRepository.selectCurrentCharacter(loggedIn)
                }
                _status.value = EveSsoUiStatus.Succeeded(loggedIn.name)
            } catch (_: Exception) {
                _status.value = EveSsoUiStatus.Failed(R.string.character_sso_error_generic)
            }
        }
    }

    /**
     * Startup restore priority: must (local session) → cache → deferred network enrich.
     * UI is hydrated before any ESI profile calls so a slow network never looks like logout.
     */
    private suspend fun restoreSessions() {
        withContext(Dispatchers.IO) {
            val sessions = tokenManager.storedSessions()
            hydrateLocalCharacters(sessions)
            characterRepository.restoreCurrentCharacterSelection()

            skipNextForegroundRefresh.set(true)
            applyRefreshResults(tokenManager.refreshAllIfNeeded())

            val remaining = tokenManager.storedSessions()
            enrichProfilesDeferred(remaining)
        }
    }

    private fun hydrateLocalCharacters(sessions: List<EveStoredSession>) {
        sessions.forEach { session ->
            val scopes = tokenManager.grantedScopes(session.characterId)
            val cached = profileCache.get(session.characterId)
            val display = when {
                cached != null -> cached.copy(
                    name = session.characterName.ifBlank { cached.name },
                    portraitUrl = portraitUrl(session.characterId),
                    grantedScopes = scopes.ifEmpty { cached.grantedScopes },
                )
                else -> localLoggedInCharacter(session, scopes)
            }
            characterRepository.upsertLoggedInCharacter(display)
        }
    }

    private suspend fun enrichProfilesDeferred(sessions: List<EveStoredSession>) {
        if (sessions.isEmpty()) return
        coroutineScope {
            sessions.map { session ->
                async {
                    runCatching {
                        val loaded = profileLoader.load(session)
                        profileCache.save(loaded)
                        characterRepository.upsertLoggedInCharacter(loaded)
                    }
                }
            }.awaitAll()
        }
    }

    private fun applyRefreshResults(results: List<EveTokenRefreshResult>) {
        results.forEach { result ->
            if (result is EveTokenRefreshResult.Failed && result.permanent) {
                profileCache.remove(result.characterId)
                characterRepository.removeLoggedInCharacter(result.characterId)
            }
        }
    }

    private fun buildAuthorizationUrl(
        authorizationEndpoint: String,
        state: String,
        codeChallenge: String,
    ): String {
        val scope = EveSsoConfig.requestedScopeApiValues.joinToString(" ")
        val query = listOf(
            EveSsoConfig.OAuth.PARAM_RESPONSE_TYPE to EveSsoConfig.OAuth.RESPONSE_TYPE_CODE,
            EveSsoConfig.OAuth.PARAM_REDIRECT_URI to EveSsoConfig.redirectUri,
            EveSsoConfig.OAuth.PARAM_CLIENT_ID to EveSsoConfig.clientId,
            EveSsoConfig.OAuth.PARAM_SCOPE to scope,
            EveSsoConfig.OAuth.PARAM_STATE to state,
            EveSsoConfig.OAuth.PARAM_CODE_CHALLENGE to codeChallenge,
            EveSsoConfig.OAuth.PARAM_CODE_CHALLENGE_METHOD to
                EveSsoConfig.OAuth.CODE_CHALLENGE_METHOD_S256,
        ).joinToString("&") { (key, value) ->
            "${enc(key)}=${enc(value)}"
        }
        return "$authorizationEndpoint?$query"
    }

    private fun enc(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
