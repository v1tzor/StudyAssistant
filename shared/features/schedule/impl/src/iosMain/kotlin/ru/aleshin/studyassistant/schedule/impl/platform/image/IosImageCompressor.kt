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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.aleshin.studyassistant.schedule.impl.platform.image

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import ru.aleshin.studyassistant.core.common.managers.AppDispatchers
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleTextRecognitionException
import ru.aleshin.studyassistant.schedule.impl.platform.CompressedScheduleImage
import ru.aleshin.studyassistant.schedule.impl.platform.ImageCompressor

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal class IosImageCompressor(
    private val dispatchers: AppDispatchers,
) : ImageCompressor {

    override suspend fun compress(image: ByteArray): CompressedScheduleImage = withContext(dispatchers.default) {
        if (image.isEmpty()) throw ScheduleTextRecognitionException.InvalidImage
        if (image.size > MAX_SOURCE_BYTES) throw ScheduleTextRecognitionException.ImageTooLarge

        val source = image.toNsData()
        val decoded = UIImage.imageWithData(source) ?: throw ScheduleTextRecognitionException.InvalidImage
        val scaled = scale(decoded)

        val qualities = doubleArrayOf(0.75, 0.70, 0.65)
        qualities.forEach { quality ->
            val encoded = UIImageJPEGRepresentation(scaled, quality)?.toByteArray()
                ?: throw ScheduleTextRecognitionException.InvalidImage
            if (encoded.size <= TARGET_MAX_BYTES) {
                return@withContext CompressedScheduleImage(
                    bytes = encoded,
                    mimeType = JPEG_MIME,
                )
            }
        }

        val encoded = UIImageJPEGRepresentation(scaled, 0.65)?.toByteArray()
            ?: throw ScheduleTextRecognitionException.InvalidImage
        if (encoded.size > TARGET_MAX_BYTES) throw ScheduleTextRecognitionException.ImageTooLarge
        CompressedScheduleImage(bytes = encoded, mimeType = JPEG_MIME)
    }

    private fun scale(image: UIImage): UIImage {
        val size = image.size.useContents { width to height }
        val longSide = maxOf(size.first, size.second)
        if (longSide <= MAX_LONG_SIDE) return image
        val scale = MAX_LONG_SIDE / longSide
        val width = size.first * scale
        val height = size.second * scale
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(width, height), false, 1.0)
        image.drawInRect(CGRectMake(0.0, 0.0, width, height))
        val scaled = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return scaled ?: image
    }

    private fun ByteArray.toNsData(): NSData = usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
    }

    private fun NSData.toByteArray(): ByteArray {
        val length = length.toInt()
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
        return bytes
    }

    private companion object {
        const val JPEG_MIME = "image/jpeg"
        const val MAX_LONG_SIDE = 1600.0
        const val TARGET_MAX_BYTES = 450 * 1024
        const val MAX_SOURCE_BYTES = 20 * 1024 * 1024
    }
}
