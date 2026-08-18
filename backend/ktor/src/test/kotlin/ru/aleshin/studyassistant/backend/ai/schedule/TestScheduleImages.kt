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
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID
import javax.imageio.ImageIO

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal fun testJpegBytes(size: Int = ScheduleImageDecoder.MIN_IMAGE_BYTES): ByteArray {
    var dimension = 32
    var bytes: ByteArray
    do {
        val image = BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB)
        val output = ByteArrayOutputStream()
        check(ImageIO.write(image, "jpeg", output))
        bytes = output.toByteArray()
        dimension *= 2
    } while (bytes.size < size && dimension <= 512)
    check(bytes.size >= size)
    return bytes
}

internal fun testPngWithDeclaredSize(width: Int, height: Int): ByteArray {
    val signature = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
    )
    val ihdrType = byteArrayOf(0x49, 0x48, 0x44, 0x52)
    val ihdrData = ByteArray(13)
    writeInt(ihdrData, 0, width)
    writeInt(ihdrData, 4, height)
    ihdrData[8] = 8
    ihdrData[9] = 2
    val chunk = ByteArray(4 + ihdrType.size + ihdrData.size + 4)
    writeInt(chunk, 0, ihdrData.size)
    ihdrType.copyInto(chunk, 4)
    ihdrData.copyInto(chunk, 8)
    return signature + chunk + ByteArray(ScheduleImageDecoder.MIN_IMAGE_BYTES)
}

private fun writeInt(target: ByteArray, offset: Int, value: Int) {
    target[offset] = (value ushr 24).toByte()
    target[offset + 1] = (value ushr 16).toByte()
    target[offset + 2] = (value ushr 8).toByte()
    target[offset + 3] = value.toByte()
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
