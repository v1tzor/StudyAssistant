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

import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTimeDetails
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalTimeDetailsUi

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
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
