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

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class InstallationCredentialService(
    secret: ByteArray,
    private val secureRandom: SecureRandom = SecureRandom(),
) {

    private val hasher = HmacHasher(
        secret = secret,
        domain = DOMAIN,
    )

    fun issue(): String {
        val nonce = ByteArray(NONCE_SIZE_BYTES).also(secureRandom::nextBytes)
        val encodedNonce = encoder.encodeToString(nonce)
        val payload = "$VERSION.$encodedNonce"
        val signature = encoder.encodeToString(hasher.hash(payload))

        return "$payload.$signature"
    }

    fun isValid(credential: String): Boolean {
        if (!CREDENTIAL_PATTERN.matches(credential)) return false

        val lastSeparator = credential.lastIndexOf('.')
        val payload = credential.substring(startIndex = 0, endIndex = lastSeparator)
        val suppliedSignature = runCatching {
            decoder.decode(credential.substring(startIndex = lastSeparator + 1))
        }.getOrNull() ?: return false
        val expectedSignature = hasher.hash(payload)

        return MessageDigest.isEqual(expectedSignature, suppliedSignature)
    }

    private companion object {

        const val VERSION = "v1"
        const val NONCE_SIZE_BYTES = 32
        const val DOMAIN = "studyassistant:installation-credential:v1"

        val encoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()
        val decoder: Base64.Decoder = Base64.getUrlDecoder()
        val CREDENTIAL_PATTERN = Regex("^v1\\.[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}$")
    }
}
