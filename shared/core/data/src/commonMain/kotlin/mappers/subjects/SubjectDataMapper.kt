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

package mappers.subjects

import entities.subject.EventType
import entities.subject.Subject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mappers.mapToData
import mappers.mapToDomain
import mappers.users.mapToData
import mappers.users.mapToDomain
import models.subjects.SubjectDetailsData
import models.subjects.SubjectPojo
import models.users.EmployeeDetailsData
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectEntity

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun SubjectDetailsData.mapToDomain() = Subject(
    uid = uid,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    name = name,
    teacher = teacher?.mapToDomain(),
    office = office,
    color = color,
    location = location.mapToDomain(),
)

fun Subject.mapToData() = SubjectDetailsData(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType.name,
    name = name,
    teacher = teacher?.mapToData(),
    office = office,
    color = color,
    location = location.mapToData(),
)

fun SubjectDetailsData.mapToLocalData() = SubjectEntity(
    uid = uid,
    organization_id = organizationId,
    event_type = eventType,
    name = name,
    teacher_id = teacher?.uid,
    office = office.toLong(),
    color = color.toLong(),
    location = Json.encodeToString(location),
)

fun SubjectEntity.mapToDetailsData(
    employee: EmployeeDetailsData?,
) = SubjectDetailsData(
    uid = uid,
    organizationId = organization_id,
    eventType = event_type,
    name = name,
    teacher = employee,
    office = office.toInt(),
    color = color.toInt(),
    location = Json.decodeFromString(location),
)

fun SubjectDetailsData.mapToRemoteData() = SubjectPojo(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacher = teacher?.uid,
    office = office,
    color = color,
    location = location,
)

fun SubjectPojo.mapToDetailsData(
    employee: EmployeeDetailsData?,
) = SubjectDetailsData(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacher = employee,
    office = office,
    color = color,
    location = location,
)