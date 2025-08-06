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

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.utils.sync.MultipleSyncMapper
import ru.aleshin.studyassistant.core.database.models.tasks.BaseTodoEntity
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoNotifications
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoNotificationsPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojoDetails

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */

// Remote

fun Todo.mapToRemoteData(userId: UID) = TodoPojo(
    id = uid,
    userId = userId,
    deadline = deadline?.toEpochMilliseconds(),
    name = name,
    description = description,
    priority = priority.name,
    notifications = notifications.mapToRemote().toJson(),
    done = isDone,
    completeDate = completeDate?.toEpochMilliseconds(),
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt,
)

fun TodoNotifications.mapToRemote() = TodoNotificationsPojo(
    beforeStart = beforeStart,
    fifteenMinutesBefore = fifteenMinutesBefore,
    oneHourBefore = oneHourBefore,
    threeHourBefore = threeHourBefore,
    oneDayBefore = oneDayBefore,
    oneWeekBefore = oneWeekBefore,
)

fun TodoPojoDetails.mapToDomain() = Todo(
    uid = uid,
    deadline = deadline?.mapEpochTimeToInstant(),
    name = name,
    description = description,
    priority = TaskPriority.valueOf(priority),
    notifications = notifications.mapToDomain(),
    isDone = done,
    completeDate = completeDate?.mapEpochTimeToInstant(),
    createdAt = createdAt.mapEpochTimeToInstant(),
    updatedAt = updatedAt,
)

fun TodoNotificationsPojo.mapToDomain() = TodoNotifications(
    beforeStart = beforeStart,
    fifteenMinutesBefore = fifteenMinutesBefore,
    oneHourBefore = oneHourBefore,
    threeHourBefore = threeHourBefore,
    oneDayBefore = oneDayBefore,
    oneWeekBefore = oneWeekBefore,
)

// Local

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
    isCacheData = 0L,
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

// Combined

fun BaseTodoEntity.convertToRemote(userId: UID) = TodoPojo(
    id = uid,
    deadline = deadline,
    userId = userId,
    name = name,
    description = description,
    priority = priority,
    notifications = TodoNotificationsPojo(
        beforeStart = notifyBeforeStart == 1L,
        fifteenMinutesBefore = notifyFifteenMinutesBefore == 1L,
        oneHourBefore = notifyOneHourBefore == 1L,
        threeHourBefore = notifyThreeHourBefore == 1L,
        oneDayBefore = notifyOneDayBefore == 1L,
        oneWeekBefore = notifyOneWeekBefore == 1L,
    ).toJson(),
    done = isDone == 1L,
    createdAt = createdAt,
    completeDate = completeDate,
    updatedAt = updatedAt,
)

fun TodoPojo.convertToLocal(): BaseTodoEntity {
    val notifications = notifications.fromJson<TodoNotificationsPojo>()
    return BaseTodoEntity(
        uid = id,
        deadline = deadline,
        name = name,
        description = description,
        notifyBeforeStart = if (notifications.beforeStart) 1L else 0L,
        notifyFifteenMinutesBefore = if (notifications.fifteenMinutesBefore) 1L else 0L,
        notifyOneHourBefore = if (notifications.oneHourBefore) 1L else 0L,
        notifyThreeHourBefore = if (notifications.threeHourBefore) 1L else 0L,
        notifyOneDayBefore = if (notifications.oneDayBefore) 1L else 0L,
        notifyOneWeekBefore = if (notifications.oneWeekBefore) 1L else 0L,
        priority = priority,
        isDone = if (done) 1L else 0L,
        completeDate = completeDate,
        updatedAt = updatedAt,
        createdAt = createdAt,
        isCacheData = 1L,
    )
}

class TodoSyncMapper : MultipleSyncMapper<BaseTodoEntity, TodoPojo>(
    localToRemote = { userId -> convertToRemote(userId) },
    remoteToLocal = { convertToLocal() },
)