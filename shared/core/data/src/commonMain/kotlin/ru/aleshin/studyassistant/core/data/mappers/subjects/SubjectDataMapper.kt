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

package ru.aleshin.studyassistant.core.data.mappers.subjects

import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToLocal
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.MediatedSubject
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.remote.models.subjects.MediatedSubjectPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectEntity

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun SubjectDetailsPojo.mapToDomain() = Subject(
    uid = uid,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    name = name,
    teacher = teacher?.mapToDomain(),
    office = office,
    color = color,
    location = location?.mapToDomain(),
)

fun MediatedSubjectPojo.mapToDomain() = MediatedSubject(
    uid = uid,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    name = name,
    teacherId = teacherId,
    office = office,
    color = color,
    location = location?.mapToDomain(),
)

fun SubjectDetailsEntity.mapToDomain() = Subject(
    uid = uid,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    name = name,
    teacher = teacher?.mapToLocal()?.mapToDomain(),
    office = office,
    color = color,
    location = location?.mapToDomain(),
)

fun Subject.mapToRemoteData(userId: UID) = SubjectPojo(
    uid = uid,
    userId = userId,
    organizationId = organizationId,
    eventType = eventType.name,
    name = name,
    teacherId = teacher?.uid,
    office = office,
    color = color,
    location = location?.mapToRemoteData()?.toJson(),
)

fun MediatedSubject.mapToRemoteData() = MediatedSubjectPojo(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType.name,
    name = name,
    teacherId = teacherId,
    office = office,
    color = color,
    location = location?.mapToRemoteData(),
)

fun Subject.mapToLocalData() = SubjectEntity(
    uid = uid,
    organization_id = organizationId,
    event_type = eventType.name,
    name = name,
    teacher_id = teacher?.uid,
    office = office,
    color = color.toLong(),
    location = location?.mapToLocalData()?.toJson(),
)