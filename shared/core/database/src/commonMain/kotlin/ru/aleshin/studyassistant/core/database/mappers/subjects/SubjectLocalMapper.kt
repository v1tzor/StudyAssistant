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

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectEntity

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
fun SubjectDetailsEntity.mapToBase() = SubjectEntity(
    uid = uid,
    organization_id = organizationId,
    event_type = eventType,
    name = name,
    teacher_id = teacher?.uid,
    office = office,
    color = color.toLong(),
    location = location?.toJson(),
)

fun SubjectEntity.mapToDetails(
    employee: EmployeeEntity?,
) = SubjectDetailsEntity(
    uid = uid,
    organizationId = organization_id,
    eventType = event_type,
    name = name,
    teacher = employee?.mapToBase(),
    office = office,
    color = color.toInt(),
    location = location?.fromJson(),
)