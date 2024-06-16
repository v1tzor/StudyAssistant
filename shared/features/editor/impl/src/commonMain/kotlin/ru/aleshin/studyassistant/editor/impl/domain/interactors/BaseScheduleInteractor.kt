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

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import entities.common.NumberOfRepeatWeek
import entities.schedules.BaseSchedule
import functional.FlowDomainResult
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import repositories.BaseScheduleRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.BaseWeekSchedule
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal interface BaseScheduleInteractor {

    suspend fun fetchScheduleById(uid: UID): FlowDomainResult<EditorFailures, BaseSchedule?>
    suspend fun fetchScheduleByTimeRange(
        timeRange: TimeRange,
        numberOfWeek: NumberOfRepeatWeek
    ): FlowDomainResult<EditorFailures, BaseWeekSchedule>

    class Base(
        private val scheduleRepository: BaseScheduleRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : BaseScheduleInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchScheduleById(uid: UID) = eitherWrapper.wrapFlow {
            scheduleRepository.fetchScheduleById(uid, targetUser)
        }

        override suspend fun fetchScheduleByTimeRange(
            timeRange: TimeRange,
            numberOfWeek: NumberOfRepeatWeek,
        ) = eitherWrapper.wrapFlow {
            val weekDaySchedules = mutableMapOf<DayOfWeek, BaseSchedule?>().apply {
                DayOfWeek.entries.forEach { put(it, null) }
            }
            val schedulesByTimeRange = scheduleRepository.fetchSchedulesByTimeRange(timeRange, numberOfWeek, targetUser)

            return@wrapFlow schedulesByTimeRange.map { rawSchedules ->
                rawSchedules.forEach { schedule ->
                    weekDaySchedules[schedule.dayOfWeek] = schedule.copy(
                        classes = schedule.classes.sortedBy { it.timeRange.from },
                    )
                }

                BaseWeekSchedule(
                    from = timeRange.from,
                    to = timeRange.to,
                    numberOfWeek = numberOfWeek,
                    weekDaySchedules = weekDaySchedules,
                )
            }
        }
    }
}