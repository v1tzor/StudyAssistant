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

package ru.aleshin.studyassistant.core.data.utils.share

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal object ShareCode {
    private const val ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private const val RAW_CODE_LENGTH = 12

    fun normalize(value: String): String {
        val normalized = value.uppercase()
            .filterNot { it == '-' || it.isWhitespace() }
            .replace('O', '0')
            .replace('I', '1')
            .replace('L', '1')
        require(normalized.length == RAW_CODE_LENGTH && normalized.all(ALPHABET::contains)) {
            "Invalid share code"
        }
        return normalized
    }

    fun format(normalized: String): String = normalized.chunked(4).joinToString("-")
}
