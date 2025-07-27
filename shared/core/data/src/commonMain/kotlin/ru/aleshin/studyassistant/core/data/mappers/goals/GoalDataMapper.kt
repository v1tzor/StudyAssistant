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
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.utils.sync.MultipleSyncMapper
import ru.aleshin.studyassistant.core.database.models.goals.BaseGoalEntity
import ru.aleshin.studyassistant.core.database.models.goals.GoalEntityDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.remote.models.goals.GoalDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.goals.GoalPojo

/**
 * @author Stanislav Aleshin on 03.05.2025.
 */
fun Goal.mapToRemoteData(userId: UID) = GoalPojo(
    id = uid,
    userId = userId,
    type = contentType.name,
    number = number,
    contentId = checkNotNull(contentHomework?.uid ?: contentTodo?.uid),
    contentOrganizationId = contentHomework?.organization?.uid,
    contentDeadline = (contentHomework?.deadline ?: contentTodo?.deadline)?.toEpochMilliseconds(),
    targetDate = targetDate.toEpochMilliseconds(),
    desiredTime = desiredTime,
    goalTimeType = time.type.name,
    targetTime = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).targetTime
        else -> null
    },
    pastStopTime = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).pastStopTime
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).pastStopTime
        else -> null
    },
    startTimePoint = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).startTimePoint.toEpochMilliseconds()
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).startTimePoint.toEpochMilliseconds()
        else -> null
    },
    active = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).isActive
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).isActive
        else -> false
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    done = isDone,
    completeDate = completeDate?.toEpochMilliseconds(),
    updatedAt = updatedAt,
)

fun Goal.mapToLocalData() = BaseGoalEntity(
    uid = uid,
    type = contentType.name,
    number = number.toLong(),
    contentId = checkNotNull(contentHomework?.uid ?: contentTodo?.uid),
    contentOrganizationId = contentHomework?.organization?.uid,
    contentDeadline = (contentHomework?.deadline ?: contentTodo?.deadline)?.toEpochMilliseconds(),
    targetDate = targetDate.toEpochMilliseconds(),
    desiredTime = desiredTime,
    goalTimeType = time.type.name,
    targetTime = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).targetTime
        else -> null
    },
    pastStopTime = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).pastStopTime
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).pastStopTime
        else -> null
    },
    startTimePoint = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).startTimePoint.toEpochMilliseconds()
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).startTimePoint.toEpochMilliseconds()
        else -> null
    },
    isActive = when (time) {
        is GoalTime.Timer -> (time as GoalTime.Timer).isActive
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).isActive
        else -> false
    }.let { if (it) 1L else 0L },
    completeAfterTimeElapsed = if (completeAfterTimeElapsed) 1L else 0L,
    isDone = if (isDone) 1L else 0L,
    completeDate = completeDate?.toEpochMilliseconds(),
    updatedAt = updatedAt,
    isCacheData = 0L
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
    updatedAt = updatedAt,
)

fun GoalPojo.mapToDomain() = GoalShort(
    uid = id,
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
    updatedAt = updatedAt,
)

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

fun BaseGoalEntity.mapToDomain() = GoalShort(
    uid = uid,
    contentType = GoalType.valueOf(type),
    contentId = contentId,
    number = number.toInt(),
    targetDate = targetDate.mapEpochTimeToInstant(),
    desiredTime = desiredTime,
    time = when (GoalTime.Type.valueOf(goalTimeType)) {
        GoalTime.Type.TIMER -> GoalTime.Timer(
            targetTime = checkNotNull(targetTime),
            pastStopTime = checkNotNull(pastStopTime),
            startTimePoint = checkNotNull(startTimePoint).mapEpochTimeToInstant(),
            isActive = isActive == 1L,
        )
        GoalTime.Type.STOPWATCH -> GoalTime.Stopwatch(
            pastStopTime = checkNotNull(pastStopTime),
            startTimePoint = checkNotNull(startTimePoint).mapEpochTimeToInstant(),
            isActive = isActive == 1L,
        )
        GoalTime.Type.NONE -> GoalTime.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed == 1L,
    isDone = isDone == 1L,
    completeDate = completeDate?.mapEpochTimeToInstant(),
    updatedAt = updatedAt,
)

fun BaseGoalEntity.convertToRemote(userId: String) = GoalPojo(
    id = uid,
    userId = userId,
    type = type,
    number = number.toInt(),
    contentId = contentId,
    contentOrganizationId = contentOrganizationId,
    contentDeadline = contentDeadline,
    targetDate = targetDate,
    desiredTime = desiredTime,
    goalTimeType = goalTimeType,
    targetTime = targetTime,
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    active = isActive == 1L,
    completeAfterTimeElapsed = completeAfterTimeElapsed == 1L,
    done = isDone == 1L,
    completeDate = completeDate,
    updatedAt = updatedAt,
)

fun GoalPojo.convertToLocal() = BaseGoalEntity(
    uid = id,
    type = type,
    number = number.toLong(),
    contentId = contentId,
    contentOrganizationId = contentOrganizationId,
    contentDeadline = contentDeadline,
    targetDate = targetDate,
    desiredTime = desiredTime,
    goalTimeType = goalTimeType,
    targetTime = targetTime,
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    isActive = if (active) 1L else 0L,
    completeAfterTimeElapsed = if (completeAfterTimeElapsed) 1L else 0L,
    isDone = if (done) 1L else 0L,
    completeDate = completeDate,
    updatedAt = updatedAt,
    isCacheData = 1L,
)

class GoalSyncMapper : MultipleSyncMapper<BaseGoalEntity, GoalPojo>(
    localToRemote = { userId -> convertToRemote(userId) },
    remoteToLocal = { convertToLocal() },
)