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

package ru.aleshin.studyassistant.core.domain.entities.classes

import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
data class ClassDetails(
    val uid: UID,
    val scheduleId: UID,
    val organization: OrganizationShort,
    val eventType: EventType,
    val subject: Subject?,
    val customData: String? = null,
    val teacher: Employee?,
    val office: String,
    val location: ContactInfo?,
    val timeRange: TimeRange,
    val notification: Boolean = false,
    val homework: Homework? = null,
)

fun Class.convertToDetails(homework: Homework?) = ClassDetails(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization,
    eventType = eventType,
    subject = subject,
    customData = customData,
    teacher = teacher,
    office = office,
    location = location,
    timeRange = timeRange,
    homework = homework,
)