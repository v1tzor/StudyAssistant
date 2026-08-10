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

package ru.aleshin.studyassistant.schedule.impl.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTimeByWeek
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftMinutes
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.classes.ActiveClass
import ru.aleshin.studyassistant.core.domain.entities.classes.ClassDetails
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
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkDetails
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.DailyScheduleOverview
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.WeekScheduleOverview
import ru.aleshin.studyassistant.core.domain.entities.tasks.convertToDetails as convertHomeworkToDetails

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface ScheduleInteractor {

    suspend fun addBaseSchedules(schedules: List<BaseSchedule>): UnitDomainResult<ScheduleFailures>
    suspend fun fetchDetailsWeekSchedule(week: TimeRange): FlowDomainResult<ScheduleFailures, WeekScheduleOverview>
    suspend fun fetchDetailsScheduleByDate(date: Instant): FlowDomainResult<ScheduleFailures, DailyScheduleOverview>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val dateManager: DateManager,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : ScheduleInteractor {

        override suspend fun addBaseSchedules(schedules: List<BaseSchedule>) = eitherWrapper.wrapUnit {
            val currentWeek = dateManager.fetchCurrentWeek()
            val currentInstant = dateManager.fetchCurrentInstant()

            val currentSchedules = baseScheduleRepository.fetchSchedulesByVersion(currentWeek, null).first()
            val deprecatedSchedules = currentSchedules.map { schedule ->
                val deprecatedVersion = schedule.dateVersion.makeDeprecated(currentInstant)
                val updatedClasses = schedule.classes.map { it.copy(uid = randomUUID()) }
                return@map schedule.copy(
                    dateVersion = deprecatedVersion,
                    classes = updatedClasses,
                    updatedAt = currentInstant.toEpochMilliseconds(),
                )
            }
            baseScheduleRepository.addOrUpdateSchedulesGroup(deprecatedSchedules)

            val newActualSchedules = schedules.map { schedules ->
                val actualVersion = DateVersion.createNewVersion(currentInstant)
                return@map schedules.copy(
                    dateVersion = actualVersion,
                    updatedAt = currentInstant.toEpochMilliseconds(),
                )
            }
            baseScheduleRepository.addOrUpdateSchedulesGroup(newActualSchedules)

            val notificationSettings = notificationSettingsRepository.fetchSettings().first()
            startClassesReminderManager.startOrRetryReminderService()
            if (notificationSettings.endOfClasses) {
                endClassesReminderManager.startOrRetryReminderService()
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchDetailsWeekSchedule(week: TimeRange) = eitherWrapper.wrapFlow {
            val calendarSettings = calendarSettingsRepository.fetchSettings().first()
            val numberOfWeek = week.from.dateTime().date.numberOfRepeatWeek(calendarSettings.numberOfWeek)
            val holidays = calendarSettings.holidays

            val baseSchedulesFlow = baseScheduleRepository.fetchSchedulesByVersion(week, numberOfWeek)
            val customSchedulesFlow = customScheduleRepository.fetchSchedulesByTimeRange(week)
            val homeworksFlow = homeworksRepository.fetchHomeworksByTimeRange(week)

            combine(
                baseSchedulesFlow,
                customSchedulesFlow,
                homeworksFlow,
                dateManager.secondTicker(),
            ) { baseSchedules, customSchedules, homeworks, _ ->
                val currentTime = dateManager.fetchCurrentInstant()
                val weekDaySchedules = buildMap {
                    customSchedules.forEach { customSchedule ->
                        val scheduleWeekDay = customSchedule.date.dateTime().dayOfWeek
                        val customDetailsSchedule = customSchedule.convertToDetails { classModel ->
                            classModel.convertToDetails(
                                homeworks.find { it.classId == classModel.uid }.toDetails(currentTime)
                            )
                        }
                        val sortedClasses = customDetailsSchedule.classes.sortedBy { classModel ->
                            classModel.timeRange.from.dateTime().time
                        }
                        val detailsSchedule = ScheduleDetails.Custom(
                            customDetailsSchedule.copy(classes = sortedClasses)
                        ).withClassNumbers()
                        put(scheduleWeekDay, detailsSchedule)
                    }
                    baseSchedules.forEach { baseSchedule ->
                        val dayOfWeek = baseSchedule.dayOfWeek
                        val date = dayOfWeek.dateTimeByWeek(week.from)
                        if (get(dayOfWeek) == null) {
                            val baseDetailsSchedule = baseSchedule.convertToDetails { classModel ->
                                classModel.convertToDetails(
                                    homeworks.find { it.classId == classModel.uid }.toDetails(currentTime)
                                )
                            }
                            val filteredClasses = baseDetailsSchedule.classes.filter { classModel ->
                                holidays.none {
                                    val dateFilter = TimeRange(it.start, it.end).containsDate(date)
                                    val orgFilter = it.organizations.contains(classModel.organization.uid)
                                    return@none dateFilter && orgFilter
                                }
                            }
                            val sortedClasses = filteredClasses.sortedBy { classModel ->
                                classModel.timeRange.from.dateTime().time
                            }
                            val detailsSchedule = ScheduleDetails.Base(
                                data = baseDetailsSchedule.copy(classes = sortedClasses)
                            ).withClassNumbers()
                            put(dayOfWeek, detailsSchedule)
                        }
                    }
                }

                val weekSchedule = WeekScheduleDetails(
                    from = week.from,
                    to = week.to,
                    numberOfWeek = numberOfWeek,
                    weekDaySchedules = weekDaySchedules,
                )
                WeekScheduleOverview(
                    schedule = weekSchedule,
                    activeClass = weekSchedule.fetchActiveClass(currentTime),
                )
            }.distinctUntilChanged()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchDetailsScheduleByDate(date: Instant) = eitherWrapper.wrapFlow {
            val calendarSettings = calendarSettingsRepository.fetchSettings().first()
            val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(calendarSettings.numberOfWeek)
            val holidays = calendarSettings.holidays

            val baseScheduleFlow = baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek)
            val customScheduleFlow = customScheduleRepository.fetchScheduleByDate(date)
            val homeworksFlow = homeworksRepository.fetchHomeworksByDate(date)

            combine(
                baseScheduleFlow,
                customScheduleFlow,
                homeworksFlow,
                dateManager.secondTicker(),
            ) { baseSchedule, customSchedule, homeworks, _ ->
                val currentTime = dateManager.fetchCurrentInstant()
                val schedule = if (customSchedule != null) {
                    val sortedClasses = customSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
                    Schedule.Custom(customSchedule.copy(classes = sortedClasses))
                } else {
                    val filteredClasses = baseSchedule?.classes?.filter { classModel ->
                        holidays.none {
                            val dateFilter = TimeRange(it.start, it.end).containsDate(date)
                            val organizationFilter = it.organizations.contains(classModel.organization.uid)
                            return@none dateFilter && organizationFilter
                        }
                    }
                    val sortedClasses = filteredClasses?.sortedBy { it.timeRange.from.dateTime().time }
                    Schedule.Base(baseSchedule?.copy(classes = sortedClasses ?: emptyList()))
                }

                val scheduleDetails = schedule.convertToDetails(
                    classesMapper = { classModel ->
                        classModel.convertToDetails(
                            homeworks.find { it.classId == classModel.uid }.toDetails(currentTime)
                        )
                    }
                ).withClassNumbers()
                DailyScheduleOverview(
                    schedule = scheduleDetails,
                    activeClass = scheduleDetails.fetchActiveClass(date, currentTime),
                )
            }.distinctUntilChanged()
        }

        private fun ScheduleDetails.fetchActiveClass(
            classesDate: Instant,
            currentInstant: Instant,
        ): ActiveClass? {
            val scheduleClasses = mapToValue(
                onBaseSchedule = { it?.classes },
                onCustomSchedule = { it?.classes },
            )
            if (!classesDate.equalsDay(currentInstant) || scheduleClasses == null) return null

            val activeClass = scheduleClasses.find { classModel ->
                val endInstant = classesDate.setHoursAndMinutes(classModel.timeRange.to)
                currentInstant <= endInstant
            } ?: return null
            val lastClass = scheduleClasses.findLast { classModel ->
                val endInstant = classesDate.setHoursAndMinutes(classModel.timeRange.to)
                val activeStartInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.from)
                activeStartInstant > endInstant
            }
            val lastEndInstant = lastClass?.timeRange?.to?.let { endTime ->
                classesDate.setHoursAndMinutes(endTime)
            }
            val startInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.from)
            val endInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.to)
            val isStarted = currentInstant > startInstant

            return ActiveClass(
                uid = activeClass.uid,
                isStarted = isStarted,
                progress = dateManager.calculateProgress(
                    startTime = if (isStarted) startInstant else lastEndInstant ?: startInstant.shiftMinutes(-10),
                    endTime = if (isStarted) endInstant else startInstant,
                ),
                duration = dateManager.calculateLeftDateTime(
                    endDateTime = if (isStarted) endInstant else startInstant,
                ),
            )
        }

        private fun WeekScheduleDetails.fetchActiveClass(currentInstant: Instant): ActiveClass? {
            val currentDateTime = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
            val weekDaySchedule = weekDaySchedules[currentDateTime.dayOfWeek]
            val classesDate = currentDateTime.dayOfWeek.dateTimeByWeek(from)
            val scheduleClasses = weekDaySchedule?.mapToValue(
                onBaseSchedule = { it?.classes },
                onCustomSchedule = { it?.classes },
            ) ?: return null
            val activeClass = scheduleClasses.find { classModel ->
                val startInstant = classesDate.setHoursAndMinutes(classModel.timeRange.from)
                val endInstant = classesDate.setHoursAndMinutes(classModel.timeRange.to)
                currentInstant in startInstant..endInstant
            } ?: return null
            val startInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.from)
            val endInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.to)

            return ActiveClass(
                uid = activeClass.uid,
                isStarted = currentInstant > startInstant,
                progress = dateManager.calculateProgress(startInstant, endInstant),
                duration = dateManager.calculateLeftDateTime(endInstant),
            )
        }

        private fun ScheduleDetails.withClassNumbers() = when (this) {
            is ScheduleDetails.Base -> ScheduleDetails.Base(
                data = data?.let { schedule ->
                    schedule.copy(classes = schedule.classes.withClassNumbers())
                },
            )

            is ScheduleDetails.Custom -> ScheduleDetails.Custom(
                data = data?.let { schedule ->
                    schedule.copy(classes = schedule.classes.withClassNumbers())
                },
            )
        }

        private fun List<ClassDetails>.withClassNumbers(): List<ClassDetails> {
            val organizationNumbers = mutableMapOf<UID, Int>()
            return map { classModel ->
                val organizationId = classModel.organization.uid
                val number = organizationNumbers.getOrElse(organizationId) { 0 } + 1
                organizationNumbers[organizationId] = number
                classModel.copy(number = number)
            }
        }

        private fun Homework?.toDetails(currentTime: Instant): HomeworkDetails? {
            return this?.convertHomeworkToDetails(
                status = HomeworkStatus.calculate(
                    isDone = isDone,
                    completeDate = completeDate,
                    deadline = deadline,
                    currentTime = currentTime,
                ),
                linkedGoal = null,
            )
        }
    }
}
