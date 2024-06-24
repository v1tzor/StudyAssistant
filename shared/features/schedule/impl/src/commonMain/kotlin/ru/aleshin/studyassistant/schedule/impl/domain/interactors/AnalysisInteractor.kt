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

import entities.analytics.DailyAnalysis
import entities.common.numberOfRepeatWeek
import extensions.dateTime
import extensions.isCurrentDay
import extensions.startThisDay
import functional.DomainResult
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import repositories.BaseScheduleRepository
import repositories.CalendarSettingsRepository
import repositories.CustomScheduleRepository
import repositories.HomeworksRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures

/**
 * @author Stanislav Aleshin on 13.06.2024.
 */
internal interface AnalysisInteractor {

    suspend fun fetchWeekAnalysis(week: TimeRange): DomainResult<ScheduleFailures, List<DailyAnalysis>>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : AnalysisInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchWeekAnalysis(week: TimeRange) = eitherWrapper.wrap {
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(targetUser).first().numberOfWeek
            val currentNumberOfWeek = week.from.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
            val baseSchedules = baseScheduleRepository.fetchSchedulesByVersion(week, currentNumberOfWeek, targetUser).first()
            val customSchedules = customScheduleRepository.fetchSchedulesByTimeRange(week, targetUser).first()
            val homeworks = homeworksRepository.fetchHomeworksByTimeRange(week, targetUser).first().groupBy {
                it.date.startThisDay()
            }
            val tasks = mapOf<Instant, List<Boolean>>() // TODO

            val classMinuteDurationRate = 1f / 60f
            val testRate = 2
            val moveRate = 2
            val theoryRate = 0.2f
            val practiceRate = 0.4f
            val presentationRate = 1.5f
            val taskRation = 1
            val taskPriorityRation = 2
            val maxRate = 31f

            val weekAnalysis = mutableListOf<DailyAnalysis>()
            week.periodDates().forEach { instant ->
                val customClasses = customSchedules.find { it.date.isCurrentDay(instant) }?.classes
                val classes = customClasses ?: baseSchedules.find { it.dayOfWeek == instant.dateTime().dayOfWeek }?.classes
                val locations = classes?.groupBy { it.organization }?.mapValues { classEntry ->
                    classEntry.value.toSet().map { it.location }
                }
                val dailyHomeworks = homeworks[instant.startThisDay()]
                val dailyTasks = tasks[instant.startThisDay()]

                val numberOfClasses = classes?.size ?: 0
                val classesDuration = classes?.map { it.timeRange.periodDuration() }?.sumOf { it.minutes } ?: 0
                val classesRate = classesDuration * classMinuteDurationRate

                val numberOfTests = dailyHomeworks?.count { it.test != null } ?: 0
                val testsRate = numberOfTests * testRate

                val numberOfMovements = locations?.map { it.value.count() }?.sum() ?: 0
                val movementsRate = numberOfMovements * moveRate

                val numberOfHomeworks = dailyHomeworks?.map { it.isDone } ?: emptyList()
                val numberOfTheories = dailyHomeworks?.sumOf { it.theoreticalTasks.split(',').size } ?: 0
                val numberOfPractices = dailyHomeworks?.sumOf { it.practicalTasks.split(',').size } ?: 0
                val numberOfPresentations = dailyHomeworks?.sumOf { it.presentationsTasks.split(',').size } ?: 0
                val homeworksRate = (numberOfTheories * theoryRate) + (numberOfPractices * practiceRate) + (numberOfPresentations * presentationRate)

                val numberOfTasks = dailyTasks ?: emptyList()
                val tasksRate = numberOfTasks.size * taskRation

                val commonRate = classesRate + testsRate + movementsRate + homeworksRate + tasksRate

                val analysis = DailyAnalysis(
                    date = instant,
                    generalAssessment = commonRate / maxRate,
                    numberOfClasses = numberOfClasses,
                    numberOfTests = numberOfTests,
                    numberOfMovements = numberOfMovements,
                    numberOfHomeworks = numberOfHomeworks,
                    numberOfTasks = numberOfTasks,
                )
                weekAnalysis.add(analysis)
            }

            return@wrap weekAnalysis
        }
    }
}