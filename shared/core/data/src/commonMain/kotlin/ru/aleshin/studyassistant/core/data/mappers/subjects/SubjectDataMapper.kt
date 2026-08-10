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

package ru.aleshin.studyassistant.core.data.mappers.subjects

import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.database.models.subjects.BaseSubjectEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
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
