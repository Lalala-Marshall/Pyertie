package com.marshall.pyerite.eveAuthModule

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

internal object EveSsoPkce {
    private const val VERIFIER_BYTE_LENGTH = 32
    private const val DEFAULT_STATE_LENGTH = 24
    private const val HASH_ALGORITHM_SHA_256 = "SHA-256"
    private const val STATE_ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    fun generateVerifier(): String {
        val bytes = ByteArray(VERIFIER_BYTE_LENGTH)
        SecureRandom().nextBytes(bytes)
        return base64UrlNoPadding(bytes)
    }

    fun challengeS256(verifier: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM_SHA_256)
            .digest(verifier.toByteArray(Charsets.US_ASCII))
        return base64UrlNoPadding(digest)
    }

    fun generateState(length: Int = DEFAULT_STATE_LENGTH): String {
        val random = SecureRandom()
        return buildString(length) {
            repeat(length) {
                append(STATE_ALPHABET[random.nextInt(STATE_ALPHABET.length)])
            }
        }
    }

    private fun base64UrlNoPadding(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
