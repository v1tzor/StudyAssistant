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

package ru.aleshin.studyassistant.core.data.mappers.schedules

import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEntry
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEventType
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportRequest
import ru.aleshin.studyassistant.core.remote.models.ai.schedule.ScheduleExtractionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.schedule.ScheduleImportDraftPojo
import ru.aleshin.studyassistant.core.remote.models.ai.schedule.ScheduleImportEntryPojo
import kotlin.io.encoding.Base64

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal fun ScheduleImportRequest.mapToRemote() = ScheduleExtractionRequestPojo(
    requestId = requestId,
    imageBase64 = Base64.encode(imageBytes),
    imageMimeType = imageMimeType,
    note = note,
    locale = locale,
    timeZone = timeZone,
    numberOfWeeks = numberOfWeeks,
    todayDate = todayDate,
)

internal fun ScheduleImportDraftPojo.mapToDomain() = ScheduleImportDraft(
    title = title,
    entries = entries.map(ScheduleImportEntryPojo::mapToDomain)
)

private fun ScheduleImportEntryPojo.mapToDomain() = ScheduleImportEntry(
    repeatWeek = repeatWeek,
    dayOfWeek = dayOfWeek,
    classNumber = classNumber,
    startTime = startTime,
    endTime = endTime,
    subject = subject,
    eventType = eventType?.name?.let(ScheduleImportEventType::valueOf),
    teacher = teacher,
    office = office,
)
