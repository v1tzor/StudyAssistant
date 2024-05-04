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

package mappers.tasks

import entities.subject.EventType
import entities.tasks.Homework
import entities.tasks.TaskPriority
import extensions.mapEpochTimeToInstant
import mappers.organizations.mapToData
import mappers.organizations.mapToDomain
import mappers.subjects.mapToData
import mappers.subjects.mapToDomain
import models.organizations.OrganizationShortData
import models.subjects.SubjectDetailsData
import models.tasks.HomeworkDetailsData
import models.tasks.HomeworkPojo
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun HomeworkDetailsData.mapToDomain() = Homework(
    uid = uid,
    classId = classId,
    date = date.mapEpochTimeToInstant(),
    subject = subject?.mapToDomain(),
    organization = organization.mapToDomain(),
    eventType = EventType.valueOf(eventType),
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = TaskPriority.valueOf(priority),
    isDone = isDone,
)

fun Homework.mapToData() = HomeworkDetailsData(
    uid = uid,
    classId = classId,
    date = date.toEpochMilliseconds(),
    subject = subject?.mapToData(),
    organization = organization.mapToData(),
    eventType = eventType.name,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority.name,
    isDone = isDone,
)

fun HomeworkDetailsData.mapToLocalData() = HomeworkEntity(
    uid = uid,
    class_id = classId,
    date = date,
    subject_id = subject?.uid,
    organization_id = organization.uid,
    event_type = eventType,
    theoretical_tasks = theoreticalTasks,
    practical_tasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority,
    is_done = if (isDone) 1L else 0L,
)

fun HomeworkEntity.mapToDetailsData(
    organization: OrganizationShortData,
    subject: SubjectDetailsData?,
) = HomeworkDetailsData(
    uid = uid,
    classId = class_id,
    date = date,
    subject = subject,
    organization = organization,
    eventType = event_type,
    theoreticalTasks = theoretical_tasks,
    practicalTasks = practical_tasks,
    presentations = presentations,
    test = test,
    priority = priority,
    isDone = is_done == 1L,
)

fun HomeworkDetailsData.mapToRemoteData() = HomeworkPojo(
    uid = uid,
    classId = classId,
    date = date,
    subjectId = subject?.uid,
    organizationId = organization.uid,
    eventType = eventType,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority,
    done = isDone,
)

fun HomeworkPojo.mapToDetailsData(
    organization: OrganizationShortData,
    subject: SubjectDetailsData?,
) = HomeworkDetailsData(
    uid = uid,
    classId = classId,
    date = date,
    subject = subject,
    organization = organization,
    eventType = eventType,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority,
    isDone = done,
)


