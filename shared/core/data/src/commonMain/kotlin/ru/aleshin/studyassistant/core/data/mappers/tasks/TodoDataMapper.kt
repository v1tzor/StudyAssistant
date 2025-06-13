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
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoNotifications
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoNotificationsPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojo
import ru.aleshin.studyassistant.sqldelight.tasks.TodoEntity

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
fun TodoPojo.mapToDomain() = Todo(
    uid = uid,
    deadline = deadline?.mapEpochTimeToInstant(),
    name = name,
    description = description,
    priority = TaskPriority.valueOf(priority),
    notifications = notifications.mapToDomain(),
    isDone = done,
    completeDate = completeDate?.mapEpochTimeToInstant(),
)

fun TodoNotificationsPojo.mapToDomain() = TodoNotifications(
    beforeStart = beforeStart,
    fifteenMinutesBefore = fifteenMinutesBefore,
    oneHourBefore = oneHourBefore,
    threeHourBefore = threeHourBefore,
    oneDayBefore = oneDayBefore,
    oneWeekBefore = oneWeekBefore,
)

fun TodoEntity.mapToDomain() = Todo(
    uid = uid,
    deadline = deadline?.mapEpochTimeToInstant(),
    name = name,
    description = description,
    priority = TaskPriority.valueOf(priority),
    notifications = TodoNotifications(
        beforeStart = notify_before_start == 1L,
        fifteenMinutesBefore = notify_fifteen_minutes_before == 1L,
        oneHourBefore = notify_one_hour_before == 1L,
        threeHourBefore = notify_three_hour_before == 1L,
        oneDayBefore = notify_one_day_before == 1L,
        oneWeekBefore = notify_one_week_before == 1L,
    ),
    isDone = is_done == 1L,
    completeDate = complete_date?.mapEpochTimeToInstant(),
)

fun Todo.mapToRemoteData() = TodoPojo(
    uid = uid,
    deadline = deadline?.toEpochMilliseconds(),
    name = name,
    description = description,
    priority = priority.name,
    notifications = notifications.mapToDomain(),
    done = isDone,
    completeDate = completeDate?.toEpochMilliseconds(),
)

fun TodoNotifications.mapToDomain() = TodoNotificationsPojo(
    beforeStart = beforeStart,
    fifteenMinutesBefore = fifteenMinutesBefore,
    oneHourBefore = oneHourBefore,
    threeHourBefore = threeHourBefore,
    oneDayBefore = oneDayBefore,
    oneWeekBefore = oneWeekBefore,
)

fun Todo.mapToLocalData() = TodoEntity(
    uid = uid,
    deadline = deadline?.toEpochMilliseconds(),
    name = name,
    description = description,
    notify_before_start = if (notifications.beforeStart) 1L else 0L,
    notify_fifteen_minutes_before = if (notifications.fifteenMinutesBefore) 1L else 0L,
    notify_one_hour_before = if (notifications.oneHourBefore) 1L else 0L,
    notify_three_hour_before = if (notifications.threeHourBefore) 1L else 0L,
    notify_one_day_before = if (notifications.oneDayBefore) 1L else 0L,
    notify_one_week_before = if (notifications.oneWeekBefore) 1L else 0L,
    priority = priority.toString(),
    is_done = if (isDone) 1L else 0L,
    complete_date = completeDate?.toEpochMilliseconds(),
)