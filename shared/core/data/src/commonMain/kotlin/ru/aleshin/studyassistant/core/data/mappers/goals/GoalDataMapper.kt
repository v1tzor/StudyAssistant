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

package ru.aleshin.studyassistant.core.data.mappers.goals

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.database.models.goals.GoalEntityDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.remote.models.goals.GoalDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.goals.GoalPojo
import ru.aleshin.studyassistant.sqldelight.goals.GoalEntity

/**
 * @author Stanislav Aleshin on 03.05.2025.
 */
fun GoalEntityDetails.mapToDomain() = Goal(
    uid = uid,
    contentType = GoalType.valueOf(contentType),
    contentHomework = contentHomework?.mapToDomain(),
    contentTodo = contentTodo?.mapToDomain(),
    number = number,
    targetDate = targetDate.mapEpochTimeToInstant(),
    desiredTime = desiredTime,
    time = when (GoalTime.Type.valueOf(goalTimeType)) {
        GoalTime.Type.TIMER -> GoalTime.Timer(
            targetTime = checkNotNull(targetTime),
            pastStopTime = checkNotNull(pastStopTime),
            startTimePoint = checkNotNull(startTimePoint).mapEpochTimeToInstant(),
            isActive = isActive,
        )
        GoalTime.Type.STOPWATCH -> GoalTime.Stopwatch(
            pastStopTime = checkNotNull(pastStopTime),
            startTimePoint = checkNotNull(startTimePoint).mapEpochTimeToInstant(),
            isActive = isActive,
        )
        GoalTime.Type.NONE -> GoalTime.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = isDone,
    completeDate = completeDate?.mapEpochTimeToInstant(),
)

fun GoalEntity.mapToDomain() = GoalShort(
    uid = uid,
    contentType = GoalType.valueOf(type),
    contentId = content_id,
    number = number.toInt(),
    targetDate = target_date.mapEpochTimeToInstant(),
    desiredTime = desired_time,
    time = when (GoalTime.Type.valueOf(goal_time_type)) {
        GoalTime.Type.TIMER -> GoalTime.Timer(
            targetTime = checkNotNull(target_time),
            pastStopTime = checkNotNull(past_stop_time),
            startTimePoint = checkNotNull(start_time_point).mapEpochTimeToInstant(),
            isActive = is_active == 1L,
        )
        GoalTime.Type.STOPWATCH -> GoalTime.Stopwatch(
            pastStopTime = checkNotNull(past_stop_time),
            startTimePoint = checkNotNull(start_time_point).mapEpochTimeToInstant(),
            isActive = is_active == 1L,
        )
        GoalTime.Type.NONE -> GoalTime.None
    },
    completeAfterTimeElapsed = complete_after_time_elapsed == 1L,
    isDone = is_done == 1L,
    completeDate = complete_date?.mapEpochTimeToInstant(),
)

fun GoalDetailsPojo.mapToDomain() = Goal(
    uid = uid,
    contentType = GoalType.valueOf(contentType),
    contentHomework = contentHomework?.mapToDomain(),
    contentTodo = contentTodo?.mapToDomain(),
    number = number,
    targetDate = targetDate.mapEpochTimeToInstant(),
    desiredTime = desiredTime,
    time = when (GoalTime.Type.valueOf(goalTimeType)) {
        GoalTime.Type.TIMER -> GoalTime.Timer(
            targetTime = checkNotNull(targetTime),
            pastStopTime = checkNotNull(pastStopTime),
            startTimePoint = checkNotNull(startTimePoint).mapEpochTimeToInstant(),
            isActive = isActive,
        )
        GoalTime.Type.STOPWATCH -> GoalTime.Stopwatch(
            pastStopTime = checkNotNull(pastStopTime),
            startTimePoint = checkNotNull(startTimePoint).mapEpochTimeToInstant(),
            isActive = isActive,
        )
        GoalTime.Type.NONE -> GoalTime.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = isDone,
    completeDate = completeDate?.mapEpochTimeToInstant(),
)

fun GoalPojo.mapToDomain() = GoalShort(
    uid = uid,
    contentType = GoalType.valueOf(type),
    contentId = contentId,
    number = number,
    targetDate = targetDate.mapEpochTimeToInstant(),
    desiredTime = desiredTime,
    time = when (GoalTime.Type.valueOf(goalTimeType)) {
        GoalTime.Type.TIMER -> GoalTime.Timer(
            targetTime = checkNotNull(targetTime),
            pastStopTime = checkNotNull(pastStopTime),
            startTimePoint = checkNotNull(startTimePoint).mapEpochTimeToInstant(),
            isActive = active,
        )
        GoalTime.Type.STOPWATCH -> GoalTime.Stopwatch(
            pastStopTime = checkNotNull(pastStopTime),
            startTimePoint = checkNotNull(startTimePoint).mapEpochTimeToInstant(),
            isActive = active,
        )
        GoalTime.Type.NONE -> GoalTime.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = done,
    completeDate = completeDate?.mapEpochTimeToInstant(),
)

fun Goal.mapToRemoteData() = GoalPojo(
    uid = uid,
    type = contentType.toString(),
    number = number,
    contentId = checkNotNull(contentHomework?.uid ?: contentTodo?.uid),
    contentOrganizationId = contentHomework?.organization?.uid,
    contentDeadline = (contentHomework?.deadline ?: contentTodo?.deadline)?.toEpochMilliseconds(),
    targetDate = targetDate.toEpochMilliseconds(),
    desiredTime = desiredTime,
    goalTimeType = time.type.toString(),
    targetTime = when (time) {
        is GoalTime.Timer -> (time as? GoalTime.Timer)?.targetTime
        is GoalTime.Stopwatch -> null
        is GoalTime.None -> null
    },
    pastStopTime = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).pastStopTime
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).pastStopTime
        is GoalTime.None -> null
    },
    startTimePoint = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).startTimePoint.toEpochMilliseconds()
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).startTimePoint.toEpochMilliseconds()
        is GoalTime.None -> null
    },
    active = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).isActive
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).isActive
        is GoalTime.None -> false
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    done = isDone,
    completeDate = completeDate?.toEpochMilliseconds(),
)

fun Goal.mapToLocalData() = GoalEntity(
    uid = uid,
    type = contentType.toString(),
    number = number.toLong(),
    content_id = checkNotNull(contentHomework?.uid ?: contentTodo?.uid),
    content_organization_id = contentHomework?.organization?.uid,
    content_deadline = (contentHomework?.deadline ?: contentTodo?.deadline)?.toEpochMilliseconds(),
    target_date = targetDate.toEpochMilliseconds(),
    desired_time = desiredTime,
    goal_time_type = time.type.toString(),
    target_time = when (time) {
        is GoalTime.Timer -> (time as? GoalTime.Timer)?.targetTime
        is GoalTime.Stopwatch -> null
        is GoalTime.None -> null
    },
    past_stop_time = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).pastStopTime
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).pastStopTime
        is GoalTime.None -> null
    },
    start_time_point = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).startTimePoint.toEpochMilliseconds()
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).startTimePoint.toEpochMilliseconds()
        is GoalTime.None -> null
    },
    is_active = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).isActive
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).isActive
        is GoalTime.None -> false
    }.let { if (it) 1L else 0L },
    complete_after_time_elapsed = if (completeAfterTimeElapsed) 1L else 0L,
    is_done = if (isDone) 1L else 0L,
    complete_date = completeDate?.toEpochMilliseconds(),
)