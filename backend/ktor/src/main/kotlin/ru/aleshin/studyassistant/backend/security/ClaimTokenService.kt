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
 * @author Stanislav Aleshin on 11.08.2026.
 */
class ClaimTokenService(
    secret: ByteArray,
    private val secureRandom: SecureRandom = SecureRandom(),
) {

    private val hasher = HmacHasher(
        secret = secret,
        domain = DOMAIN,
    )

    fun generate(): String {
        val bytes = ByteArray(TOKEN_SIZE_BYTES)

        secureRandom.nextBytes(bytes)

        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    fun hash(token: String): ByteArray {
        require(token.isNotBlank()) { "Claim token must not be blank" }
        return hasher.hash(value = token)
    }

    fun verify(token: String, expectedHash: ByteArray): Boolean {
        val actualHash = hash(token = token)
        return MessageDigest.isEqual(actualHash, expectedHash)
    }

    private companion object {
        const val TOKEN_SIZE_BYTES = 32

        const val DOMAIN = "studyassistant:claim:v1"
    }
}