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

package ru.aleshin.studyassistant.backend.ai.schedule

import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleExtractionRequestDto
import ru.aleshin.studyassistant.backend.ai.schedule.api.validation.ScheduleImageDecoder
import java.util.Base64
import java.util.UUID

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal fun testJpegBytes(size: Int = ScheduleImageDecoder.MIN_IMAGE_BYTES): ByteArray {
    val bytes = ByteArray(size)
    bytes[0] = 0xFF.toByte()
    bytes[1] = 0xD8.toByte()
    bytes[2] = 0xFF.toByte()
    bytes[size - 2] = 0xFF.toByte()
    bytes[size - 1] = 0xD9.toByte()
    return bytes
}

internal fun testScheduleExtractionRequestDto(
    requestId: String = UUID.randomUUID().toString(),
    imageBytes: ByteArray = testJpegBytes(),
    note: String? = "9б",
    numberOfWeeks: Int = 1,
    todayDate: String = "2026-08-16",
): ScheduleExtractionRequestDto {
    return ScheduleExtractionRequestDto(
        requestId = requestId,
        imageBase64 = Base64.getEncoder().encodeToString(imageBytes),
        imageMimeType = ScheduleImageDecoder.IMAGE_JPEG,
        note = note,
        locale = "ru-RU",
        timeZone = "Europe/Moscow",
        numberOfWeeks = numberOfWeeks,
        todayDate = todayDate,
    )
}
