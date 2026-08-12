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

package ru.aleshin.studyassistant.schedule.impl.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.aleshin.studyassistant.core.common.managers.AppDispatchers
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleTextRecognitionException
import ru.aleshin.studyassistant.schedule.impl.domain.services.ScheduleTextRecognizer
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal actual fun createScheduleTextRecognizer(
    dispatchers: AppDispatchers,
): ScheduleTextRecognizer = AndroidScheduleTextRecognizer(dispatchers)

private class AndroidScheduleTextRecognizer(
    private val dispatchers: AppDispatchers,
) : ScheduleTextRecognizer {

    override suspend fun recognize(imageBytes: ByteArray): String {
        val bitmap = withContext(dispatchers.default) { decodeBitmap(imageBytes) }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return try {
            val text = suspendCancellableCoroutine { continuation ->
                recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { result ->
                        if (continuation.isActive) continuation.resume(result.text.trim())
                    }
                    .addOnFailureListener { error ->
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
            }
            text.ifBlank { throw ScheduleTextRecognitionException.NoText }
        } catch (error: ScheduleTextRecognitionException) {
            throw error
        } catch (_: Throwable) {
            throw ScheduleTextRecognitionException.Unavailable
        } finally {
            recognizer.close()
            bitmap.recycle()
        }
    }

    private fun decodeBitmap(imageBytes: ByteArray): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw ScheduleTextRecognitionException.InvalidImage
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
            ?: throw ScheduleTextRecognitionException.InvalidImage
        val rotation = runCatching {
            ExifInterface(ByteArrayInputStream(imageBytes)).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL).toRotationDegrees()
        if (rotation == 0f) return decoded

        return Bitmap.createBitmap(
            decoded,
            0,
            0,
            decoded.width,
            decoded.height,
            Matrix().apply { postRotate(rotation) },
            true,
        ).also { rotated ->
            if (rotated !== decoded) decoded.recycle()
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        while (
            width / sampleSize > MAX_IMAGE_DIMENSION ||
            height / sampleSize > MAX_IMAGE_DIMENSION ||
            width.toLong() * height / sampleSize / sampleSize > MAX_IMAGE_PIXELS
        ) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun Int.toRotationDegrees(): Float = when (this) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    private companion object {
        const val MAX_IMAGE_DIMENSION = 4_096
        const val MAX_IMAGE_PIXELS = 16_000_000L
    }
}
