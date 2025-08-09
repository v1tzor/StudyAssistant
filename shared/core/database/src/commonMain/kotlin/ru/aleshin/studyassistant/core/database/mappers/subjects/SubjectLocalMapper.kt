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

package ru.aleshin.studyassistant.core.database.mappers.subjects

import ru.aleshin.studyassistant.core.common.extensions.tryFromJson
import ru.aleshin.studyassistant.core.common.extensions.tryToJson
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.core.database.models.subjects.BaseSubjectEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectEntity

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
fun SubjectEntity.mapToBase() = BaseSubjectEntity(
    uid = uid,
    organizationId = organization_id,
    eventType = event_type,
    name = name,
    teacherId = teacher_id,
    office = office,
    color = color,
    location = location,
    updatedAt = updated_at,
    isCacheData = is_cache_data,
)

fun BaseSubjectEntity.mapToEntity() = SubjectEntity(
    uid = uid,
    organization_id = organizationId,
    event_type = eventType,
    name = name,
    teacher_id = teacherId,
    office = office,
    color = color,
    location = location,
    updated_at = updatedAt,
    is_cache_data = isCacheData,
)

fun SubjectDetailsEntity.mapToBase() = BaseSubjectEntity(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacherId = teacher?.uid,
    office = office,
    color = color.toLong(),
    location = location?.tryToJson<ContactInfoEntity>(),
    updatedAt = updatedAt,
    isCacheData = 0L,
)

fun BaseSubjectEntity.mapToDetails(
    employee: BaseEmployeeEntity?,
) = SubjectDetailsEntity(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacher = employee,
    office = office,
    color = color.toInt(),
    location = location?.tryFromJson<ContactInfoEntity>(),
    updatedAt = updatedAt,
)