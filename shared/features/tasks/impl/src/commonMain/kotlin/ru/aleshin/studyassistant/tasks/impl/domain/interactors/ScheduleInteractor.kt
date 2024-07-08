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

package ru.aleshin.studyassistant.tasks.impl.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkResult
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
internal interface ScheduleInteractor {

    suspend fun fetchScheduleByDate(date: Instant): FlowWorkResult<TasksFailures, Schedule>

    class Base(
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: TasksEitherWrapper,
    ) : ScheduleInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchScheduleByDate(date: Instant) = eitherWrapper.wrapFlow {
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(targetUser).first().numberOfWeek
            val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)

            val baseScheduleFlow = baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek, targetUser)
            val customScheduleFlow = customScheduleRepository.fetchScheduleByDate(date, targetUser)

            return@wrapFlow baseScheduleFlow.flatMapLatest { baseSchedule ->
                customScheduleFlow.map { customSchedule ->
                    if (customSchedule != null) {
                        val schedule = customSchedule.copy(
                            classes = customSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
                        )
                        Schedule.Custom(schedule)
                    } else {
                        val schedule = baseSchedule?.copy(
                            classes = baseSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
                        )
                        Schedule.Base(schedule)
                    }
                }
            }
        }
    }
}