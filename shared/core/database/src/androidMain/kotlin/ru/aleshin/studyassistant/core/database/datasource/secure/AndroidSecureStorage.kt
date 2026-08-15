/*
 * Copyright 2026 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.aleshin.studyassistant.core.database.datasource.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal class AndroidSecureStorage(
    context: Context,
) {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    init {
        runCatching {
            context
                .getSharedPreferences(LEGACY_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
            KeyStore
                .getInstance(ANDROID_KEY_STORE)
                .apply { load(null) }
                .takeIf { keyStore -> keyStore.containsAlias(LEGACY_KEY_ALIAS) }
                ?.deleteEntry(LEGACY_KEY_ALIAS)
        }
    }

    fun write(key: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, fetchOrCreateSecretKey())
        cipher.updateAAD(key.encodeToByteArray())
        val encoded = cipher.iv + cipher.doFinal(value.encodeToByteArray())
        check(
            preferences
                .edit()
                .putString(key, Base64.encodeToString(encoded, Base64.NO_WRAP))
                .commit(),
        )
    }

    fun read(key: String): String? {
        val encoded = preferences.getString(key, null) ?: return null
        return runCatching {
            val encrypted = Base64.decode(encoded, Base64.NO_WRAP)
            val iv = encrypted.copyOfRange(0, GCM_IV_SIZE)
            val cipherText = encrypted.copyOfRange(GCM_IV_SIZE, encrypted.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, fetchOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_SIZE, iv))
            cipher.updateAAD(key.encodeToByteArray())
            cipher.doFinal(cipherText).decodeToString()
        }.getOrNull()
    }

    fun delete(key: String) {
        check(preferences.edit().remove(key).commit())
    }

    private fun fetchOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(AES_KEY_SIZE_BITS)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "installation_secure_storage"
        const val KEY_ALIAS = "study_assistant_installation"
        const val LEGACY_PREFERENCES_NAME = "ai_secure_storage"
        const val LEGACY_KEY_ALIAS = "study_assistant_ai"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_SIZE = 12
        const val GCM_TAG_SIZE = 128
        const val AES_KEY_SIZE_BITS = 256
    }
}
