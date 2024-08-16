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

package ru.aleshin.studyassistant.core.data.mappers.tasks

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.database.models.tasks.HomeworkDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.MediatedHomework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.MediatedHomeworkPojo
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun HomeworkDetailsPojo.mapToDomain() = Homework(
    uid = uid,
    classId = classId,
    deadline = deadline.mapEpochTimeToInstant(),
    subject = subject?.mapToDomain(),
    organization = organization.mapToDomain(),
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentations,
    test = test,
    priority = TaskPriority.valueOf(priority),
    isDone = isDone,
    completeDate = completeDate?.mapEpochTimeToInstant(),
)

fun MediatedHomeworkPojo.mapToDomain() = MediatedHomework(
    uid = uid,
    subjectName = subjectName,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = TaskPriority.valueOf(priority),
)

fun HomeworkDetailsEntity.mapToDomain() = Homework(
    uid = uid,
    classId = classId,
    deadline = deadline.mapEpochTimeToInstant(),
    subject = subject?.mapToDomain(),
    organization = organization.mapToDomain(),
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentations,
    test = test,
    priority = TaskPriority.valueOf(priority),
    isDone = isDone,
    completeDate = completeDate?.mapEpochTimeToInstant(),
)

fun Homework.mapToRemoteData() = HomeworkPojo(
    uid = uid,
    classId = classId,
    deadline = deadline.toEpochMilliseconds(),
    subjectId = subject?.uid,
    organizationId = organization.uid,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentationTasks,
    test = test,
    priority = priority.name,
    done = isDone,
    completeDate = completeDate?.toEpochMilliseconds(),
)

fun MediatedHomework.mapToRemoteData() = MediatedHomeworkPojo(
    uid = uid,
    subjectName = subjectName,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = priority.name,
)

fun Homework.mapToLocalData() = HomeworkEntity(
    uid = uid,
    class_id = classId,
    deadline = deadline.toEpochMilliseconds(),
    subject_id = subject?.uid,
    organization_id = organization.uid,
    theoretical_tasks = theoreticalTasks,
    practical_tasks = practicalTasks,
    presentations = presentationTasks,
    test = test,
    priority = priority.name,
    is_done = if (isDone) 1L else 0L,
    complete_date = completeDate?.toEpochMilliseconds(),
)