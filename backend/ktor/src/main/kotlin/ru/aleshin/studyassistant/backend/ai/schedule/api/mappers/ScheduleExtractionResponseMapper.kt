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

import ru.aleshin.studyassistant.backend.ai.domain.model.AiQuota
import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleDraftDto
import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleDraftEntryDto
import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleEventTypeDto
import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleExtractionResponseDto
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleDraft
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleEventType
import java.time.Instant

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class ScheduleExtractionResponseMapper {

    fun map(
        draft: ScheduleDraft,
        quota: AiQuota,
        quotaResetAt: Instant,
    ): ScheduleExtractionResponseDto {
        return ScheduleExtractionResponseDto(
            draft = ScheduleDraftDto(
                title = draft.title,
                entries = draft.entries.map { entry ->
                    ScheduleDraftEntryDto(
                        repeatWeek = entry.repeatWeek,
                        dayOfWeek = entry.dayOfWeek,
                        classNumber = entry.classNumber,
                        startTime = entry.startTime,
                        endTime = entry.endTime,
                        subject = entry.subject,
                        eventType = entry.eventType?.let { eventType ->
                            when (eventType) {
                                ScheduleEventType.LESSON -> ScheduleEventTypeDto.LESSON
                                ScheduleEventType.LECTURE -> ScheduleEventTypeDto.LECTURE
                                ScheduleEventType.PRACTICE -> ScheduleEventTypeDto.PRACTICE
                                ScheduleEventType.SEMINAR -> ScheduleEventTypeDto.SEMINAR
                                ScheduleEventType.CLASS -> ScheduleEventTypeDto.CLASS
                                ScheduleEventType.ONLINE_CLASS -> ScheduleEventTypeDto.ONLINE_CLASS
                                ScheduleEventType.WEBINAR -> ScheduleEventTypeDto.WEBINAR
                            }
                        },
                        teacher = entry.teacher,
                        office = entry.office,
                        location = entry.location,
                        organization = entry.organization,
                        notes = entry.notes,
                    )
                },
                unparsedLines = draft.unparsedLines,
            ),
            quotaRemaining = quota.remaining,
            quotaResetAt = quotaResetAt.toEpochMilli(),
        )
    }
}
