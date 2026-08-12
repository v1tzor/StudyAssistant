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

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
internal class HmacHasher(
    secret: ByteArray,
    domain: String,
) {

    private val secretKey = SecretKeySpec(secret.copyOf(), ALGORITHM)

    private val domainBytes = domain.toByteArray(StandardCharsets.UTF_8)

    fun hash(value: String): ByteArray {
        return hash(value = value.toByteArray(StandardCharsets.UTF_8))
    }

    fun hash(value: ByteArray): ByteArray {
        val mac = Mac.getInstance(ALGORITHM)

        mac.init(secretKey)

        mac.update(domainBytes)
        mac.update(SEPARATOR)

        return mac.doFinal(value)
    }

    private companion object {

        const val ALGORITHM = "HmacSHA256"

        val SEPARATOR = byteArrayOf(0)
    }
}