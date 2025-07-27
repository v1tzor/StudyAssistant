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
import ru.aleshin.studyassistant.core.data.utils.sync.MultipleSyncMapper
import ru.aleshin.studyassistant.core.database.models.subjects.BaseSubjectEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.MediatedSubject
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.remote.models.subjects.MediatedSubjectPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */

// Remote

fun Subject.mapToRemoteData(userId: UID) = SubjectPojo(
    id = uid,
    userId = userId,
    organizationId = organizationId,
    eventType = eventType.name,
    name = name,
    teacherId = teacher?.uid,
    office = office,
    color = color,
    location = location?.mapToRemoteData()?.toJson(),
    updatedAt = updatedAt,
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

fun SubjectDetailsPojo.mapToDomain() = Subject(
    uid = uid,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    name = name,
    teacher = teacher?.mapToDomain(),
    office = office,
    color = color,
    location = location?.mapToDomain(),
    updatedAt = updatedAt,
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

// Local

fun Subject.mapToLocalData() = BaseSubjectEntity(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType.name,
    name = name,
    teacherId = teacher?.uid,
    office = office,
    color = color.toLong(),
    location = location?.mapToLocalData()?.toJson(),
    updatedAt = updatedAt,
    isCacheData = 0L,
)

fun SubjectDetailsEntity.mapToDomain() = Subject(
    uid = uid,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    name = name,
    teacher = teacher?.mapToDomain(),
    office = office,
    color = color,
    location = location?.mapToDomain(),
    updatedAt = updatedAt,
)

// Combined

fun BaseSubjectEntity.convertToRemote(userId: String) = SubjectPojo(
    id = uid,
    userId = userId,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacherId = teacherId,
    office = office,
    color = color.toInt(),
    location = location,
    updatedAt = updatedAt,
)

fun SubjectPojo.convertToLocal() = BaseSubjectEntity(
    uid = id,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacherId = teacherId,
    office = office,
    color = color.toLong(),
    location = location,
    updatedAt = updatedAt,
    isCacheData = 1L,
)

class SubjectSyncMapper : MultipleSyncMapper<BaseSubjectEntity, SubjectPojo>(
    localToRemote = { userId -> convertToRemote(userId) },
    remoteToLocal = { convertToLocal() },
)