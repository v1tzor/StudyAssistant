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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.shiftMinutes
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewState

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
internal class OverviewComposeStore(
    private val todoWorkProcessor: TodoWorkProcessor,
    private val goalWorkProcessor: GoalWorkProcessor,
    private val homeworksWorkProcessor: HomeworksWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<OverviewState>,
    effectCommunicator: EffectCommunicator<OverviewEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<OverviewState, OverviewEvent, OverviewAction, OverviewEffect, OverviewOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(OverviewEvent.Init)
    }

    override suspend fun WorkScope<OverviewState, OverviewAction, OverviewEffect, OverviewOutput>.handleEvent(
        event: OverviewEvent,
    ) {
        when (event) {
            is OverviewEvent.Init -> with(state) {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                sendAction(OverviewAction.UpdateCurrentDate(currentDate))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(currentDate)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PROGRESS) {
                    val command = HomeworksWorkCommand.LoadHomeworksProgress(currentDate)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_TASKS) {
                    val command = TodoWorkCommand.LoadTodos(currentDate)
                    todoWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_GOALS) {
                    val command = GoalWorkCommand.LoadGoals(selectedGoalsDate)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ACTIVE_SCHEDULE) {
                    val command = HomeworksWorkCommand.LoadActiveSchedule(currentDate)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SHARE) {
                    val command = HomeworksWorkCommand.LoadSharedHomeworks
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_USER_STATUS) {
                    val command = HomeworksWorkCommand.LoadPaidUserStatus
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.DoHomework -> with(event) {
                launchBackgroundWork(BackgroundKey.TASK_ACTION) {
                    val command = HomeworksWorkCommand.DoHomework(homework)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.SkipHomework -> with(event) {
                launchBackgroundWork(BackgroundKey.TASK_ACTION) {
                    val command = HomeworksWorkCommand.SkipHomework(homework)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.RepeatHomework -> with(event) {
                launchBackgroundWork(BackgroundKey.TASK_ACTION) {
                    val command = HomeworksWorkCommand.RepeatHomework(homework)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.SelectedGoalsDate -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_GOALS) {
                    val command = GoalWorkCommand.LoadGoals(date)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.SetNewGoalNumbers -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.SetNewGoalNumbers(goals)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.CompleteGoal -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.CompleteGoal(state().dailyGoals, goal)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.DeleteGoal -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.DeleteGoal(goal)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ChangeGoalDesiredTime -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.ChangeGoalDesiredTime(goal, time)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ChangeGoalTimeType -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.ChangeGoalTimeType(goal, type)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ClickPauseGoalTime -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.PauseGoalTime(goal)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ClickResetGoalTime -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.ResetGoalTime(goal)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ClickStartGoalTime -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.StartGoalTime(goal)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ScheduleGoal -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.ScheduleGoal(createModel)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.UpdateTodoDone -> with(event) {
                launchBackgroundWork(BackgroundKey.TASK_ACTION) {
                    val command = TodoWorkCommand.UpdateTodoDone(todo)
                    todoWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ShareHomeworks -> with(event) {
                launchBackgroundWork(BackgroundKey.TASK_ACTION) {
                    val command = HomeworksWorkCommand.ShareHomeworks(sentMediatedHomeworks)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ClickEditHomework -> with(event) {
                val config = EditorConfig.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                consumeOutput(OverviewOutput.NavigateToHomeworkEditor(config))
            }
            is OverviewEvent.ClickEditTodo -> with(event) {
                val config = EditorConfig.Todo(todoId = todo?.uid)
                consumeOutput(OverviewOutput.NavigateToTodoEditor(config))
            }
            is OverviewEvent.AddHomeworkInEditor -> with(state()) {
                val currentTime = dateManager.fetchCurrentInstant()
                val activeClass = if (activeSchedule != null && activeSchedule.classes.isNotEmpty()) {
                    val dailyTimeRange = TimeRange(
                        from = activeSchedule.classes.first().timeRange.from,
                        to = activeSchedule.classes.last().timeRange.to.shiftMinutes(10),
                    )
                    if (dailyTimeRange.containsTime(currentTime)) {
                        activeSchedule.classes.findLast { classModel ->
                            val firstFilter = classModel.timeRange.to.dateTime().time < currentTime.dateTime().time
                            val secondFilter = classModel.timeRange.containsTime(currentTime)
                            return@findLast firstFilter || secondFilter
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
                val config = EditorConfig.Homework(
                    homeworkId = null,
                    date = null,
                    subjectId = activeClass?.subject?.uid,
                    organizationId = activeClass?.organization?.uid,
                )
                consumeOutput(OverviewOutput.NavigateToHomeworkEditor(config))
            }
            is OverviewEvent.ClickHomework -> with(event) {
                val targetDate = homework?.deadline?.startThisDay()?.toEpochMilliseconds()
                consumeOutput(OverviewOutput.NavigateToHomeworks(targetDate))
            }
            is OverviewEvent.ClickShowAllSharedHomeworks -> {
                consumeOutput(OverviewOutput.NavigateToShareHomeworks)
            }
            is OverviewEvent.ClickShowAllTodo -> {
                consumeOutput(OverviewOutput.NavigateToTodo)
            }
            is OverviewEvent.ClickPaidFunction -> {
                consumeOutput(OverviewOutput.NavigateToBilling)
            }
        }
    }

    override suspend fun reduce(
        action: OverviewAction,
        currentState: OverviewState,
    ) = when (action) {
        is OverviewAction.UpdateHomeworks -> currentState.copy(
            homeworks = action.homeworks,
            homeworksScope = action.homeworkScope,
            isLoadingHomeworks = false,
        )
        is OverviewAction.UpdateTodos -> currentState.copy(
            groupedTodos = action.groupedTodos,
            isLoadingTasks = false,
        )
        is OverviewAction.UpdateGoals -> currentState.copy(
            selectedGoalsDate = action.selectedGoalsDate,
            dailyGoals = action.dailyGoals,
            goalsProgress = action.goalsProgress,
            isLoadingGoals = false,
        )
        is OverviewAction.UpdateHomeworksProgress -> currentState.copy(
            homeworksProgress = action.homeworkProgress,
            isLoadingHomeworksProgress = false,
        )
        is OverviewAction.UpdateSharedHomeworks -> currentState.copy(
            sharedHomeworks = action.homeworks,
            friends = action.friends,
            isLoadingShare = false,
        )
        is OverviewAction.UpdateActiveSchedule -> currentState.copy(
            activeSchedule = action.activeSchedule,
        )
        is OverviewAction.UpdateCurrentDate -> currentState.copy(
            currentDate = action.date,
        )
        is OverviewAction.UpdateUserPaidStatus -> currentState.copy(
            isPaidUser = action.isPaidUser,
        )
        is OverviewAction.UpdateHomeworksLoading -> currentState.copy(
            isLoadingHomeworks = action.isLoading,
        )
        is OverviewAction.UpdateHomeworksProgressLoading -> currentState.copy(
            isLoadingHomeworksProgress = action.isLoading,
        )
        is OverviewAction.UpdateTasksLoading -> currentState.copy(
            isLoadingTasks = action.isLoading,
        )
        is OverviewAction.UpdateShareLoading -> currentState.copy(
            isLoadingShare = action.isLoading,
        )
        is OverviewAction.UpdateGoalsLoading -> currentState.copy(
            isLoadingGoals = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_HOMEWORKS,
        LOAD_ACTIVE_SCHEDULE,
        LOAD_GOALS,
        LOAD_SHARE,
        LOAD_PROGRESS,
        LOAD_TASKS,
        LOAD_PAID_USER_STATUS,
        GOAL_ACTION,
        TASK_ACTION,
    }

     class Factory(
         private val todoWorkProcessor: TodoWorkProcessor,
         private val goalWorkProcessor: GoalWorkProcessor,
         private val homeworksWorkProcessor: HomeworksWorkProcessor,
         private val dateManager: DateManager,
         private val coroutineManager: CoroutineManager,
     ) : BaseOnlyOutComposeStore.Factory<OverviewComposeStore, OverviewState> {

         override fun create(savedState: OverviewState): OverviewComposeStore {
             return OverviewComposeStore(
                 todoWorkProcessor = todoWorkProcessor,
                 goalWorkProcessor = goalWorkProcessor,
                 homeworksWorkProcessor = homeworksWorkProcessor,
                 dateManager = dateManager,
                 stateCommunicator = StateCommunicator.Default(savedState),
                 effectCommunicator = EffectCommunicator.Default(),
                 coroutineManager = coroutineManager,
             )
         }
     }
}