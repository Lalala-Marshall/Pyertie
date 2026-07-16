package com.marshall.pyerite.characterModule.auth

import android.net.Uri
import android.util.Base64
import com.marshall.pyerite.data.network.PyeriteJson
import kotlinx.serialization.decodeFromString
import java.nio.charset.StandardCharsets

object EveJwtDecoder {

    fun decodeUnverified(accessToken: String): EveJwtClaims {
        val parts = accessToken.split('.')
        require(parts.size >= 2) { "Invalid JWT" }
        val payloadJson = String(
            Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
            StandardCharsets.UTF_8,
        )
        val payload = PyeriteJson.decodeFromString<EveJwtPayloadDto>(payloadJson)
        val characterId = payload.sub.substringAfterLast(':').toLongOrNull()
            ?: error("JWT sub missing character id: ${payload.sub}")
        return EveJwtClaims(
            characterId = characterId,
            characterName = payload.name.ifBlank { "Character $characterId" },
            expiresAtEpochMs = payload.exp * 1_000L,
            scopes = payload.scp,
            audience = payload.aud,
            issuer = payload.iss,
        )
    }

    fun assertBasicClaims(claims: EveJwtClaims, clientId: String) {
        require(claims.issuer in EveSsoConfig.acceptedIssuers) {
            "Unexpected JWT issuer: ${claims.issuer}"
        }
        require(claims.audience.contains(clientId)) {
            "JWT audience missing client id"
        }
        require(
            claims.audience.any {
                it.equals(EveSsoConfig.JWT_AUDIENCE_EVE_ONLINE, ignoreCase = true)
            },
        ) {
            "JWT audience missing ${EveSsoConfig.JWT_AUDIENCE_EVE_ONLINE}"
        }
        require(claims.expiresAtEpochMs > System.currentTimeMillis()) {
            "Access token expired"
        }
    }
}

fun Uri.queryParamOrNull(name: String): String? =
    getQueryParameter(name)?.takeIf { it.isNotBlank() }
