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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter

import ru.aleshin.studyassistant.backend.ai.schedule.api.validation.ScheduleImageDecoder
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
class ScheduleImageNormalizer {

    fun normalize(imageBytes: ByteArray, mimeType: String): NormalizedScheduleImage {
        val image = runCatching {
            ImageIO.read(ByteArrayInputStream(imageBytes))
        }.getOrNull()
        if (image == null || image.width <= 0 || image.height <= 0) {
            return NormalizedScheduleImage(
                bytes = imageBytes,
                mimeType = mimeType,
            )
        }

        val longSide = max(image.width, image.height)
        val alreadyCompact = longSide <= MAX_LONG_SIDE &&
            imageBytes.size <= TARGET_MAX_BYTES &&
            mimeType == ScheduleImageDecoder.IMAGE_JPEG
        if (alreadyCompact) {
            return NormalizedScheduleImage(
                bytes = imageBytes,
                mimeType = mimeType,
            )
        }

        val scale = min(1.0, MAX_LONG_SIDE.toDouble() / longSide.toDouble())
        val width = (image.width * scale).roundToInt().coerceAtLeast(1)
        val height = (image.height * scale).roundToInt().coerceAtLeast(1)
        val scaled = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = scaled.createGraphics()
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
        )
        graphics.drawImage(image, 0, 0, width, height, null)
        graphics.dispose()

        val encoded = encodeJpeg(image = scaled, quality = JPEG_QUALITY)
            ?: return NormalizedScheduleImage(bytes = imageBytes, mimeType = mimeType)
        return NormalizedScheduleImage(
            bytes = encoded,
            mimeType = ScheduleImageDecoder.IMAGE_JPEG,
        )
    }

    private fun encodeJpeg(image: BufferedImage, quality: Float): ByteArray? {
        val writers = ImageIO.getImageWritersByFormatName(JPEG_FORMAT)
        if (!writers.hasNext()) return null
        val writer = writers.next()
        val output = ByteArrayOutputStream()
        return try {
            ImageIO.createImageOutputStream(output).use { imageOutput ->
                writer.output = imageOutput
                val param = writer.defaultWriteParam
                if (param.canWriteCompressed()) {
                    param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                    param.compressionQuality = quality
                }
                writer.write(null, IIOImage(image, null, null), param)
            }
            output.toByteArray()
        } catch (_: Exception) {
            null
        } finally {
            writer.dispose()
        }
    }

    private companion object {

        const val MAX_LONG_SIDE = 1_600
        const val TARGET_MAX_BYTES = 450_000
        const val JPEG_QUALITY = 0.75f
        const val JPEG_FORMAT = "jpeg"
    }
}
