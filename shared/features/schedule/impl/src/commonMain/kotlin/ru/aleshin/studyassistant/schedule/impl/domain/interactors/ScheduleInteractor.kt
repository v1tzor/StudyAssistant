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

package ru.aleshin.studyassistant.schedule.impl.domain.interactors

import entities.classes.convertToDetails
import entities.common.numberOfRepeatWeek
import entities.schedules.ScheduleDetails
import entities.schedules.convertToDetails
import extensions.dateTimeUTC
import functional.FlowDomainResult
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import repositories.BaseScheduleRepository
import repositories.CalendarSettingsRepository
import repositories.CustomScheduleRepository
import repositories.HomeworksRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.WeekScheduleDetails

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface ScheduleInteractor {

    suspend fun fetchDetailsWeekSchedule(week: TimeRange): FlowDomainResult<ScheduleFailures, WeekScheduleDetails>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : ScheduleInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchDetailsWeekSchedule(week: TimeRange) = eitherWrapper.wrapFlow {
            val baseSchedules = baseScheduleRepository.fetchSchedulesByTimeRange(week, targetUser).first()
            val customSchedules = customScheduleRepository.fetchSchedulesByTimeRange(week, targetUser).first()
            val homeworksFlow = homeworksRepository.fetchHomeworksByTimeRange(week, targetUser)

            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(targetUser).first().numberOfWeek
            val currentNumberOfWeek = week.from.dateTimeUTC().date.numberOfRepeatWeek(maxNumberOfWeek)

            val weekDaySchedules = mutableMapOf<DayOfWeek, ScheduleDetails?>().apply {
                DayOfWeek.entries.forEach { put(it, null) }
            }

            homeworksFlow.map { homeworks ->
                customSchedules.forEach { customSchedule ->
                    val dayOfWeek = customSchedule.date.dateTimeUTC().dayOfWeek
                    val detailsSchedule = customSchedule.convertToDetails(
                        classesMapper = { classModel ->
                            classModel.convertToDetails(homeworks.find { it.classId == classModel.uid })
                        }
                    )
                    weekDaySchedules[dayOfWeek] = ScheduleDetails.Custom(detailsSchedule)
                }
                baseSchedules.groupBy { it.dayOfWeek }.forEach { scheduleEntry ->
                    val dayOfWeek = scheduleEntry.key
                    if (weekDaySchedules[dayOfWeek] == null) {
                        val schedule = scheduleEntry.value.findLast { baseSchedule ->
                            baseSchedule.week == currentNumberOfWeek
                        }
                        val detailsSchedule = schedule?.convertToDetails(
                            classesMapper = { classModel ->
                                classModel.convertToDetails(homeworks.find { it.classId == classModel.uid })
                            }
                        )
                        weekDaySchedules[dayOfWeek] = ScheduleDetails.Base(detailsSchedule)
                    }
                }

                WeekScheduleDetails(
                    from = week.from,
                    to = week.to,
                    numberOfWeek = currentNumberOfWeek,
                    weekDaySchedules = weekDaySchedules,
                )
            }
        }
    }
}
