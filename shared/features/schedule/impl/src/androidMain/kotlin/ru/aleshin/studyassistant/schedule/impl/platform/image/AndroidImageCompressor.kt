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

package ru.aleshin.studyassistant.schedule.impl.platform.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.withContext
import ru.aleshin.studyassistant.core.common.managers.AppDispatchers
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleTextRecognitionException
import ru.aleshin.studyassistant.schedule.impl.platform.CompressedScheduleImage
import ru.aleshin.studyassistant.schedule.impl.platform.ImageCompressor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal class AndroidImageCompressor(
    private val dispatchers: AppDispatchers,
) : ImageCompressor {

    override suspend fun compress(image: ByteArray): CompressedScheduleImage = withContext(dispatchers.default) {
        if (image.isEmpty()) throw ScheduleTextRecognitionException.InvalidImage
        if (image.size > MAX_SOURCE_BYTES) throw ScheduleTextRecognitionException.ImageTooLarge

        val decoded = BitmapFactory.decodeByteArray(image, 0, image.size)
            ?: throw ScheduleTextRecognitionException.InvalidImage
        val oriented = applyExifOrientation(image = image, bitmap = decoded)
        val scaled = scale(oriented)

        val qualities = intArrayOf(75, 70, 65)
        qualities.forEach { quality ->
            val encoded = encodeJpeg(bitmap = scaled, quality = quality)
            if (encoded.size <= TARGET_MAX_BYTES) {
                return@withContext CompressedScheduleImage(
                    bytes = encoded,
                    mimeType = JPEG_MIME,
                )
            }
        }

        val encoded = encodeJpeg(bitmap = scaled, quality = 65)
        if (encoded.size > TARGET_MAX_BYTES) throw ScheduleTextRecognitionException.ImageTooLarge
        CompressedScheduleImage(bytes = encoded, mimeType = JPEG_MIME)
    }

    private fun scale(bitmap: Bitmap): Bitmap {
        val longSide = max(bitmap.width, bitmap.height)
        if (longSide <= MAX_LONG_SIDE) return bitmap
        val scale = MAX_LONG_SIDE.toFloat() / longSide.toFloat()
        val width = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
        val height = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun encodeJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        return output.toByteArray()
    }

    private fun applyExifOrientation(image: ByteArray, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            ExifInterface(ByteArrayInputStream(image)).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private companion object {
        const val JPEG_MIME = "image/jpeg"
        const val MAX_LONG_SIDE = 1600
        const val TARGET_MAX_BYTES = 450 * 1024
        const val MAX_SOURCE_BYTES = 20 * 1024 * 1024
    }
}
