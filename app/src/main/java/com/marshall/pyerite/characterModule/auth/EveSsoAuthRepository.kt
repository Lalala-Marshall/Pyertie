package com.marshall.pyerite.characterModule.auth

import android.net.Uri
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import com.marshall.pyerite.characterModule.viewModel.CharacterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed interface EveSsoUiStatus {
    data object Idle : EveSsoUiStatus
    data object AwaitingBrowser : EveSsoUiStatus
    data object ExchangingToken : EveSsoUiStatus
    data class Failed(val message: String) : EveSsoUiStatus
    data class Succeeded(val characterName: String) : EveSsoUiStatus
}

class EveSsoAuthRepository(
    private val remote: EveSsoRemoteDataSource,
    private val tokenStore: EveTokenStore,
    private val esi: EsiPublicDataSource,
    private val characterRepository: CharacterRepository,
    private val callbackBus: EveSsoCallbackBus,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private var pendingState: String? = null
    private var pendingCodeVerifier: String? = null

    private val _status = MutableStateFlow<EveSsoUiStatus>(EveSsoUiStatus.Idle)
    val status: StateFlow<EveSsoUiStatus> = _status.asStateFlow()

    init {
        scope.launch {
            restoreSessions()
            callbackBus.callbacks.collect { uri ->
                handleCallback(uri)
            }
        }
    }

    fun clearStatus() {
        _status.value = EveSsoUiStatus.Idle
    }

    /** Returns the authorization URL for the UI to open, or null if preparation failed. */
    suspend fun prepareLogin(): String? = mutex.withLock {
        if (EveSsoConfig.clientId.isBlank()) {
            _status.value = EveSsoUiStatus.Failed("Missing EVE_SSO_CLIENT_ID in local.properties")
            return@withLock null
        }
        val verifier = EveSsoPkce.generateVerifier()
        val challenge = EveSsoPkce.challengeS256(verifier)
        val state = EveSsoPkce.generateState()
        pendingCodeVerifier = verifier
        pendingState = state

        val authorizationEndpoint = remote.resolveAuthorizationEndpoint()
        val url = buildAuthorizationUrl(
            authorizationEndpoint = authorizationEndpoint,
            state = state,
            codeChallenge = challenge,
        )
        _status.value = EveSsoUiStatus.AwaitingBrowser
        url
    }

    fun removeCharacterSession(characterId: Long) {
        tokenStore.remove(characterId)
        characterRepository.removeLoggedInCharacter(characterId)
    }

    private suspend fun handleCallback(uri: Uri) {
        mutex.withLock {
            val expectedState = pendingState
            val verifier = pendingCodeVerifier
            pendingState = null
            pendingCodeVerifier = null

            val error = uri.queryParamOrNull("error")
            if (error != null) {
                _status.value = EveSsoUiStatus.Failed(
                    uri.queryParamOrNull("error_description") ?: error,
                )
                return
            }

            val code = uri.queryParamOrNull("code")
            val state = uri.queryParamOrNull("state")
            if (code.isNullOrBlank() || state.isNullOrBlank() || verifier.isNullOrBlank()) {
                _status.value = EveSsoUiStatus.Failed("Invalid SSO callback")
                return
            }
            if (expectedState == null || expectedState != state) {
                _status.value = EveSsoUiStatus.Failed("SSO state mismatch")
                return
            }

            _status.value = EveSsoUiStatus.ExchangingToken
            try {
                val tokenResponse = remote.exchangeAuthorizationCode(code, verifier)
                val claims = EveJwtDecoder.decodeUnverified(tokenResponse.accessToken)
                EveJwtDecoder.assertBasicClaims(claims, EveSsoConfig.clientId)

                val tokenSet = EveTokenSet(
                    characterId = claims.characterId,
                    characterName = claims.characterName,
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    tokenType = tokenResponse.tokenType,
                    expiresAtEpochMs = System.currentTimeMillis() +
                        tokenResponse.expiresInSeconds * 1_000L,
                    scopes = tokenResponse.scopes.ifEmpty { claims.scopes },
                )
                tokenStore.save(tokenSet)

                val loggedIn = buildLoggedInCharacter(tokenSet)
                characterRepository.upsertLoggedInCharacter(loggedIn)
                if (characterRepository.currentCharacter.value == null) {
                    characterRepository.selectCurrentCharacter(loggedIn)
                }
                _status.value = EveSsoUiStatus.Succeeded(loggedIn.name)
            } catch (error: Exception) {
                _status.value = EveSsoUiStatus.Failed(
                    error.message?.takeIf { it.isNotBlank() } ?: "SSO login failed",
                )
            }
        }
    }

    private suspend fun restoreSessions() {
        val sessions = tokenStore.all()
        if (sessions.isEmpty()) return
        sessions.forEach { tokenSet ->
            runCatching {
                val loggedIn = buildLoggedInCharacter(tokenSet)
                characterRepository.upsertLoggedInCharacter(loggedIn)
            }
        }
    }

    private suspend fun buildLoggedInCharacter(tokenSet: EveTokenSet): LoggedInCharacter {
        val public = runCatching { esi.fetchCharacter(tokenSet.characterId) }.getOrNull()
        val corporation = public?.corporationId?.let { id ->
            runCatching { esi.fetchCorporation(id) }.getOrNull()
        }
        val alliance = public?.allianceId?.let { id ->
            runCatching { esi.fetchAlliance(id) }.getOrNull()
        }
        val corpLabel = corporation?.let { org ->
            val ticker = org.ticker?.let { "[$it] " }.orEmpty()
            "$ticker${org.name}"
        }
        val allianceLabel = alliance?.let { org ->
            val ticker = org.ticker?.let { "[$it] " }.orEmpty()
            "$ticker${org.name}"
        }
        return LoggedInCharacter(
            characterId = tokenSet.characterId,
            name = public?.name ?: tokenSet.characterName,
            portraitUrl = portraitUrl(tokenSet.characterId),
            securityStatus = public?.securityStatus,
            location = null,
            locationStatus = null,
            walletBalance = null,
            totalSkillPoints = null,
            unallocatedSkillPoints = null,
            corporationName = corpLabel,
            corporationIconUrl = public?.corporationId?.let { corporationLogoUrl(it) },
            allianceName = allianceLabel,
            allianceIconUrl = public?.allianceId?.let { allianceLogoUrl(it) },
            skillQueue = null,
        )
    }

    private fun buildAuthorizationUrl(
        authorizationEndpoint: String,
        state: String,
        codeChallenge: String,
    ): String {
        val scope = EveSsoConfig.requestedScopes.joinToString(" ")
        val query = listOf(
            "response_type" to "code",
            "redirect_uri" to EveSsoConfig.redirectUri,
            "client_id" to EveSsoConfig.clientId,
            "scope" to scope,
            "state" to state,
            "code_challenge" to codeChallenge,
            "code_challenge_method" to "S256",
        ).joinToString("&") { (key, value) ->
            "${enc(key)}=${enc(value)}"
        }
        return "$authorizationEndpoint?$query"
    }

    private fun enc(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
