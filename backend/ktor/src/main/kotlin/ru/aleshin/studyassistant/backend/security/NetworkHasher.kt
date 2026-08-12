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

import java.net.InetAddress

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class NetworkHasher(
    secret: ByteArray,
) {

    private val hasher = HmacHasher(
        secret = secret,
        domain = DOMAIN,
    )

    fun hash(remoteAddress: String): ByteArray {
        require(remoteAddress.isNotBlank())
        val addressBytes = InetAddress
            .getByName(remoteAddress.substringBefore('%'))
            .address
        val networkBytes = when (addressBytes.size) {
            IPV4_SIZE_BYTES -> addressBytes
            IPV6_SIZE_BYTES -> addressBytes.copyOf().apply {
                fill(
                    element = 0,
                    fromIndex = IPV6_NETWORK_PREFIX_BYTES,
                )
            }
            else -> error("Unsupported network address")
        }

        return hasher.hash(
            value = byteArrayOf(networkBytes.size.toByte()) + networkBytes,
        )
    }

    private companion object {
        const val DOMAIN = "studyassistant:registration-network:v1"
        const val IPV4_SIZE_BYTES = 4
        const val IPV6_SIZE_BYTES = 16
        const val IPV6_NETWORK_PREFIX_BYTES = 8
    }
}
