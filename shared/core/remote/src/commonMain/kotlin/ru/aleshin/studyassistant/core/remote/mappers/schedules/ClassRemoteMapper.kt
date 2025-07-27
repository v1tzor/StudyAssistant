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

package ru.aleshin.studyassistant.core.remote.mappers.schedules

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.classes.ClassDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.classes.ClassPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationShortPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun ClassDetailsPojo.mapToBase() = ClassPojo(
    uid = uid,
    organizationId = organization.uid,
    eventType = eventType,
    subjectId = subject?.uid,
    customData = customData,
    teacherId = teacher?.id,
    office = office,
    location = location?.toJson(),
    startTime = startTime,
    endTime = endTime,
)

fun ClassPojo.mapToDetails(
    scheduleId: UID,
    organization: OrganizationShortPojo,
    subject: SubjectDetailsPojo?,
    employee: EmployeePojo?,
) = ClassDetailsPojo(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization,
    eventType = eventType,
    subject = subject,
    customData = customData,
    teacher = employee,
    office = office,
    location = location?.fromJson(),
    startTime = startTime,
    endTime = endTime,
)