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

package ru.aleshin.studyassistant.schedule.impl.platform

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import ru.aleshin.studyassistant.core.common.managers.AppDispatchers
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleTextRecognitionException
import ru.aleshin.studyassistant.schedule.impl.domain.services.ScheduleTextRecognizer

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal actual fun createScheduleTextRecognizer(
    dispatchers: AppDispatchers,
): ScheduleTextRecognizer = IosScheduleTextRecognizer(dispatchers)

private class IosScheduleTextRecognizer(
    private val dispatchers: AppDispatchers,
) : ScheduleTextRecognizer {

    override suspend fun recognize(imageBytes: ByteArray): String = withContext(dispatchers.default) {
        if (imageBytes.isEmpty()) throw ScheduleTextRecognitionException.InvalidImage

        try {
            val data = imageBytes.usePinned { pinned ->
                NSData.dataWithBytes(
                    bytes = pinned.addressOf(0),
                    length = imageBytes.size.toULong(),
                )
            }
            var recognizedText = ""
            var recognitionFailed = false
            val request = VNRecognizeTextRequest { completedRequest, error ->
                if (error != null) {
                    recognitionFailed = true
                } else {
                    recognizedText = completedRequest?.results
                        .orEmpty()
                        .mapNotNull { observation ->
                            val textObservation = observation as? VNRecognizedTextObservation
                                ?: return@mapNotNull null
                            val candidate = textObservation.topCandidates(1uL)
                                .firstOrNull() as? VNRecognizedText
                            candidate?.string
                        }
                        .joinToString("\n")
                        .trim()
                }
            }.apply {
                recognitionLevel = VNRequestTextRecognitionLevelAccurate
                usesLanguageCorrection = true
                recognitionLanguages = listOf("ru-RU", "en-US")
            }
            val handler = VNImageRequestHandler(
                data = data,
                options = emptyMap<Any?, Any>(),
            )
            val completed = handler.performRequests(listOf(request), error = null)

            when {
                !completed || recognitionFailed -> throw ScheduleTextRecognitionException.Unavailable
                recognizedText.isBlank() -> throw ScheduleTextRecognitionException.NoText
                else -> recognizedText
            }
        } catch (error: ScheduleTextRecognitionException) {
            throw error
        } catch (_: Throwable) {
            throw ScheduleTextRecognitionException.InvalidImage
        }
    }
}
