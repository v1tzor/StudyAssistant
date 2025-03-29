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
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.shiftDay

/**
 * @author Stanislav Aleshin on 26.03.2025.
 */
enum class DailyHomeworksStatus {
    COMPLETE_ALL, IN_COMING, IN_FUTURE, ERROR, EMPTY;

    companion object {
        fun calculate(
            targetDate: Instant,
            currentDate: Instant,
            homeworkStatuses: List<HomeworkStatus>,
        ): DailyHomeworksStatus {
            return if (homeworkStatuses.isEmpty()) {
                EMPTY
            } else if (homeworkStatuses.all { it == HomeworkStatus.COMPLETE || it == HomeworkStatus.SKIPPED }) {
                COMPLETE_ALL
            } else if (targetDate.equalsDay(currentDate) || currentDate > targetDate) {
                ERROR
            } else {
                if (targetDate.equalsDay(currentDate.shiftDay(1))) {
                    IN_COMING
                } else {
                    IN_FUTURE
                }
            }
        }
    }
}