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

package ru.aleshin.studyassistant.core.database.mappers.goals

import ru.aleshin.studyassistant.core.database.models.goals.BaseGoalEntity
import ru.aleshin.studyassistant.core.database.models.goals.GoalEntityDetails
import ru.aleshin.studyassistant.core.database.models.tasks.BaseTodoEntity
import ru.aleshin.studyassistant.core.database.models.tasks.HomeworkDetailsEntity
import ru.aleshin.studyassistant.sqldelight.goals.GoalEntity

/**
 * @author Stanislav Aleshin on 26.04.2025.
 */
fun GoalEntity.mapToBase() = BaseGoalEntity(
    uid = uid,
    type = type,
    number = number,
    contentId = content_id,
    contentOrganizationId = content_organization_id,
    contentDeadline = content_deadline,
    targetDate = target_date,
    desiredTime = desired_time,
    goalTimeType = goal_time_type,
    targetTime = target_time,
    pastStopTime = past_stop_time,
    startTimePoint = start_time_point,
    isActive = is_active,
    completeAfterTimeElapsed = complete_after_time_elapsed,
    isDone = is_done,
    completeDate = complete_date,
    updatedAt = updated_at,
    isCacheData = is_cache_data,
)

fun BaseGoalEntity.mapToEntity() = GoalEntity(
    uid = uid,
    type = type,
    number = number,
    content_id = contentId,
    content_organization_id = contentOrganizationId,
    content_deadline = contentDeadline,
    target_date = targetDate,
    desired_time = desiredTime,
    goal_time_type = goalTimeType,
    target_time = targetTime,
    past_stop_time = pastStopTime,
    start_time_point = startTimePoint,
    is_active = isActive,
    complete_after_time_elapsed = completeAfterTimeElapsed,
    is_done = isDone,
    complete_date = completeDate,
    updated_at = updatedAt,
    is_cache_data = isCacheData,
)

fun BaseGoalEntity.mapToDetails(
    homeworksMapper: (() -> HomeworkDetailsEntity?)? = null,
    todoMapper: (() -> BaseTodoEntity?)? = null,
) = GoalEntityDetails(
    uid = uid,
    contentType = type,
    contentHomework = homeworksMapper?.invoke(),
    contentTodo = todoMapper?.invoke(),
    number = number.toInt(),
    targetDate = targetDate,
    desiredTime = desiredTime,
    goalTimeType = goalTimeType,
    targetTime = targetTime,
    pastStopTime = pastStopTime,
    isActive = isActive != 0L,
    startTimePoint = startTimePoint,
    completeAfterTimeElapsed = completeAfterTimeElapsed != 0L,
    isDone = isDone != 0L,
    completeDate = completeDate,
    updatedAt = updatedAt,
)