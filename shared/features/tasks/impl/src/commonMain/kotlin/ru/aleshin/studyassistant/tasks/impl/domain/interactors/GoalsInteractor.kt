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
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.mapWith
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.goals.DailyGoalsProgress
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTimeDetails
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType.HOMEWORK
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType.TODO
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 02.06.2025.
 */
internal interface GoalsInteractor {

    suspend fun fetchGoalsByDate(date: Instant): FlowDomainResult<TasksFailures, List<GoalDetails>>
    suspend fun fetchGoalsProgressByTimeRange(timeRange: TimeRange): FlowDomainResult<TasksFailures, Map<Instant, DailyGoalsProgress>>
    suspend fun updateGoal(goal: Goal): UnitDomainResult<TasksFailures>

    class Base(
        private val goalsRepository: DailyGoalsRepository,
        private val dateManager: DateManager,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: TasksEitherWrapper,
    ) : GoalsInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchGoalsByDate(date: Instant) = eitherWrapper.wrapFlow {
            val ticker = dateManager.secondTicker()
            val goalsFlow = goalsRepository.fetchDailyGoalsByDate(date, targetUser)

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
                                isActive = if (makeItDone) false else isActive,
                            )
                        }
                        GoalTime.None -> GoalTimeDetails.None
                    }

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
                    )
                }.sortedBy { goal ->
                    goal.number
                }
            }
        }

        override suspend fun fetchGoalsProgressByTimeRange(timeRange: TimeRange) = eitherWrapper.wrapFlow {
            return@wrapFlow goalsRepository.fetchDailyGoalsByTimeRange(timeRange, targetUser).map { goals ->
                val goalsByDate = goals.groupBy { it.targetDate }
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
                        progress = allGoals.count().toFloat() / allGoals.size,
                    )
                }
                return@map goalsProgress
            }
        }

        override suspend fun updateGoal(goal: Goal) = eitherWrapper.wrapUnit {
            goalsRepository.addOrUpdateGoal(goal, targetUser)
        }
    }
}