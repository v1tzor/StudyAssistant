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

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.mapWith
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.goals.DailyGoalsProgress
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTimeDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType.HOMEWORK
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType.TODO
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.GoalCreateModel
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 02.06.2025.
 */
internal interface GoalsInteractor {

    suspend fun addGoal(createModel: GoalCreateModel): UnitDomainResult<TasksFailures>
    suspend fun fetchGoalsByDate(date: Instant): FlowDomainResult<TasksFailures, List<GoalDetails>>
    suspend fun fetchGoalsProgressByTimeRange(
        timeRange: TimeRange
    ): FlowDomainResult<TasksFailures, Map<Instant, DailyGoalsProgress>>
    suspend fun updateGoals(goals: List<Goal>): UnitDomainResult<TasksFailures>
    suspend fun completeOrCancelGoal(goal: Goal): UnitDomainResult<TasksFailures>
    suspend fun deleteGoal(goal: GoalShort): UnitDomainResult<TasksFailures>

    class Base(
        private val goalsRepository: DailyGoalsRepository,
        private val homeworksRepository: HomeworksRepository,
        private val todoRepository: TodoRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: TasksEitherWrapper,
    ) : GoalsInteractor {

        override suspend fun addGoal(createModel: GoalCreateModel) = eitherWrapper.wrapUnit {
            val dailyGoals = goalsRepository.fetchDailyGoalsByDate(createModel.date).first()
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val maxNumber = dailyGoals.maxOfOrNull { it.number } ?: 0

            val createdGoal = Goal(
                uid = randomUUID(),
                contentType = createModel.contentType,
                contentHomework = createModel.contentHomework,
                contentTodo = createModel.contentTodo,
                number = maxNumber.inc(),
                targetDate = createModel.date,
                desiredTime = createModel.desiredTime,
                updatedAt = updatedAt,
            )
            goalsRepository.addOrUpdateGoal(createdGoal)
        }

        override suspend fun fetchGoalsByDate(date: Instant) = eitherWrapper.wrapFlow {
            val ticker = dateManager.secondTicker()
            val goalsFlow = goalsRepository.fetchDailyGoalsByDate(date)

            return@wrapFlow combine(goalsFlow, ticker) { goals, _ ->
                val currentTime = dateManager.fetchCurrentInstant()
                goals.mapWith {
                    var makeItDone = false

                    val goalTimeDetails = when (time) {
                        is GoalTime.Stopwatch -> with(time as GoalTime.Stopwatch) {
                            val elapsedTime = if (isActive) {
                                val duration = currentTime.toEpochMilliseconds() - startTimePoint.toEpochMilliseconds()
                                pastStopTime + duration
                            } else {
                                pastStopTime
                            }
                            return@with GoalTimeDetails.Stopwatch(
                                pastStopTime = pastStopTime,
                                startTimePoint = startTimePoint,
                                elapsedTime = elapsedTime,
                                progress = if (desiredTime != null && desiredTime != 0L) {
                                    elapsedTime / desiredTime!!.toFloat()
                                } else {
                                    null
                                },
                                isActive = isActive,
                            )
                        }

                        is GoalTime.Timer -> with(time as GoalTime.Timer) {
                            val leftTime = if (isActive) {
                                val duration = currentTime.toEpochMilliseconds() - startTimePoint.toEpochMilliseconds()
                                val result = targetTime - (pastStopTime + duration)
                                if (completeAfterTimeElapsed && result <= 0) makeItDone = true
                                result
                            } else {
                                targetTime - pastStopTime
                            }
                            GoalTimeDetails.Timer(
                                targetTime = targetTime,
                                pastStopTime = if (makeItDone) targetTime else pastStopTime,
                                startTimePoint = startTimePoint,
                                leftTime = if (makeItDone) 0 else leftTime,
                                progress = if (targetTime != 0L) {
                                    (targetTime - leftTime) / targetTime.toFloat()
                                } else {
                                    0f
                                },
                                isActive = if (makeItDone) false else isActive,
                            )
                        }

                        GoalTime.None -> GoalTimeDetails.None
                    }

                    if (makeItDone) completeOrCancelGoal(this)

                    return@mapWith GoalDetails(
                        uid = uid,
                        contentType = contentType,
                        contentHomework = contentHomework,
                        contentTodo = contentTodo,
                        number = number,
                        targetDate = targetDate,
                        desiredTime = desiredTime,
                        time = goalTimeDetails,
                        completeAfterTimeElapsed = completeAfterTimeElapsed,
                        isDone = if (makeItDone) true else isDone,
                        completeDate = if (makeItDone) currentTime else completeDate,
                        updatedAt = updatedAt,
                    )
                }.sortedBy { goal ->
                    goal.number
                }
            }
        }

        override suspend fun fetchGoalsProgressByTimeRange(timeRange: TimeRange) = eitherWrapper.wrapFlow {
            return@wrapFlow goalsRepository.fetchDailyGoalsByTimeRange(timeRange).map { goals ->
                val goalsByDate = goals.groupBy { it.targetDate.startThisDay() }
                val goalsProgress = goalsByDate.mapValues { entry ->
                    val homeworkGoals = entry.value.filter { it.contentType == HOMEWORK }.map {
                        it.isDone && it.completeDate != null
                    }
                    val todoGoals = entry.value.filter { it.contentType == TODO }.map {
                        it.isDone && it.completeDate != null
                    }
                    val allGoals = homeworkGoals + todoGoals
                    DailyGoalsProgress(
                        goalsCount = entry.value.size,
                        homeworkGoals = homeworkGoals,
                        todoGoals = todoGoals,
                        progress = allGoals.count { it }.toFloat() / allGoals.size,
                    )
                }
                return@map goalsProgress
            }
        }

        override suspend fun updateGoals(goals: List<Goal>) = eitherWrapper.wrapUnit {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val updatedGoals = goals.map { it.copy(updatedAt = updatedAt) }
            goalsRepository.addDailyDailyGoals(updatedGoals)
        }

        override suspend fun completeOrCancelGoal(goal: Goal) = eitherWrapper.wrapUnit {
            val currentTime = dateManager.fetchCurrentInstant()
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            if (!goal.isDone) {
                val completedGoal = goal.copy(
                    time = when (goal.time) {
                        is GoalTime.Stopwatch -> with(goal.time as GoalTime.Stopwatch) {
                            val stopTime = startTimePoint.toEpochMilliseconds()
                            val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                            return@with copy(
                                pastStopTime = pastStopTime + timeAfterStop,
                                isActive = false,
                            )
                        }
                        is GoalTime.Timer -> with(goal.time as GoalTime.Timer) {
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
                    updatedAt = updatedAt,
                )
                when (goal.contentType) {
                    HOMEWORK -> goal.contentHomework?.let { goalHomework ->
                        val actualHomework = homeworksRepository.fetchHomeworkById(goalHomework.uid)
                        val completedHomework = actualHomework.first()?.copy(
                            isDone = true,
                            completeDate = currentTime,
                            updatedAt = updatedAt,
                        )
                        if (completedHomework != null) {
                            homeworksRepository.addOrUpdateHomework(completedHomework)
                        }
                    }
                    TODO -> goal.contentTodo?.let { goalTodo ->
                        val actualTodo = todoRepository.fetchTodoById(goalTodo.uid)
                        val completedTodo = actualTodo.first()?.copy(
                            isDone = true,
                            completeDate = currentTime,
                            updatedAt = updatedAt,
                        )
                        if (completedTodo != null) {
                            todoRepository.addOrUpdateTodo(completedTodo)
                        }
                    }
                }
                goalsRepository.addOrUpdateGoal(completedGoal)
            } else {
                val canceledGoal = goal.copy(
                    time = when (goal.time) {
                        is GoalTime.Stopwatch -> (goal.time as GoalTime.Stopwatch).copy(
                            pastStopTime = 0L,
                            startTimePoint = currentTime,
                            isActive = false,
                        )
                        is GoalTime.Timer -> (goal.time as GoalTime.Timer).copy(
                            pastStopTime = 0L,
                            startTimePoint = currentTime,
                            isActive = false,
                        )
                        is GoalTime.None -> GoalTime.None
                    },
                    isDone = false,
                    completeDate = null,
                    updatedAt = updatedAt,
                )
                when (goal.contentType) {
                    HOMEWORK -> goal.contentHomework?.let { goalHomework ->
                        val actualHomework = homeworksRepository.fetchHomeworkById(goalHomework.uid)
                        val completedHomework = actualHomework.first()?.copy(
                            isDone = false,
                            completeDate = null,
                            updatedAt = updatedAt,
                        )
                        if (completedHomework != null) {
                            homeworksRepository.addOrUpdateHomework(completedHomework)
                        }
                    }
                    TODO -> goal.contentTodo?.let { goalTodo ->
                        val actualTodo = todoRepository.fetchTodoById(goalTodo.uid)
                        val completedTodo = actualTodo.first()?.copy(
                            isDone = false,
                            completeDate = null,
                            updatedAt = updatedAt,
                        )
                        if (completedTodo != null) {
                            todoRepository.addOrUpdateTodo(completedTodo)
                        }
                    }
                }
                goalsRepository.addOrUpdateGoal(canceledGoal)
            }
        }

        override suspend fun deleteGoal(goal: GoalShort) = eitherWrapper.wrap {
            val dailyGoals = goalsRepository.fetchDailyGoalsByDate(goal.targetDate).first()
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            goalsRepository.deleteGoal(goal.uid)
            if (dailyGoals.size > 1) {
                val updatedGoals = dailyGoals.filter { it.uid != goal.uid }.map { targetGoal ->
                    val newNumber = if (targetGoal.number < goal.number) targetGoal.number else targetGoal.number - 1
                    targetGoal.copy(number = newNumber, updatedAt = updatedAt)
                }
                goalsRepository.addDailyDailyGoals(updatedGoals)
            }
        }
    }
}