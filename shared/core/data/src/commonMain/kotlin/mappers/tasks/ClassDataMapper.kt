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

package mappers.tasks

import entities.classes.Class
import entities.subject.EventType
import extensions.mapEpochTimeToInstant
import functional.TimeRange
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mappers.mapToData
import mappers.mapToDomain
import mappers.organizations.mapToData
import mappers.organizations.mapToDomain
import mappers.subjects.mapToData
import mappers.subjects.mapToDomain
import mappers.users.mapToData
import mappers.users.mapToDomain
import models.classes.ClassDetailsData
import models.classes.ClassPojo
import models.organizations.OrganizationShortData
import models.subjects.SubjectDetailsData
import models.users.EmployeeDetailsData
import ru.aleshin.studyassistant.sqldelight.`class`.ClassEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun ClassDetailsData.mapToDomain() = Class(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapToDomain(),
    eventType = EventType.valueOf(eventType),
    subject = subject?.mapToDomain(),
    customData = customData,
    teacher = teacher?.mapToDomain(),
    office = office,
    location = location.mapToDomain(),
    timeRange = TimeRange(startTime.mapEpochTimeToInstant(), endTime.mapEpochTimeToInstant()),
    notification = notification,
)

fun Class.mapToData() = ClassDetailsData(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapToData(),
    eventType = eventType.name,
    subject = subject?.mapToData(),
    customData = customData,
    teacher = teacher?.mapToData(),
    office = office,
    location = location.mapToData(),
    startTime = timeRange.from.toEpochMilliseconds(),
    endTime = timeRange.to.toEpochMilliseconds(),
    notification = notification,
)

fun ClassDetailsData.mapToLocalDate() = ClassEntity(
    uid = uid,
    schedule_id = scheduleId,
    organization_id = organization.uid,
    event_type = eventType,
    subject_id = subject?.uid,
    custom_data = customData,
    teacher_id = teacher?.uid,
    office = office.toLong(),
    location = Json.encodeToString(location),
    start_time = startTime,
    end_time = endTime,
    notification = if (notification) 1L else 0L,
)

fun ClassEntity.mapToDetailsDate(
    organization: OrganizationShortData,
    subject: SubjectDetailsData?,
    employee: EmployeeDetailsData?,
) = ClassDetailsData(
    uid = uid,
    scheduleId = schedule_id,
    organization = organization,
    eventType = event_type,
    subject = subject,
    customData = custom_data,
    teacher = employee,
    office = office.toInt(),
    location = Json.decodeFromString(location),
    startTime = start_time,
    endTime = end_time,
    notification = notification == 1L,
)

fun ClassDetailsData.mapToRemoteDate() = ClassPojo(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organization.uid,
    eventType = eventType,
    subjectId = subject?.uid,
    customData = customData,
    teacherId = teacher?.uid,
    office = office,
    location = location,
    startTime = startTime,
    endTime = endTime,
    notification = notification,
)

fun ClassPojo.mapToDetailsDate(
    organization: OrganizationShortData,
    subject: SubjectDetailsData?,
    employee: EmployeeDetailsData?,
) = ClassDetailsData(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization,
    eventType = eventType,
    subject = subject,
    customData = customData,
    teacher = employee,
    office = office,
    location = location,
    startTime = startTime,
    endTime = endTime,
    notification = notification,
)
