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

package mappers.schedules

import entities.classes.Class
import entities.subject.EventType
import extensions.mapEpochTimeToInstant
import functional.TimeRange
import functional.UID
import mappers.mapToData
import mappers.mapToDomain
import mappers.organizations.mapToData
import mappers.organizations.mapToDomain
import mappers.subjects.mapToData
import mappers.subjects.mapToDomain
import mappers.users.mapToData
import mappers.users.mapToDomain
import models.classes.ClassData
import models.classes.ClassDetailsData
import models.organizations.OrganizationShortData
import models.subjects.SubjectDetailsData
import models.users.EmployeeDetailsData

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
    location = location?.mapToDomain(),
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
    location = location?.mapToData(),
    startTime = timeRange.from.toEpochMilliseconds(),
    endTime = timeRange.to.toEpochMilliseconds(),
    notification = notification,
)

fun ClassDetailsData.mapToData() = ClassData(
    uid = uid,
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

fun ClassData.mapToDetailsData(
    scheduleId: UID,
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