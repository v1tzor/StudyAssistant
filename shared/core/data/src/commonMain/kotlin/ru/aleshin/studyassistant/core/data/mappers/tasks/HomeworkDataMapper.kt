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

package ru.aleshin.studyassistant.core.data.mappers.tasks

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.database.models.tasks.BaseHomeworkEntity
import ru.aleshin.studyassistant.core.database.models.tasks.HomeworkDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun Homework.mapToLocalData() = BaseHomeworkEntity(
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
    isDone = if (isDone) 1L else 0L,
    completeDate = completeDate?.toEpochMilliseconds(),
    updatedAt = updatedAt,
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
    updatedAt = updatedAt,
)
