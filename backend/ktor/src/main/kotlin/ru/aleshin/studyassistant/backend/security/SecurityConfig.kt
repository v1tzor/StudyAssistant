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

import io.ktor.server.config.ApplicationConfig
import ru.aleshin.studyassistant.backend.common.config.SecretValueReader

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class SecurityConfig(
    val installationHmacSecret: ByteArray,
    val shareHmacSecret: ByteArray,
    val payloadEncryptionKey: ByteArray,
) {

    companion object {

        private const val KEY_SIZE_BYTES = 32
        private const val KEY_SIZE_HEX = KEY_SIZE_BYTES * 2

        fun from(
            applicationConfig: ApplicationConfig,
            secretValueReader: SecretValueReader = SecretValueReader(),
        ): SecurityConfig {
            val config = applicationConfig.config("security")

            return SecurityConfig(
                installationHmacSecret = decodeKey(
                    name = "installationHmacSecret",
                    value = secretValueReader.read(
                        config = config,
                        propertyName = "installationHmacSecret",
                        environmentName = "INSTALLATION_HMAC_SECRET",
                    ),
                ),
                shareHmacSecret = decodeKey(
                    name = "shareHmacSecret",
                    value = secretValueReader.read(
                        config = config,
                        propertyName = "shareHmacSecret",
                        environmentName = "SHARE_HMAC_SECRET",
                    ),
                ),
                payloadEncryptionKey = decodeKey(
                    name = "payloadEncryptionKey",
                    value = secretValueReader.read(
                        config = config,
                        propertyName = "payloadEncryptionKey",
                        environmentName = "PAYLOAD_ENCRYPTION_KEY",
                    ),
                ),
            )
        }

        private fun decodeKey(name: String, value: String): ByteArray {
            require(value.length == KEY_SIZE_HEX) {
                "$name must contain exactly $KEY_SIZE_HEX hex characters"
            }

            require(value.all { character -> character.digitToIntOrNull(radix = 16) != null }) {
                "$name must contain only hexadecimal characters"
            }

            return ByteArray(KEY_SIZE_BYTES) { index ->
                value
                    .substring(startIndex = index * 2, endIndex = index * 2 + 2)
                    .toInt(radix = 16)
                    .toByte()
            }
        }
    }
}
