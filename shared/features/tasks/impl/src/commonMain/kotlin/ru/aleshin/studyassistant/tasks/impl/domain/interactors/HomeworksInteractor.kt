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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startOfWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.tasks.DailyHomeworks
import ru.aleshin.studyassistant.core.domain.entities.tasks.DailyHomeworksStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkScope
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.convertToDetails
import ru.aleshin.studyassistant.core.domain.entities.tasks.fetchAllTasks
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.HomeworksCompleteProgress
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 20.06.2024.
 */
internal interface HomeworksInteractor {

    suspend fun addHomeworksGroup(homeworks: List<Homework>): UnitDomainResult<TasksFailures>
    suspend fun fetchHomeworksByTimeRange(
        timeRange: TimeRange
    ): FlowDomainResult<TasksFailures, Map<Instant, DailyHomeworks>>
    suspend fun fetchHomeworksProgress(targetDate: Instant): FlowDomainResult<TasksFailures, HomeworksCompleteProgress>
    suspend fun updateHomework(homework: Homework): UnitDomainResult<TasksFailures>
    suspend fun doHomework(homework: Homework): UnitDomainResult<TasksFailures>
    suspend fun skipHomework(homework: Homework): UnitDomainResult<TasksFailures>
    fun calculateHomeworkScope(homeworksMap: Map<Instant, DailyHomeworks>): HomeworkScope

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val goalsRepository: DailyGoalsRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: TasksEitherWrapper,
    ) : HomeworksInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addHomeworksGroup(homeworks: List<Homework>) = eitherWrapper.wrapUnit {
            homeworksRepository.addHomeworksGroup(homeworks, targetUser)
        }

        override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange) = eitherWrapper.wrapFlow {
            val ticker = dateManager.secondTicker()
            val goalsTimeRange = TimeRange(timeRange.from, timeRange.to.shiftDay(21))
            val shortGoalsFlow = goalsRepository.fetchShortDailyGoalsByTimeRange(goalsTimeRange, targetUser)
            val homeworksFlow = homeworksRepository.fetchHomeworksByTimeRange(timeRange, targetUser)

            return@wrapFlow combine(homeworksFlow, shortGoalsFlow, ticker) { homeworks, goals, _ ->
                val currentTime = dateManager.fetchCurrentInstant()
                val detailsHomeworks = homeworks.map { homework ->
                    val status = HomeworkStatus.calculate(
                        isDone = homework.isDone,
                        completeDate = homework.completeDate,
                        deadline = homework.deadline,
                        currentTime = currentTime,
                    )
                    return@map homework.convertToDetails(
                        status = status,
                        linkedGoal = goals.find { it.contentId == homework.uid },
                    )
                }
                val groupedDetailsHomeworks = detailsHomeworks.groupBy { homework ->
                    homework.deadline.startThisDay()
                }
                val dailyHomeworksMap = buildMap {
                    timeRange.periodDates().forEach { date ->
                        val homeworksByDate = groupedDetailsHomeworks[date] ?: emptyList()
                        val dailyHomeworks = DailyHomeworks(
                            dailyStatus = DailyHomeworksStatus.calculate(
                                targetDate = date,
                                currentDate = currentTime.startThisDay(),
                                homeworkStatuses = homeworksByDate.map { it.status }
                            ),
                            homeworks = homeworksByDate.groupBy { it.status },
                        )
                        put(date, dailyHomeworks)
                    }
                }
                return@combine dailyHomeworksMap
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchHomeworksProgress(targetDate: Instant) = eitherWrapper.wrapFlow {
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(targetUser).first().numberOfWeek

            val targetTimeRange = TimeRange(targetDate.startOfWeek(), targetDate.endOfWeek().shiftDay(1))
            val homeworksFlow = homeworksRepository.fetchHomeworksByTimeRange(targetTimeRange, targetUser)
            val completedHomeworksFlow = homeworksRepository.fetchCompletedHomeworksCount(targetUser)
            val overdueHomeworksFlow = homeworksRepository.fetchOverdueHomeworks(targetDate, targetUser)
            val activeLinkedHomeworksFlow = homeworksRepository.fetchActiveLinkedHomeworks(targetDate, targetUser)

            return@wrapFlow combine(
                completedHomeworksFlow,
                overdueHomeworksFlow,
                activeLinkedHomeworksFlow,
                homeworksFlow,
            ) { completedHomeworks, overdueHomeworks, activeLinkedHomeworks, homeworks ->
                val comingTimeRange = TimeRange(targetDate, targetDate.endThisDay().shiftDay(1))
                val weekTimeRange = targetDate.dateTime().weekTimeRange()

                val comingHomeworks = homeworks.filter { comingTimeRange.containsDate(it.deadline) }
                val comingHomeworksExecution = comingHomeworks.map { it.completeDate != null }
                val comingHomeworksProgress = if (comingHomeworksExecution.isNotEmpty()) {
                    comingHomeworksExecution.count { it }.toFloat() / comingHomeworksExecution.size
                } else {
                    0f
                }
                val weekHomeworks = homeworks.filter { weekTimeRange.containsDate(it.deadline) }
                val weekHomeworksExecution = weekHomeworks.map { it.completeDate != null }
                val weekHomeworksProgress = if (weekHomeworksExecution.isNotEmpty()) {
                    weekHomeworksExecution.count { it }.toFloat() / weekHomeworksExecution.size
                } else {
                    0f
                }

                val detachedActiveTasks = buildList {
                    activeLinkedHomeworks.forEach { homework ->
                        val homeworkDate = homework.deadline.startThisDay()
                        val homeworkNumberOfWeek = homeworkDate.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
                        val customScheduleByDate = customScheduleRepository.fetchScheduleByDate(homeworkDate, targetUser).first()
                        val classesByDate = if (customScheduleByDate?.classes != null) {
                            customScheduleByDate.classes
                        } else {
                            baseScheduleRepository.fetchScheduleByDate(homeworkDate, homeworkNumberOfWeek, targetUser).first()?.classes
                        }
                        if (classesByDate?.find { it.uid == homework.classId } == null) {
                            add(homework)
                        }
                    }
                }

                HomeworksCompleteProgress(
                    comingHomeworksExecution = comingHomeworksExecution,
                    comingHomeworksProgress = comingHomeworksProgress,
                    weekHomeworksExecution = weekHomeworksExecution,
                    weekHomeworksProgress = weekHomeworksProgress,
                    overdueTasks = overdueHomeworks,
                    detachedActiveTasks = detachedActiveTasks,
                    completedHomeworksCount = completedHomeworks,
                )
            }
        }

        override suspend fun updateHomework(homework: Homework) = eitherWrapper.wrapUnit {
            homeworksRepository.addOrUpdateHomework(homework, targetUser)
        }

        override suspend fun doHomework(homework: Homework) = eitherWrapper.wrapUnit {
            val currentTime = dateManager.fetchCurrentInstant()
            val linkedGoal = goalsRepository.fetchGoalByContentId(homework.uid, targetUser).first()
            val updatedHomework = homework.copy(isDone = true, completeDate = currentTime)
            if (linkedGoal != null && !linkedGoal.isDone) completeLinkedGoal(linkedGoal)

            homeworksRepository.addOrUpdateHomework(updatedHomework, targetUser)
        }

        override suspend fun skipHomework(homework: Homework) = eitherWrapper.wrapUnit {
            val currentTime = dateManager.fetchCurrentInstant()
            val linkedGoal = goalsRepository.fetchGoalByContentId(homework.uid, targetUser).first()
            val updatedHomework = homework.copy(isDone = false, completeDate = currentTime)
            if (linkedGoal != null && !linkedGoal.isDone) completeLinkedGoal(linkedGoal)

            homeworksRepository.addOrUpdateHomework(updatedHomework, targetUser)
        }

        override fun calculateHomeworkScope(homeworksMap: Map<Instant, DailyHomeworks>): HomeworkScope {
            return HomeworkScope(
                theoreticalTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.values.toList().extractAllItem().sumOf { homework ->
                        return@sumOf homework.theoreticalTasks.components.fetchAllTasks().size
                    }
                },
                practicalTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.values.toList().extractAllItem().sumOf { homework ->
                        return@sumOf homework.practicalTasks.components.fetchAllTasks().size
                    }
                },
                presentationTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.values.toList().extractAllItem().sumOf { homework ->
                        return@sumOf homework.presentationTasks.components.fetchAllTasks().size
                    }
                },
            )
        }

        private suspend fun completeLinkedGoal(linkedGoal: Goal) {
            val currentTime = dateManager.fetchCurrentInstant()
            val updatedGoal = linkedGoal.copy(
                time = when (linkedGoal.time) {
                    is GoalTime.Stopwatch -> with(linkedGoal.time as GoalTime.Stopwatch) {
                        val stopTime = startTimePoint.toEpochMilliseconds()
                        val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                        return@with copy(
                            pastStopTime = pastStopTime + timeAfterStop,
                            isActive = false,
                        )
                    }
                    is GoalTime.Timer -> with(linkedGoal.time as GoalTime.Timer) {
                        val stopTime = startTimePoint.toEpochMilliseconds()
                        val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                        return@with copy(
                            pastStopTime = pastStopTime + timeAfterStop,
                            isActive = false,
                        )
                    }
                    is GoalTime.None -> GoalTime.None
                },
                isDone = true,
                completeDate = currentTime,
            )
            goalsRepository.addOrUpdateGoal(updatedGoal, targetUser)
        }
    }
}