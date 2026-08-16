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

package ru.aleshin.studyassistant.schedule.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEntry
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportDraftUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal fun ScheduleImportDraft.mapToUi() = ScheduleImportDraftUi(
    title = title.orEmpty(),
    entries = entries.mapIndexed { index, entry -> entry.mapToUi(index) },
    unparsedLines = unparsedLines,
)

internal fun ScheduleImportDraftUi.mapToDomain() = ScheduleImportDraft(
    title = title.takeIf(String::isNotBlank),
    entries = entries.map(ScheduleImportEntryUi::mapToDomain),
    unparsedLines = unparsedLines,
)

private fun ScheduleImportEntry.mapToUi(index: Int) = ScheduleImportEntryUi(
    id = index,
    repeatWeek = repeatWeek,
    dayOfWeek = dayOfWeek,
    classNumber = classNumber,
    startTime = startTime.orEmpty(),
    endTime = endTime.orEmpty(),
    subject = subject.orEmpty(),
    subjectId = subjectId,
    eventType = eventType,
    teacher = teacher.orEmpty(),
    teacherId = teacherId,
    office = office.orEmpty(),
    location = location.orEmpty(),
    organization = organization.orEmpty(),
    organizationId = organizationId,
    notes = notes.orEmpty(),
    included = included,
)

private fun ScheduleImportEntryUi.mapToDomain() = ScheduleImportEntry(
    repeatWeek = repeatWeek,
    dayOfWeek = dayOfWeek,
    classNumber = classNumber,
    startTime = startTime.takeIf(String::isNotBlank),
    endTime = endTime.takeIf(String::isNotBlank),
    subject = subject.takeIf(String::isNotBlank),
    subjectId = subjectId,
    eventType = eventType,
    teacher = teacher.takeIf(String::isNotBlank),
    teacherId = teacherId,
    office = office.takeIf(String::isNotBlank),
    location = location.takeIf(String::isNotBlank),
    organization = organization.takeIf(String::isNotBlank),
    organizationId = organizationId,
    notes = notes.takeIf(String::isNotBlank),
    included = included,
)
