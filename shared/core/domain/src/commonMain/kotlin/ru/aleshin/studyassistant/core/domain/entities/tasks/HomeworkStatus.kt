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

package ru.aleshin.studyassistant.core.domain.entities.tasks

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
enum class HomeworkStatus {
    COMPLETE, WAIT, IN_FUTURE, SKIPPED, NOT_COMPLETE;

    companion object {
        fun calculate(
            isDone: Boolean,
            completeDate: Instant?,
            deadline: Instant,
            currentTime: Instant
        ): HomeworkStatus {
            val isComplete = completeDate != null
            val currentDate = currentTime.startThisDay()
            return if (isComplete) {
                if (isDone) COMPLETE else SKIPPED
            } else {
                val duration = deadline - currentTime
                if (duration.isPositive()) {
                    val nearestTimeRange = TimeRange(currentDate, currentDate.shiftDay(1))
                    if (nearestTimeRange.containsDate(deadline)) WAIT else IN_FUTURE
                } else {
                    NOT_COMPLETE
                }
            }
        }
    }
}