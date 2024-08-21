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

package ru.aleshin.studyassistant.core.database.mappers.schedules

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.models.classes.ClassDetailsEntity
import ru.aleshin.studyassistant.core.database.models.classes.ClassEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun ClassDetailsEntity.mapToBase() = ClassEntity(
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
)

fun ClassEntity.mapToDetails(
    scheduleId: UID,
    organization: OrganizationShortEntity,
    subject: SubjectDetailsEntity?,
    employee: EmployeeEntity?,
) = ClassDetailsEntity(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization,
    eventType = eventType,
    subject = subject,
    customData = customData,
    teacher = employee?.mapToBase(),
    office = office,
    location = location,
    startTime = startTime,
    endTime = endTime,
)