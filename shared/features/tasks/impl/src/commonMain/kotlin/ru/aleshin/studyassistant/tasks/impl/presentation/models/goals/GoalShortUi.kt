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

package ru.aleshin.studyassistant.tasks.impl.presentation.models.goals

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis

/**
 * @author Stanislav Aleshin on 01.06.2025.
 */
@Immutable
@Serializable
internal data class GoalShortUi(
    val uid: UID,
    val contentType: GoalType,
    val contentId: UID,
    val number: Int = 0,
    val targetDate: Instant,
    val desiredTime: Millis?,
    val time: GoalTimeUi,
    val completeAfterTimeElapsed: Boolean = false,
    val isDone: Boolean = false,
    val completeDate: Instant?,
    val updatedAt: Long,
)

internal fun GoalDetailsUi.convertToShort() = GoalShortUi(
    uid = uid,
    contentType = contentType,
    contentId = checkNotNull(contentHomework?.uid ?: contentTodo?.uid),
    number = number,
    targetDate = targetDate,
    desiredTime = desiredTime,
    time = when (time) {
        is GoalTimeDetailsUi.Stopwatch -> GoalTimeUi.Stopwatch(
            pastStopTime = time.pastStopTime,
            startTimePoint = time.startTimePoint,
            isActive = time.isActive,
        )
        is GoalTimeDetailsUi.Timer -> GoalTimeUi.Timer(
            targetTime = time.targetTime,
            pastStopTime = time.pastStopTime,
            startTimePoint = time.startTimePoint,
            isActive = time.isActive,
        )
        is GoalTimeDetailsUi.None -> GoalTimeUi.None
    },
    completeAfterTimeElapsed = completeAfterTimeElapsed,
    isDone = isDone,
    completeDate = completeDate,
    updatedAt = updatedAt,
)