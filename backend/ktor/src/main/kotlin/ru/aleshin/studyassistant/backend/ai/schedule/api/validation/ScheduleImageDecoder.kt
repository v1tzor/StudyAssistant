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

package ru.aleshin.studyassistant.backend.ai.schedule.api.validation

import java.util.Base64

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal object ScheduleImageDecoder {

    fun decode(imageBase64: String): ByteArray? {
        val normalized = imageBase64.trim()
        if (normalized.isEmpty() || normalized.length > MAX_BASE64_CHARACTERS) {
            return null
        }
        return runCatching {
            Base64.getDecoder().decode(normalized)
        }.getOrNull()
    }

    fun mimeTypeOf(bytes: ByteArray): String? {
        return when {
            bytes.size >= JPEG_MAGIC.size &&
                bytes[0] == JPEG_MAGIC[0] &&
                bytes[1] == JPEG_MAGIC[1] &&
                bytes[2] == JPEG_MAGIC[2] -> IMAGE_JPEG
            bytes.size >= PNG_MAGIC.size &&
                PNG_MAGIC.indices.all { index -> bytes[index] == PNG_MAGIC[index] } -> IMAGE_PNG
            else -> null
        }
    }

    fun normalizeDeclaredMime(value: String): String? {
        return when (value.trim().lowercase()) {
            IMAGE_JPEG, "image/jpg" -> IMAGE_JPEG
            IMAGE_PNG -> IMAGE_PNG
            else -> null
        }
    }

    const val IMAGE_JPEG = "image/jpeg"
    const val IMAGE_PNG = "image/png"
    const val MIN_IMAGE_BYTES = 2_048

    private const val MAX_BASE64_CHARACTERS = 1_400_000
    private val JPEG_MAGIC = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val PNG_MAGIC = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
    )
}
