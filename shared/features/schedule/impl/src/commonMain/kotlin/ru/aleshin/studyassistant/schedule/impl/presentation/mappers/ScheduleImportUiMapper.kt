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

import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToUi
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToUi
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportClass
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportSession
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportSessionUi

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
internal fun ScheduleImportSession.mapToUi() = ScheduleImportSessionUi(
    title = title.orEmpty(),
    organizationId = organizationId,
    classes = classes.map { classModel -> classModel.mapToUi() },
    subjects = subjects.map { subject -> subject.mapToUi() },
    employees = employees.map { employee -> employee.mapToUi() },
    originalSubjectIds = originalSubjectIds,
    originalEmployeeIds = originalEmployeeIds,
    dirtySubjectIds = dirtySubjectIds,
    dirtyEmployeeIds = dirtyEmployeeIds,
    unparsedLines = unparsedLines,
)

internal fun ScheduleImportSessionUi.mapToDomain() = ScheduleImportSession(
    title = title.takeIf(String::isNotBlank),
    organizationId = organizationId,
    classes = classes.map { classModel -> classModel.mapToDomain() },
    subjects = subjects.map { subject -> subject.mapToDomain() },
    employees = employees.map { employee -> employee.mapToDomain() },
    originalSubjectIds = originalSubjectIds,
    originalEmployeeIds = originalEmployeeIds,
    dirtySubjectIds = dirtySubjectIds,
    dirtyEmployeeIds = dirtyEmployeeIds,
    unparsedLines = unparsedLines,
)

internal fun ScheduleImportClass.mapToUi() = ScheduleImportClassUi(
    uid = uid,
    repeatWeek = repeatWeek,
    dayOfWeek = dayOfWeek,
    number = number,
    startTime = startTime,
    endTime = endTime,
    subjectId = subjectId,
    teacherId = teacherId,
    office = office,
    location = location,
    eventType = eventType,
    included = included,
)

internal fun ScheduleImportClassUi.mapToDomain() = ScheduleImportClass(
    uid = uid,
    repeatWeek = repeatWeek,
    dayOfWeek = dayOfWeek,
    number = number,
    startTime = startTime,
    endTime = endTime,
    subjectId = subjectId,
    teacherId = teacherId,
    office = office,
    location = location,
    eventType = eventType,
    included = included,
)
