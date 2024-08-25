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
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.CLASS_MINUTE_DURATION_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.MAX_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.MOVEMENT_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.PRACTICE_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.PRESENTATION_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.TEST_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.THEORY_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.TODO_PRIORITY_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.TODO_RATE
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.fetchAllTasks
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkComponents
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures

/**
 * @author Stanislav Aleshin on 13.06.2024.
 */
internal interface AnalysisInteractor {

    suspend fun fetchWeekAnalysis(weekTimeRange: TimeRange): FlowDomainResult<ScheduleFailures, List<DailyAnalysis>>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val todoRepository: TodoRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : AnalysisInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchWeekAnalysis(weekTimeRange: TimeRange) = eitherWrapper.wrapFlow {
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(targetUser).first().numberOfWeek
            val week = weekTimeRange.from.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)

            val baseSchedulesFlow = baseScheduleRepository.fetchSchedulesByVersion(weekTimeRange, week, targetUser)
            val customSchedulesFlow = customScheduleRepository.fetchSchedulesByTimeRange(weekTimeRange, targetUser)
            val todosFlow = todoRepository.fetchTodosByTimeRange(weekTimeRange, targetUser)
            val homeworksFlow = homeworksRepository.fetchHomeworksByTimeRange(weekTimeRange, targetUser)

            return@wrapFlow baseSchedulesFlow.flatMapLatest { baseSchedules ->
                customSchedulesFlow.flatMapLatest { customSchedules ->
                    homeworksFlow.flatMapLatest { homeworks ->
                        todosFlow.map { todos ->
                            val groupedHomeworks = homeworks.groupBy { it.deadline.startThisDay() }
                            val weekAnalysis = mutableListOf<DailyAnalysis>()

                            weekTimeRange.periodDates().forEach { instant ->
                                val customSchedule = customSchedules.find { it.date.equalsDay(instant) }
                                val baseSchedule = baseSchedules.find { it.dayOfWeek == instant.dateTime().dayOfWeek }
                                val analysis = fetchDailyAnalysis(
                                    date = instant,
                                    baseSchedule = baseSchedule,
                                    customSchedule = customSchedule,
                                    groupedHomeworks = groupedHomeworks,
                                    todos = todos,
                                )
                                weekAnalysis.add(analysis)
                            }

                            return@map weekAnalysis
                        }
                    }
                }
            }
        }

        private fun fetchDailyAnalysis(
            date: Instant,
            baseSchedule: BaseSchedule?,
            customSchedule: CustomSchedule?,
            groupedHomeworks: Map<Instant, List<Homework>>,
            todos: List<Todo>,
        ): DailyAnalysis {
            val classes = customSchedule?.classes ?: baseSchedule?.classes
            val movementMap = classes?.groupBy { it.organization }?.mapValues { classEntry ->
                classEntry.value.map { it.location }.toSet()
            }

            val dailyHomeworks = groupedHomeworks[date]
            val dailyTodos = todos.filter { it.deadline?.equalsDay(date) == true }

            val testsNumberAndRate = fetchNumberAndRateOfTests(dailyHomeworks)
            val classesNumberAndRate = fetchNumberAndRateOfClasses(classes)
            val movementsNumberAndRate = fetchNumberAndRateOfMovements(movementMap)
            val homeworksProgressAndRate = fetchProgressAndRateOfHomeworks(dailyHomeworks)
            val todosProgressAndRate = fetchProgressAndRateOfTodos(dailyTodos)

            val rateList = listOf(
                classesNumberAndRate.second,
                testsNumberAndRate.second,
                movementsNumberAndRate.second,
                homeworksProgressAndRate.second,
                todosProgressAndRate.second,
            )

            return DailyAnalysis(
                date = date,
                generalAssessment = rateList.sum() / MAX_RATE,
                numberOfClasses = classesNumberAndRate.first,
                numberOfTests = testsNumberAndRate.first,
                numberOfMovements = movementsNumberAndRate.first,
                homeworksProgress = homeworksProgressAndRate.first,
                todosProgress = todosProgressAndRate.first,
            )
        }

        private fun fetchProgressAndRateOfHomeworks(homeworks: List<Homework>?): Pair<List<Boolean>, Float> {
            val homeworkList = homeworks ?: emptyList()

            val homeworksProgress = homeworkList.map { it.isDone || it.completeDate != null }

            val numberOfTheories = homeworkList.sumOf { homework ->
                homework.theoreticalTasks.toHomeworkComponents().fetchAllTasks().size
            }
            val numberOfPractices = homeworkList.sumOf { homework ->
                homework.practicalTasks.toHomeworkComponents().fetchAllTasks().size
            }
            val numberOfPresentations = homeworkList.sumOf { homework ->
                homework.presentationTasks.toHomeworkComponents().fetchAllTasks().size
            }
            val homeworksRate = (numberOfTheories * THEORY_RATE) +
                (numberOfPractices * PRACTICE_RATE) +
                (numberOfPresentations * PRESENTATION_RATE)

            return Pair(homeworksProgress, homeworksRate)
        }

        private fun fetchProgressAndRateOfTodos(todos: List<Todo>): Pair<List<Boolean>, Float> {
            val todosProgress = todos.map { it.isDone || it.completeDate != null }
            val todosRate = todos.sumOf { todo ->
                if (todo.priority == TaskPriority.STANDARD) {
                    TODO_RATE
                } else {
                    TODO_PRIORITY_RATE
                }
            }

            return Pair(todosProgress, todosRate.toFloat())
        }

        private fun fetchNumberAndRateOfMovements(
            movementMap: Map<OrganizationShort, Set<ContactInfo?>>?
        ): Pair<Int, Float> {
            val numberOfMovements = movementMap?.toList()?.sumOf { it.second.size } ?: 0
            val movementsRate = numberOfMovements * MOVEMENT_RATE

            return Pair(numberOfMovements, movementsRate)
        }

        private fun fetchNumberAndRateOfClasses(classes: List<Class>?): Pair<Int, Float> {
            val numberOfClasses = classes?.size ?: 0
            val classesDuration = classes?.map { it.timeRange.periodDuration() }?.sumOf { it.minutes } ?: 0
            val classesRate = classesDuration * CLASS_MINUTE_DURATION_RATE

            return Pair(numberOfClasses, classesRate)
        }

        private fun fetchNumberAndRateOfTests(homeworks: List<Homework>?): Pair<Int, Float> {
            val numberOfTests = homeworks?.count { it.test != null } ?: 0
            val testsRate = numberOfTests * TEST_RATE

            return Pair(numberOfTests, testsRate)
        }
    }
}