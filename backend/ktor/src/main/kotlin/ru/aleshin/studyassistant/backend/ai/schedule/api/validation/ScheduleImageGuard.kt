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

import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.max

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
internal object ScheduleImageGuard {

    fun requireSafeDimensions(imageBytes: ByteArray) {
        if (!hasSafeDimensions(imageBytes)) {
            throw InvalidRequestException()
        }
    }

    fun hasSafeDimensions(imageBytes: ByteArray): Boolean {
        val dimensions = readDimensions(imageBytes) ?: return false
        if (dimensions.width <= 0 || dimensions.height <= 0) return false
        val longSide = max(dimensions.width, dimensions.height)
        val pixelCount = dimensions.width.toLong() * dimensions.height.toLong()
        return longSide <= MAX_LONG_SIDE && pixelCount <= MAX_PIXELS
    }

    fun readDimensions(imageBytes: ByteArray): ImageDimensions? {
        val stream = runCatching {
            ImageIO.createImageInputStream(ByteArrayInputStream(imageBytes))
        }.getOrNull() ?: return null
        val readers = ImageIO.getImageReaders(stream)
        if (!readers.hasNext()) {
            stream.close()
            return null
        }
        val reader = readers.next()
        return try {
            reader.input = stream
            ImageDimensions(
                width = reader.getWidth(0),
                height = reader.getHeight(0),
            )
        } catch (_: Exception) {
            null
        } finally {
            reader.dispose()
            stream.close()
        }
    }

    data class ImageDimensions(
        val width: Int,
        val height: Int,
    )

    const val MAX_LONG_SIDE = 4_096
    const val MAX_PIXELS = 4_096L * 4_096L
}
