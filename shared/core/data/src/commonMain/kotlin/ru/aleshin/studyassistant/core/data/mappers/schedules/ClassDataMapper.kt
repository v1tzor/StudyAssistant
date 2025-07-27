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

package ru.aleshin.studyassistant.core.data.mappers.schedules

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.database.models.classes.ClassDetailsEntity
import ru.aleshin.studyassistant.core.database.models.classes.ClassEntity
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.classes.MediatedClass
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.remote.models.classes.ClassDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.classes.ClassPojo
import ru.aleshin.studyassistant.core.remote.models.classes.MediatedClassPojo
import ru.aleshin.studyassistant.core.remote.models.users.ContactInfoPojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun ClassDetailsEntity.mapToDomain() = Class(
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
)

fun ClassDetailsPojo.mapToDomain() = Class(
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
)

fun MediatedClassPojo.mapToDomain() = MediatedClass(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapToDomain(),
    timeRange = TimeRange(startTime.mapEpochTimeToInstant(), endTime.mapEpochTimeToInstant()),
)

fun Class.mapToRemoteData() = ClassPojo(
    uid = uid,
    organizationId = organization.uid,
    eventType = eventType.toString(),
    subjectId = subject?.uid,
    customData = customData,
    teacherId = teacher?.uid,
    office = office,
    location = location?.mapToRemoteData()?.toJson(),
    startTime = timeRange.from.toEpochMilliseconds(),
    endTime = timeRange.to.toEpochMilliseconds(),
)

fun ClassPojo.mapToLocal() = ClassEntity(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.fromJson<ContactInfoPojo>()?.mapToDomain()?.mapToLocalData(),
    startTime = startTime,
    endTime = endTime,
)

fun ClassEntity.mapToRemote() = ClassPojo(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapToDomain()?.mapToRemoteData()?.toJson(),
    startTime = startTime,
    endTime = endTime,
)

fun MediatedClass.mapToRemoteData() = MediatedClassPojo(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organizationId,
    eventType = eventType.toString(),
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapToRemoteData(),
    startTime = timeRange.from.toEpochMilliseconds(),
    endTime = timeRange.to.toEpochMilliseconds(),
)

fun Class.mapToLocalData() = ClassEntity(
    uid = uid,
    organizationId = organization.uid,
    eventType = eventType.toString(),
    subjectId = subject?.uid,
    customData = customData,
    teacherId = teacher?.uid,
    office = office,
    location = location?.mapToLocalData(),
    startTime = timeRange.from.toEpochMilliseconds(),
    endTime = timeRange.to.toEpochMilliseconds(),
)