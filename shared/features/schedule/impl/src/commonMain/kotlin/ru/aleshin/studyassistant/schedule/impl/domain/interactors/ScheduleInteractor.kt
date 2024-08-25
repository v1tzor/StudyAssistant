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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.classes.convertToDetails
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.ScheduleDetails
import ru.aleshin.studyassistant.core.domain.entities.schedules.WeekScheduleDetails
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.convertToDetails
import ru.aleshin.studyassistant.core.domain.entities.schedules.convertToDetails
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.convertToDetails
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface ScheduleInteractor {

    suspend fun addBaseSchedules(schedules: List<BaseSchedule>): UnitDomainResult<ScheduleFailures>
    suspend fun fetchDetailsWeekSchedule(week: TimeRange): FlowDomainResult<ScheduleFailures, WeekScheduleDetails>
    suspend fun fetchDetailsScheduleByDate(date: Instant): FlowDomainResult<ScheduleFailures, ScheduleDetails>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val usersRepository: UsersRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val dateManager: DateManager,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : ScheduleInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addBaseSchedules(schedules: List<BaseSchedule>) = eitherWrapper.wrapUnit {
            val currentWeek = dateManager.fetchCurrentWeek()
            val currentInstant = dateManager.fetchCurrentInstant()

            val currentSchedules = baseScheduleRepository.fetchSchedulesByVersion(currentWeek, null, targetUser).first()
            val deprecatedSchedules = currentSchedules.map { schedule ->
                val deprecatedVersion = schedule.dateVersion.makeDeprecated(currentInstant)
                val updatedClasses = schedule.classes.map { it.copy(uid = randomUUID()) }
                return@map schedule.copy(dateVersion = deprecatedVersion, classes = updatedClasses)
            }
            baseScheduleRepository.addOrUpdateSchedulesGroup(deprecatedSchedules, targetUser)

            val newActualSchedules = schedules.map { schedules ->
                val actualVersion = DateVersion.createNewVersion(currentInstant)
                return@map schedules.copy(dateVersion = actualVersion)
            }
            baseScheduleRepository.addOrUpdateSchedulesGroup(newActualSchedules, targetUser)

            val notificationSettings = notificationSettingsRepository.fetchSettings(targetUser).first()
            if (notificationSettings.beginningOfClasses != null) {
                startClassesReminderManager.startOrRetryReminderService()
            }
            if (notificationSettings.endOfClasses) {
                endClassesReminderManager.startOrRetryReminderService()
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchDetailsWeekSchedule(week: TimeRange) = eitherWrapper.wrapFlow {
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(targetUser).first().numberOfWeek
            val numberOfWeek = week.from.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)

            val baseSchedulesFlow = baseScheduleRepository.fetchSchedulesByVersion(week, numberOfWeek, targetUser)
            val customSchedulesFlow = customScheduleRepository.fetchSchedulesByTimeRange(week, targetUser)

            val homeworksFlow = homeworksRepository.fetchHomeworksByTimeRange(week, targetUser)

            baseSchedulesFlow.flatMapLatest { baseSchedules ->
                customSchedulesFlow.flatMapLatest { customSchedules ->
                    homeworksFlow.map { homeworks ->
                        val weekDaySchedules = buildMap {
                            customSchedules.forEach { customSchedule ->
                                val scheduleWeekDay = customSchedule.date.dateTime().dayOfWeek
                                val customDetailsSchedule = customSchedule.convertToDetails { classModel ->
                                    classModel.convertToDetails(homeworks.find { it.classId == classModel.uid })
                                }
                                val sortedClasses = customDetailsSchedule.classes.sortedBy { classModel ->
                                    classModel.timeRange.from.dateTime().time
                                }
                                val detailsSchedule = ScheduleDetails.Custom(
                                    customDetailsSchedule.copy(classes = sortedClasses)
                                )
                                put(scheduleWeekDay, detailsSchedule)
                            }
                            baseSchedules.forEach { baseSchedule ->
                                val dayOfWeek = baseSchedule.dayOfWeek
                                if (get(dayOfWeek) == null) {
                                    val baseDetailsSchedule = baseSchedule.convertToDetails { classModel ->
                                        classModel.convertToDetails(homeworks.find { it.classId == classModel.uid })
                                    }
                                    val sortedClasses = baseDetailsSchedule.classes.sortedBy { classModel ->
                                        classModel.timeRange.from.dateTime().time
                                    }
                                    val detailsSchedule = ScheduleDetails.Base(
                                        baseDetailsSchedule.copy(classes = sortedClasses)
                                    )
                                    put(dayOfWeek, detailsSchedule)
                                }
                            }
                        }

                        WeekScheduleDetails(
                            from = week.from,
                            to = week.to,
                            numberOfWeek = numberOfWeek,
                            weekDaySchedules = weekDaySchedules,
                        )
                    }
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchDetailsScheduleByDate(date: Instant) = eitherWrapper.wrapFlow {
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(targetUser).first().numberOfWeek
            val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)

            val homeworksFlow = homeworksRepository.fetchHomeworksByDate(date, targetUser)

            val baseScheduleFlow = baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek, targetUser)
            val customScheduleFlow = customScheduleRepository.fetchScheduleByDate(date, targetUser)
            val scheduleFlow = baseScheduleFlow.flatMapLatest { baseSchedule ->
                customScheduleFlow.map { customSchedule ->
                    if (customSchedule != null) {
                        val sortedClasses = customSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
                        Schedule.Custom(customSchedule.copy(classes = sortedClasses))
                    } else {
                        val sortedClasses = baseSchedule?.classes?.sortedBy { it.timeRange.from.dateTime().time }
                        Schedule.Base(baseSchedule?.copy(classes = sortedClasses ?: emptyList()))
                    }
                }
            }

            return@wrapFlow scheduleFlow.flatMapLatest { schedule ->
                homeworksFlow.map { homeworks ->
                    schedule.convertToDetails(
                        classesMapper = { classModel ->
                            classModel.convertToDetails(homeworks.find { it.classId == classModel.uid })
                        }
                    )
                }
            }
        }
    }
}