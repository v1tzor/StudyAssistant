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
import ru.aleshin.studyassistant.core.database.models.goals.BaseGoalEntity
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
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
