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

import ru.aleshin.studyassistant.backend.ai.AiConfig
import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleExtractionRequestDto
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class ScheduleExtractionRequestValidator(
    private val config: AiConfig,
) {

    fun validate(request: ScheduleExtractionRequestDto) {
        val locale = request.locale.replace('_', '-')
        val note = request.note?.trim().orEmpty()
        val declaredMime = ScheduleImageDecoder.normalizeDeclaredMime(request.imageMimeType)
        val imageBytes = ScheduleImageDecoder.decode(request.imageBase64)
        val detectedMime = imageBytes?.let(ScheduleImageDecoder::mimeTypeOf)

        if (
            runCatching { UUID.fromString(request.requestId) }.isFailure ||
            declaredMime == null ||
            imageBytes == null ||
            detectedMime == null ||
            detectedMime != declaredMime ||
            imageBytes.size < ScheduleImageDecoder.MIN_IMAGE_BYTES ||
            imageBytes.size > config.maxScheduleImageBytes ||
            !ScheduleImageGuard.hasSafeDimensions(imageBytes) ||
            note.length > config.maxScheduleNoteCharacters ||
            !LOCALE_PATTERN.matches(locale) ||
            Locale.forLanguageTag(locale).language.isBlank() ||
            runCatching { ZoneId.of(request.timeZone) }.isFailure ||
            runCatching { LocalDate.parse(request.todayDate) }.isFailure ||
            request.numberOfWeeks !in MIN_REPEAT_WEEKS..MAX_REPEAT_WEEKS
        ) {
            throw InvalidRequestException()
        }
    }

    private companion object {

        const val MIN_REPEAT_WEEKS = 1
        const val MAX_REPEAT_WEEKS = 3

        val LOCALE_PATTERN = Regex("^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8}){0,3}$")
    }
}
