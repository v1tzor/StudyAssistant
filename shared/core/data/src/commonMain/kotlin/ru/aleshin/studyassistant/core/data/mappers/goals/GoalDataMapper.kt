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

package ru.aleshin.studyassistant.core.data.mappers.goals

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.database.models.goals.BaseGoalEntity
import ru.aleshin.studyassistant.core.database.models.goals.GoalEntityDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType

/**
 * @author Stanislav Aleshin on 03.05.2025.
 */
fun Goal.mapToLocalData(): BaseGoalEntity {
    val goalTime = time
    return BaseGoalEntity(
        uid = uid,
        type = contentType.name,
        number = number.toLong(),
        contentId = checkNotNull(contentHomework?.uid ?: contentTodo?.uid),
        contentOrganizationId = contentHomework?.organization?.uid,
        contentDeadline = (contentHomework?.deadline ?: contentTodo?.deadline)?.toEpochMilliseconds(),
        targetDate = targetDate.toEpochMilliseconds(),
        desiredTime = desiredTime,
        goalTimeType = goalTime.type.name,
        targetTime = when (goalTime) {
            is GoalTime.Timer -> goalTime.targetTime
            else -> null
        },
        pastStopTime = when (goalTime) {
            is GoalTime.Timer -> goalTime.pastStopTime
            is GoalTime.Stopwatch -> goalTime.pastStopTime
            else -> null
        },
        startTimePoint = when (goalTime) {
            is GoalTime.Timer -> goalTime.startTimePoint.toEpochMilliseconds()
            is GoalTime.Stopwatch -> goalTime.startTimePoint.toEpochMilliseconds()
            else -> null
        },
        isActive = when (goalTime) {
            is GoalTime.Timer -> goalTime.isActive
            is GoalTime.Stopwatch -> goalTime.isActive
            else -> false
        }.let { isActive -> if (isActive) 1L else 0L },
        completeAfterTimeElapsed = if (completeAfterTimeElapsed) 1L else 0L,
        isDone = if (isDone) 1L else 0L,
        completeDate = completeDate?.toEpochMilliseconds(),
        updatedAt = updatedAt,
    )
}

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
    updatedAt = updatedAt,
)
