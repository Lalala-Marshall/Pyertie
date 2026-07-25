package com.marshall.pyerite.eveAuthModule.token

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import com.marshall.pyerite.eveAuthModule.model.EveTokenSet
import com.marshall.pyerite.infra.network.PyeriteJson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypted token persistence — only [EveTokenManager] may read/write. */
internal class EveTokenStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val secretKey: SecretKey by lazy { getOrCreateSecretKey() }

    fun save(tokenSet: EveTokenSet) {
        prefs.edit {
            putString(keyFor(tokenSet.characterId), encrypt(PyeriteJson.encodeToString(tokenSet)))
        }
    }

    fun get(characterId: Long): EveTokenSet? {
        val raw = prefs.getString(keyFor(characterId), null) ?: return null
        return runCatching { PyeriteJson.decodeFromString<EveTokenSet>(decrypt(raw)) }.getOrNull()
    }

    fun remove(characterId: Long) {
        prefs.edit { remove(keyFor(characterId)) }
    }

    fun all(): List<EveTokenSet> {
        return prefs.all.mapNotNull { (_, value) ->
            val raw = value as? String ?: return@mapNotNull null
            runCatching { PyeriteJson.decodeFromString<EveTokenSet>(decrypt(raw)) }.getOrNull()
        }
    }

    private fun keyFor(characterId: Long): String = "$KEY_PREFIX$characterId"

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val payload = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, payload, 0, iv.size)
        System.arraycopy(cipherBytes, 0, payload, iv.size, cipherBytes.size)
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        require(payload.size > IV_SIZE_BYTES) { "Invalid ciphertext" }
        val iv = payload.copyOfRange(0, IV_SIZE_BYTES)
        val cipherBytes = payload.copyOfRange(IV_SIZE_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return keyGenerator.generateKey()
    }

    companion object {
        private const val PREFS_NAME = "pyerite_eve_sso_tokens"
        private const val KEY_PREFIX = "character_"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "pyerite_eve_sso_master"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE_BYTES = 12
        private const val TAG_LENGTH_BITS = 128
    }
}
