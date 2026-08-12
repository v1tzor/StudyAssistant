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

import java.util.Locale

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
@JvmInline
value class ShareCode private constructor(
    val value: String,
) {

    fun formatted(): String {
        return value.chunked(GROUP_SIZE).joinToString("-")
    }

    companion object {

        const val LENGTH = 12

        const val ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

        private const val GROUP_SIZE = 4

        fun parse(raw: String): ShareCode {
            val normalized = raw
                .filterNot { character -> character == '-' || character.isWhitespace() }
                .uppercase(Locale.ROOT)

            require(normalized.length == LENGTH) {
                "Share code must contain exactly $LENGTH characters"
            }

            require(normalized.all { character -> character in ALPHABET }) {
                "Share code contains unsupported characters"
            }

            return ShareCode(value = normalized)
        }
    }
}