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
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.data.utils.sync.MultipleSyncMapper
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.MediatedHomeworkEntity
import ru.aleshin.studyassistant.core.database.models.tasks.BaseHomeworkEntity
import ru.aleshin.studyassistant.core.database.models.tasks.HomeworkDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.MediatedHomework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.MediatedHomeworkPojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */

// Remote

fun Homework.mapToRemoteData(userId: UID) = HomeworkPojo(
    id = uid,
    userId = userId,
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
    updatedAt = updatedAt,
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
    updatedAt = updatedAt,
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

// Local

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
    isCacheData = 0L,
)

fun MediatedHomework.mapToLocalData() = MediatedHomeworkEntity(
    uid = uid,
    subjectName = subjectName,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = priority.name,
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

fun MediatedHomeworkEntity.mapToDomain() = MediatedHomework(
    uid = uid,
    subjectName = subjectName,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = TaskPriority.valueOf(priority),
)

// Combined

fun HomeworkPojo.convertToLocal() = BaseHomeworkEntity(
    uid = id,
    classId = classId,
    deadline = deadline,
    subjectId = subjectId,
    organizationId = organizationId,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority,
    isDone = if (done) 1L else 0L,
    completeDate = completeDate,
    updatedAt = updatedAt,
    isCacheData = 1L,
)

fun BaseHomeworkEntity.convertToRemote(userId: UID) = HomeworkPojo(
    id = uid,
    userId = userId,
    classId = classId,
    deadline = deadline,
    subjectId = subjectId,
    organizationId = organizationId,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority,
    done = isDone == 1L,
    completeDate = completeDate,
)

class HomeworkSyncMapper : MultipleSyncMapper<BaseHomeworkEntity, HomeworkPojo>(
    localToRemote = { userId -> convertToRemote(userId) },
    remoteToLocal = { convertToLocal() },
)