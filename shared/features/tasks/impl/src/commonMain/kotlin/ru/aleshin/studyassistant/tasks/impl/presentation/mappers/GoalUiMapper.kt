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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.goals.DailyGoalsProgress
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTimeDetails
import ru.aleshin.studyassistant.tasks.impl.domain.entities.GoalCreateModel
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.DailyGoalsProgressUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalTimeDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalTimeUi

/**
 * @author Stanislav Aleshin on 02.06.2025.
 */
internal fun GoalDetails.mapToUi() = GoalDetailsUi(
    uid = uid,
    contentType = contentType,
    contentHomework = contentHomework?.mapToUi(),
    contentTodo = contentTodo?.mapToUi(),
    number = number,
    targetDate = targetDate,
    desiredTime = desiredTime,
    time = when (time) {
        is GoalTimeDetails.Stopwatch -> (time as GoalTimeDetails.Stopwatch).mapToUi()
        is GoalTimeDetails.Timer -> (time as GoalTimeDetails.Timer).mapToUi()
        is GoalTimeDetails.None -> GoalTimeDetailsUi.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun GoalShort.mapToUi() = GoalShortUi(
    uid = uid,
    contentType = contentType,
    contentId = contentId,
    number = number,
    targetDate = targetDate,
    desiredTime = desiredTime,
    time = when (time) {
        is GoalTime.Stopwatch -> (time as GoalTime.Stopwatch).mapToUi()
        is GoalTime.Timer -> (time as GoalTime.Timer).mapToUi()
        is GoalTime.None -> GoalTimeUi.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun DailyGoalsProgress.mapToUi() = DailyGoalsProgressUi(
    goalsCount = goalsCount,
    homeworkGoals = homeworkGoals,
    todoGoals = todoGoals,
    progress = progress,
)

internal fun GoalTimeDetails.Timer.mapToUi() = GoalTimeDetailsUi.Timer(
    targetTime = targetTime,
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    leftTime = leftTime,
    progress = progress,
    isActive = isActive,
)

internal fun GoalTimeDetails.Stopwatch.mapToUi() = GoalTimeDetailsUi.Stopwatch(
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    elapsedTime = elapsedTime,
    progress = progress,
    isActive = isActive,
)

internal fun GoalTime.Timer.mapToUi() = GoalTimeUi.Timer(
    targetTime = targetTime,
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    isActive = isActive,
)

internal fun GoalTime.Stopwatch.mapToUi() = GoalTimeUi.Stopwatch(
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    isActive = isActive,
)

internal fun GoalDetailsUi.mapToDomain() = Goal(
    uid = uid,
    contentType = contentType,
    contentHomework = contentHomework?.mapToDomain(),
    contentTodo = contentTodo?.mapToDomain(),
    number = number,
    targetDate = targetDate,
    desiredTime = desiredTime,
    time = when (time) {
        is GoalTimeDetailsUi.Stopwatch -> time.mapToDomain()
        is GoalTimeDetailsUi.Timer -> time.mapToDomain()
        is GoalTimeDetailsUi.None -> GoalTime.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun GoalShortUi.mapToDomain() = GoalShort(
    uid = uid,
    contentType = contentType,
    contentId = contentId,
    number = number,
    targetDate = targetDate,
    desiredTime = desiredTime,
    time = when (time) {
        is GoalTimeUi.Stopwatch -> time.mapToDomain()
        is GoalTimeUi.Timer -> time.mapToDomain()
        is GoalTimeUi.None -> GoalTime.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun GoalTimeDetailsUi.Timer.mapToDomain() = GoalTime.Timer(
    targetTime = targetTime,
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    isActive = isActive,
)

internal fun GoalTimeDetailsUi.Stopwatch.mapToDomain() = GoalTime.Stopwatch(
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    isActive = isActive,
)

internal fun GoalTimeUi.Timer.mapToDomain() = GoalTime.Timer(
    targetTime = targetTime,
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    isActive = isActive,
)

internal fun GoalTimeUi.Stopwatch.mapToDomain() = GoalTime.Stopwatch(
    pastStopTime = pastStopTime,
    startTimePoint = startTimePoint,
    isActive = isActive,
)

internal fun GoalCreateModelUi.mapToDomain() = GoalCreateModel(
    date = date,
    contentType = contentType,
    contentTodo = contentTodo?.mapToDomain(),
    contentHomework = contentHomework?.mapToDomain(),
    desiredTime = desiredTime,
)