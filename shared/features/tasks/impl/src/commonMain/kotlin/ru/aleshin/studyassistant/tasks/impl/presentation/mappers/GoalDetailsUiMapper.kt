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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTimeDetails
import ru.aleshin.studyassistant.core.presentation.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.tasks.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalTimeDetailsUi

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
    time = when (val time = time) {
        is GoalTimeDetails.Stopwatch -> time.mapToUi()
        is GoalTimeDetails.Timer -> time.mapToUi()
        is GoalTimeDetails.None -> GoalTimeDetailsUi.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = isDone,
    completeDate = completeDate,
    updatedAt = updatedAt,
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
    updatedAt = updatedAt,
)
