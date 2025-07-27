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

package ru.aleshin.studyassistant.core.database.mappers.tasks

import ru.aleshin.studyassistant.core.database.models.tasks.BaseTodoEntity
import ru.aleshin.studyassistant.sqldelight.tasks.FetchCompletedTodos
import ru.aleshin.studyassistant.sqldelight.tasks.FetchCompletedTodosByTimeRange
import ru.aleshin.studyassistant.sqldelight.tasks.TodoEntity

/**
 * @author Stanislav Aleshin on 12.06.2025.
 */
internal fun FetchCompletedTodos.mapToBase() = BaseTodoEntity(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifyBeforeStart = notify_before_start,
    notifyFifteenMinutesBefore = notify_fifteen_minutes_before,
    notifyOneHourBefore = notify_one_hour_before,
    notifyThreeHourBefore = notify_three_hour_before,
    notifyOneDayBefore = notify_one_day_before,
    notifyOneWeekBefore = notify_one_week_before,
    isDone = is_done,
    completeDate = complete_date,
    updatedAt = updated_at,
    isCacheData = is_cache_data,
)

internal fun FetchCompletedTodosByTimeRange.mapToBase() = BaseTodoEntity(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifyBeforeStart = notify_before_start,
    notifyFifteenMinutesBefore = notify_fifteen_minutes_before,
    notifyOneHourBefore = notify_one_hour_before,
    notifyThreeHourBefore = notify_three_hour_before,
    notifyOneDayBefore = notify_one_day_before,
    notifyOneWeekBefore = notify_one_week_before,
    isDone = is_done,
    completeDate = complete_date,
    updatedAt = updated_at,
    isCacheData = is_cache_data,
)

internal fun TodoEntity.mapToBase() = BaseTodoEntity(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifyBeforeStart = notify_before_start,
    notifyFifteenMinutesBefore = notify_fifteen_minutes_before,
    notifyOneHourBefore = notify_one_hour_before,
    notifyThreeHourBefore = notify_three_hour_before,
    notifyOneDayBefore = notify_one_day_before,
    notifyOneWeekBefore = notify_one_week_before,
    isDone = is_done,
    completeDate = complete_date,
    updatedAt = updated_at,
    isCacheData = is_cache_data,
)

internal fun BaseTodoEntity.mapToEntity() = TodoEntity(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notify_before_start = notifyBeforeStart,
    notify_fifteen_minutes_before = notifyFifteenMinutesBefore,
    notify_one_hour_before = notifyOneHourBefore,
    notify_three_hour_before = notifyThreeHourBefore,
    notify_one_day_before = notifyOneDayBefore,
    notify_one_week_before = notifyOneWeekBefore,
    is_done = isDone,
    complete_date = completeDate,
    updated_at = updatedAt,
    is_cache_data = isCacheData,
)