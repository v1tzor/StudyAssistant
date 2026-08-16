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

package ru.aleshin.studyassistant.backend.ai.schedule.api.mappers

import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleExtractionRequestDto
import ru.aleshin.studyassistant.backend.ai.schedule.api.validation.ScheduleImageDecoder
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleExtractionCommand
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleExtractionRequest
import java.util.UUID

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class ScheduleExtractionRequestMapper {

    fun map(request: ScheduleExtractionRequestDto): ScheduleExtractionCommand {
        return ScheduleExtractionCommand(
            requestId = UUID.fromString(request.requestId),
            request = ScheduleExtractionRequest(
                imageBytes = checkNotNull(ScheduleImageDecoder.decode(request.imageBase64)),
                imageMimeType = checkNotNull(
                    ScheduleImageDecoder.normalizeDeclaredMime(request.imageMimeType),
                ),
                note = request.note?.trim()?.takeIf(String::isNotEmpty),
                locale = request.locale.replace('_', '-'),
                timeZone = request.timeZone,
                numberOfWeeks = request.numberOfWeeks,
                todayDate = request.todayDate,
            ),
        )
    }
}
