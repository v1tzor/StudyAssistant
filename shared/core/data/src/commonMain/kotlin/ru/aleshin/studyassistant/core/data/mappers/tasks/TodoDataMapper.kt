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
import ru.aleshin.studyassistant.core.database.models.tasks.BaseTodoEntity
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoNotifications

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
fun Todo.mapToLocalData() = BaseTodoEntity(
    uid = uid,
    deadline = deadline?.toEpochMilliseconds(),
    name = name,
    description = description,
    notifyBeforeStart = if (notifications.beforeStart) 1L else 0L,
    notifyFifteenMinutesBefore = if (notifications.fifteenMinutesBefore) 1L else 0L,
    notifyOneHourBefore = if (notifications.oneHourBefore) 1L else 0L,
    notifyThreeHourBefore = if (notifications.threeHourBefore) 1L else 0L,
    notifyOneDayBefore = if (notifications.oneDayBefore) 1L else 0L,
    notifyOneWeekBefore = if (notifications.oneWeekBefore) 1L else 0L,
    priority = priority.name,
    isDone = if (isDone) 1L else 0L,
    createdAt = createdAt.toEpochMilliseconds(),
    completeDate = completeDate?.toEpochMilliseconds(),
    updatedAt = updatedAt,
)

fun BaseTodoEntity.mapToDomain() = Todo(
    uid = uid,
    deadline = deadline?.mapEpochTimeToInstant(),
    name = name,
    description = description,
    priority = TaskPriority.valueOf(priority),
    notifications = TodoNotifications(
        beforeStart = notifyBeforeStart == 1L,
        fifteenMinutesBefore = notifyFifteenMinutesBefore == 1L,
        oneHourBefore = notifyOneHourBefore == 1L,
        threeHourBefore = notifyThreeHourBefore == 1L,
        oneDayBefore = notifyOneDayBefore == 1L,
        oneWeekBefore = notifyOneWeekBefore == 1L,
    ),
    isDone = isDone == 1L,
    createdAt = createdAt.mapEpochTimeToInstant(),
    completeDate = completeDate?.mapEpochTimeToInstant(),
    updatedAt = updatedAt,
)
