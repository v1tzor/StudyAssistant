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

package ru.aleshin.studyassistant.core.remote.mappers.subjects

import ru.aleshin.studyassistant.core.common.extensions.tryFromJson
import ru.aleshin.studyassistant.core.common.extensions.tryToJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun SubjectDetailsPojo.mapToBase(userId: UID) = SubjectPojo(
    id = uid,
    userId = userId,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacherId = teacher?.id,
    office = office,
    color = color,
    location = location?.tryToJson(),
    updatedAt = updatedAt,
)

fun SubjectPojo.mapToDetails(
    employee: EmployeePojo?,
) = SubjectDetailsPojo(
    uid = id,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacher = employee,
    office = office,
    color = color,
    location = location?.tryFromJson(),
    updatedAt = updatedAt,
)