package com.marshall.pyerite.eveAuthModule.sso

import android.net.Uri
import android.util.Base64
import com.marshall.pyerite.eveAuthModule.model.EveJWTsDto
import com.marshall.pyerite.eveAuthModule.model.EveJwtClaims
import com.marshall.pyerite.eveAuthModule.model.EveJwtPayloadDto
import com.marshall.pyerite.eveAuthModule.model.EveJwkDto
import com.marshall.pyerite.infra.network.PyeriteJson
import kotlinx.serialization.Serializable
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec

internal object EveJwtDecoder {

    fun decodeAndVerify(accessToken: String, jwks: EveJWTsDto): EveJwtClaims {
        val parts = accessToken.split('.')
        require(parts.size == 3) { "Invalid JWT" }
        val header = PyeriteJson.decodeFromString<EveJwtHeaderDto>(
            String(base64UrlDecode(parts[0]), StandardCharsets.UTF_8),
        )
        require(header.alg.equals(EveSsoConfig.JWT_ALGORITHM_RS256, ignoreCase = true)) {
            "Unexpected JWT alg: ${header.alg}"
        }
        val key = jwks.keys.firstOrNull { it.kid == header.kid }
            ?: error("No JWKS key for kid=${header.kid}")
        val publicKey = rsaPublicKey(key)
        val signingInput = "${parts[0]}.${parts[1]}".toByteArray(StandardCharsets.US_ASCII)
        val signatureBytes = base64UrlDecode(parts[2])
        val verified = Signature.getInstance("SHA256withRSA").run {
            initVerify(publicKey)
            update(signingInput)
            verify(signatureBytes)
        }
        require(verified) { "JWT signature verification failed" }

        val payload = PyeriteJson.decodeFromString<EveJwtPayloadDto>(
            String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8),
        )
        val characterId = payload.sub.substringAfterLast(':').toLongOrNull()
            ?: error("JWT sub missing character id: ${payload.sub}")
        return EveJwtClaims(
            characterId = characterId,
            characterName = payload.name.takeIf { it.isNotBlank() }.orEmpty(),
            expiresAtEpochMs = payload.exp * EveSsoConfig.MILLIS_PER_SECOND,
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

    private fun rsaPublicKey(jwk: EveJwkDto): RSAPublicKey {
        val modulus = BigInteger(1, base64UrlDecode(jwk.n))
        val exponent = BigInteger(1, base64UrlDecode(jwk.e))
        val spec = RSAPublicKeySpec(modulus, exponent)
        return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }

    private fun base64UrlDecode(value: String): ByteArray =
        Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

@Serializable
internal data class EveJwtHeaderDto(
    val alg: String = "",
    val kid: String = "",
    val typ: String = "",
)

fun Uri.queryParamOrNull(name: String): String? =
    getQueryParameter(name)?.takeIf { it.isNotBlank() }
