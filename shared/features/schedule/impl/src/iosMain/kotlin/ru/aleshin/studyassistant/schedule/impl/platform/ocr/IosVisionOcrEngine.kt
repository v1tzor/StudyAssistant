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

package ru.aleshin.studyassistant.schedule.impl.platform.ocr

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrEngine
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrLanguage
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrLine
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrRect
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrResult
import ru.aleshin.studyassistant.core.common.managers.AppDispatchers

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal class IosVisionOcrEngine(
    private val dispatchers: AppDispatchers,
) : OcrEngine {

    override suspend fun recognize(
        image: ByteArray,
        languages: Set<OcrLanguage>
    ): OcrResult = withContext(dispatchers.default) {
        val data = image.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), image.size.toULong())
        }

        val requestedLangs = languages.map { lang ->
            when (lang) {
                OcrLanguage.RUSSIAN -> "ru-RU"
                OcrLanguage.ENGLISH -> "en-US"
            }
        }

        var result: OcrResult? = null
        var error: Throwable? = null

        val request = VNRecognizeTextRequest { completedRequest, requestError ->
            if (requestError != null) {
                error = Exception(requestError.localizedDescription)
                return@VNRecognizeTextRequest
            }
            val observations = completedRequest?.results?.filterIsInstance<VNRecognizedTextObservation>() ?: emptyList()
            val lines = observations.map { observation ->
                val candidate = observation.topCandidates(1uL).firstOrNull() as? VNRecognizedText
                val text = candidate?.string ?: ""
                val box = observation.boundingBox.useContents {
                    OcrRect(
                        left = origin.x.toFloat(),
                        top = (1.0 - (origin.y + size.height)).toFloat(),
                        right = (origin.x + size.width).toFloat(),
                        bottom = (1.0 - origin.y).toFloat()
                    )
                }
                OcrLine(
                    text = text,
                    confidence = observation.confidence,
                    box = box,
                    words = emptyList()
                )
            }
            result = OcrResult(
                text = lines.joinToString("\n") { it.text },
                lines = lines
            )
        }.apply {
            recognitionLevel = VNRequestTextRecognitionLevelAccurate
            usesLanguageCorrection = true
            recognitionLanguages = requestedLangs
        }

        val handler = VNImageRequestHandler(data, options = emptyMap<Any?, Any>())
        handler.performRequests(listOf(request), error = null)

        error?.let { throw it }
        return@withContext result ?: OcrResult("", emptyList())
    }
}
