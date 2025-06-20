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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.GoalsInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalTimeDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect

/**
 * @author Stanislav Aleshin on 11.06.2025.
 */
internal interface GoalWorkProcessor : FlowWorkProcessor<GoalWorkCommand, OverviewAction, OverviewEffect> {

    class Base(
        private val goalsInteractor: GoalsInteractor,
        private val dateManager: DateManager,
    ) : GoalWorkProcessor {

        override suspend fun work(command: GoalWorkCommand) = when (command) {
            is GoalWorkCommand.LoadGoals -> loadGoals(command.selectedDate)
            is GoalWorkCommand.ScheduleGoal -> scheduleGoalWork(command.createModel)
            is GoalWorkCommand.CompleteGoal -> completeGoalWork(command.goals, command.target)
            is GoalWorkCommand.DeleteGoal -> deleteGoalWork(command.goal)
            is GoalWorkCommand.SetNewGoalNumbers -> setNewGoalNumbersWork(command.goals)
            is GoalWorkCommand.StartGoalTime -> startGoalTimeWork(command.goal)
            is GoalWorkCommand.PauseGoalTime -> pauseGoalTimeWork(command.goal)
            is GoalWorkCommand.ResetGoalTime -> resetGoalTimeWork(command.goal)
            is GoalWorkCommand.ChangeGoalTimeType -> changeGoalTimeTypeWork(command.goal, command.type)
            is GoalWorkCommand.ChangeGoalDesiredTime -> changeGoalDesiredTimeWork(command.goal, command.time)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadGoals(selectedDate: Instant) = flow {
            val goalsFlow = goalsInteractor.fetchGoalsByDate(selectedDate).map { goalEither ->
                goalEither.mapRight { goals -> goals.map { it.mapToUi() } }
            }
            val goalsProgressFlow = goalsInteractor.fetchGoalsProgressByTimeRange(
                timeRange = TimeRange(selectedDate, selectedDate.shiftDay(3))
            ).map { progressEither ->
                progressEither.mapRight { progressMap -> progressMap.mapValues { it.value.mapToUi() } }
            }
            goalsFlow.combineWithResult(
                secondFlow = goalsProgressFlow,
                onError = { OverviewEffect.ShowError(it) },
                onData = { goals, goalsProgress ->
                    OverviewAction.UpdateGoals(selectedDate, goals, goalsProgress)
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateGoalsLoading(true)))
        }

        private fun scheduleGoalWork(createModel: GoalCreateModelUi) = flow {
            goalsInteractor.addGoal(createModel.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }

        private fun completeGoalWork(goals: List<GoalDetailsUi>, target: GoalDetailsUi) = flow {
            val actualGoal = goals.find { it.uid == target.uid } ?: return@flow
            goalsInteractor.completeOrCancelGoal(actualGoal.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }
        private fun deleteGoalWork(goal: GoalShortUi) = flow {
            goalsInteractor.deleteGoal(goal.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }

        private fun setNewGoalNumbersWork(goals: List<GoalDetailsUi>) = flow {
            val updatedGoals = mutableListOf<Goal>().apply {
                goals.forEachIndexed { index, goal ->
                    add(goal.copy(number = index.inc()).mapToDomain())
                }
            }
            goalsInteractor.updateGoals(updatedGoals).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }

        private fun startGoalTimeWork(goal: GoalDetailsUi) = flow {
            val currentTime = dateManager.fetchCurrentInstant()
            val updatedGoal = goal.copy(
                time = when (goal.time) {
                    is GoalTimeDetailsUi.Stopwatch -> goal.time.copy(
                        startTimePoint = currentTime,
                        isActive = true,
                    )
                    is GoalTimeDetailsUi.Timer -> goal.time.copy(
                        startTimePoint = currentTime,
                        isActive = true,
                    )
                    is GoalTimeDetailsUi.None -> GoalTimeDetailsUi.None
                },
            )
            goalsInteractor.updateGoals(listOf(updatedGoal.mapToDomain())).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }

        private fun pauseGoalTimeWork(goal: GoalDetailsUi) = flow {
            val currentTime = dateManager.fetchCurrentInstant()
            val updatedGoal = goal.copy(
                time = when (goal.time) {
                    is GoalTimeDetailsUi.Stopwatch -> {
                        val stopTime = goal.time.startTimePoint.toEpochMilliseconds()
                        val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                        goal.time.copy(
                            pastStopTime = goal.time.pastStopTime + timeAfterStop,
                            isActive = false,
                        )
                    }
                    is GoalTimeDetailsUi.Timer -> {
                        val stopTime = goal.time.startTimePoint.toEpochMilliseconds()
                        val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                        goal.time.copy(
                            pastStopTime = goal.time.pastStopTime + timeAfterStop,
                            isActive = false,
                        )
                    }
                    is GoalTimeDetailsUi.None -> GoalTimeDetailsUi.None
                },
            )
            goalsInteractor.updateGoals(listOf(updatedGoal.mapToDomain())).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }

        private fun resetGoalTimeWork(goal: GoalDetailsUi) = flow {
            val currentTime = dateManager.fetchCurrentInstant()
            val updatedGoal = goal.copy(
                time = when (goal.time) {
                    is GoalTimeDetailsUi.Stopwatch -> goal.time.copy(
                        pastStopTime = 0L,
                        elapsedTime = 0L,
                        startTimePoint = currentTime,
                        isActive = false,
                    )
                    is GoalTimeDetailsUi.Timer -> goal.time.copy(
                        pastStopTime = 0L,
                        startTimePoint = currentTime,
                        isActive = false,
                        leftTime = goal.time.targetTime,
                    )
                    is GoalTimeDetailsUi.None -> GoalTimeDetailsUi.None
                },
            )
            goalsInteractor.updateGoals(listOf(updatedGoal.mapToDomain())).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }

        private fun changeGoalTimeTypeWork(goal: GoalDetailsUi, type: GoalTime.Type) = flow {
            val currentTime = dateManager.fetchCurrentInstant()
            val updatedGoal = goal.copy(
                time = when (type) {
                    GoalTime.Type.TIMER -> GoalTimeDetailsUi.Timer(
                        targetTime = goal.desiredTime ?: 0L,
                        startTimePoint = currentTime,
                        leftTime = goal.desiredTime ?: 0L,
                        progress = 0f,
                        isActive = false,
                    )
                    GoalTime.Type.STOPWATCH -> GoalTimeDetailsUi.Stopwatch(
                        startTimePoint = currentTime,
                        isActive = false,
                    )
                    GoalTime.Type.NONE -> GoalTimeDetailsUi.None
                },
            )
            goalsInteractor.updateGoals(listOf(updatedGoal.mapToDomain())).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }

        private fun changeGoalDesiredTimeWork(goal: GoalDetailsUi, time: Millis?) = flow {
            val updatedGoal = goal.copy(
                time = when (goal.time) {
                    is GoalTimeDetailsUi.Stopwatch -> goal.time
                    is GoalTimeDetailsUi.Timer -> goal.time.copy(
                        targetTime = time ?: goal.time.targetTime
                    )
                    is GoalTimeDetailsUi.None -> goal.time
                },
                desiredTime = time,
            )
            goalsInteractor.updateGoals(listOf(updatedGoal.mapToDomain())).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }
    }
}

internal sealed class GoalWorkCommand : WorkCommand {
    data class LoadGoals(val selectedDate: Instant) : GoalWorkCommand()
    data class ScheduleGoal(val createModel: GoalCreateModelUi) : GoalWorkCommand()
    data class SetNewGoalNumbers(val goals: List<GoalDetailsUi>) : GoalWorkCommand()
    data class CompleteGoal(val goals: List<GoalDetailsUi>, val target: GoalDetailsUi) : GoalWorkCommand()
    data class DeleteGoal(val goal: GoalShortUi) : GoalWorkCommand()
    data class StartGoalTime(val goal: GoalDetailsUi) : GoalWorkCommand()
    data class PauseGoalTime(val goal: GoalDetailsUi) : GoalWorkCommand()
    data class ResetGoalTime(val goal: GoalDetailsUi) : GoalWorkCommand()
    data class ChangeGoalTimeType(val goal: GoalDetailsUi, val type: GoalTime.Type) : GoalWorkCommand()
    data class ChangeGoalDesiredTime(val goal: GoalDetailsUi, val time: Millis?) : GoalWorkCommand()
}