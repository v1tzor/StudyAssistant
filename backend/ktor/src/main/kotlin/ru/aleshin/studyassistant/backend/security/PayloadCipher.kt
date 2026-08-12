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

package ru.aleshin.studyassistant.backend.security

import java.nio.charset.StandardCharsets.UTF_8
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class PayloadCipher(
    key: ByteArray,
    private val secureRandom: SecureRandom = SecureRandom(),
) {

    private val secretKey = SecretKeySpec(
        key.copyOf(),
        KEY_ALGORITHM,
    )

    init {
        require(key.size == KEY_SIZE_BYTES) {
            "AES-256 key must contain exactly $KEY_SIZE_BYTES bytes"
        }
    }

    fun encrypt(plaintext: ByteArray, purpose: PayloadPurpose): EncryptedPayload {
        require(plaintext.isNotEmpty()) {
            "Payload must not be empty"
        }

        val nonce = ByteArray(NONCE_SIZE_BYTES)

        secureRandom.nextBytes(nonce)

        val cipher = createCipher(
            mode = Cipher.ENCRYPT_MODE,
            nonce = nonce,
            purpose = purpose,
        )

        return EncryptedPayload(
            ciphertext = cipher.doFinal(plaintext),
            nonce = nonce,
        )
    }

    fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        purpose: PayloadPurpose,
    ): ByteArray {
        require(ciphertext.isNotEmpty()) {
            "Ciphertext must not be empty"
        }

        require(nonce.size == NONCE_SIZE_BYTES) {
            "AES-GCM nonce must contain exactly $NONCE_SIZE_BYTES bytes"
        }

        val cipher = createCipher(
            mode = Cipher.DECRYPT_MODE,
            nonce = nonce,
            purpose = purpose,
        )

        return cipher.doFinal(ciphertext)
    }

    private fun createCipher(
        mode: Int,
        nonce: ByteArray,
        purpose: PayloadPurpose,
    ): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(mode, secretKey, GCMParameterSpec(TAG_SIZE_BITS, nonce))
        cipher.updateAAD(purpose.value.toByteArray(UTF_8))

        return cipher
    }

    private companion object {

        const val KEY_ALGORITHM = "AES"
        const val TRANSFORMATION = "AES/GCM/NoPadding"

        const val KEY_SIZE_BYTES = 32
        const val NONCE_SIZE_BYTES = 12

        const val TAG_SIZE_BITS = 128
    }
}

/**
 * Encrypted AES-GCM payload.
 *
 * Authentication tag is included in [ciphertext].
 */
data class EncryptedPayload(
    val ciphertext: ByteArray,
    val nonce: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedPayload

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!nonce.contentEquals(other.nonce)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + nonce.contentHashCode()
        return result
    }
}