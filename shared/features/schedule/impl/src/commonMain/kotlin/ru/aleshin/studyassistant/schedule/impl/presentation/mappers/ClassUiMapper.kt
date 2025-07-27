/*
 * Copyright 2024 Stanislav Aleshin
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

import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.classes.ClassDetails
import ru.aleshin.studyassistant.core.domain.entities.classes.MediatedClass
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.MediatedClassUi

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal fun Class.mapToUi(number: Int) = ClassUi(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapToUi(),
    eventType = eventType,
    subject = subject?.mapToUi(),
    customData = customData,
    teacher = teacher?.mapToUi(),
    office = office,
    location = location?.mapToUi(),
    timeRange = timeRange,
    number = number,
)


internal inline fun ClassDetails.mapToUi(
    number: Int,
    homeworkStatus: (Homework) -> HomeworkStatus,
) = ClassDetailsUi(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapToUi(),
    eventType = eventType,
    subject = subject?.mapToUi(),
    customData = customData,
    teacher = teacher?.mapToUi(),
    office = office,
    location = location?.mapToUi(),
    timeRange = timeRange,
    number = number,
    homework = homework?.let { it.mapToUi(status = homeworkStatus(it)) },
)

internal fun MediatedClass.mapToUi() = MediatedClassUi(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organizationId,
    eventType = eventType,
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapToUi(),
    timeRange = timeRange,
)

internal fun ClassUi.mapToDomain() = Class(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapToDomain(),
    eventType = eventType,
    subject = subject?.mapToDomain(),
    customData = customData,
    teacher = teacher?.mapToDomain(),
    office = office,
    location = location?.mapToDomain(),
    timeRange = timeRange,
)

internal fun MediatedClassUi.mapToDomain() = MediatedClass(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organizationId,
    eventType = eventType,
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapToDomain(),
    timeRange = timeRange,
)