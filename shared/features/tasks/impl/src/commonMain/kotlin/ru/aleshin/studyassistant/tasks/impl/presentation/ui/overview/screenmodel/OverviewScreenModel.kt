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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.shiftMinutes
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen.Homework
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen.Todo
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen.Homeworks
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureDIHolder
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksScreenProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewAction.UpdateCurrentDate
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect.NavigateToGlobal
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect.NavigateToLocal
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel.GoalWorkCommand.LoadGoals
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel.TodoWorkCommand.LoadTodos

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
internal class OverviewScreenModel(
    private val todoWorkProcessor: TodoWorkProcessor,
    private val goalWorkProcessor: GoalWorkProcessor,
    private val homeworksWorkProcessor: HomeworksWorkProcessor,
    private val screenProvider: TasksScreenProvider,
    private val dateManager: DateManager,
    stateCommunicator: OverviewStateCommunicator,
    effectCommunicator: OverviewEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<OverviewViewState, OverviewEvent, OverviewAction, OverviewEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(OverviewEvent.Init)
        }
    }

    override suspend fun WorkScope<OverviewViewState, OverviewAction, OverviewEffect>.handleEvent(
        event: OverviewEvent,
    ) {
        when (event) {
            is OverviewEvent.Init -> {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                sendAction(UpdateCurrentDate(currentDate))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(currentDate)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PROGRESS) {
                    val command = HomeworksWorkCommand.LoadHomeworksProgress(currentDate)
                    homeworksWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_TASKS) {
                    val command = LoadTodos(currentDate)
                    todoWorkProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_GOALS) {
                    val command = LoadGoals(currentDate)
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
                    val command = LoadGoals(date)
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
            is OverviewEvent.PauseGoalTime -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.PauseGoalTime(goal)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ResetGoalTime -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = GoalWorkCommand.ResetGoalTime(goal)
                    goalWorkProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.StartGoalTime -> with(event) {
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
            is OverviewEvent.NavigateToHomeworkEditor -> with(event) {
                val featureScreen = Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(NavigateToGlobal(screen))
            }
            is OverviewEvent.NavigateToTodoEditor -> with(event) {
                val featureScreen = Todo(todoId = todo?.uid)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(NavigateToGlobal(screen))
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
                val featureScreen = Homework(
                    homeworkId = null,
                    date = null,
                    subjectId = activeClass?.subject?.uid,
                    organizationId = activeClass?.organization?.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(NavigateToGlobal(screen))
            }
            is OverviewEvent.NavigateToHomeworks -> with(event) {
                val featureScreen = Homeworks(
                    targetDate = homework?.deadline?.startThisDay()?.toEpochMilliseconds(),
                )
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(NavigateToLocal(screen))
            }
            is OverviewEvent.NavigateToShare -> {
                val featureScreen = TasksScreen.Share
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(NavigateToLocal(screen))
            }
            is OverviewEvent.NavigateToTodos -> {
                val screen = screenProvider.provideFeatureScreen(TasksScreen.Todos)
                sendEffect(NavigateToLocal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: OverviewAction,
        currentState: OverviewViewState,
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
        GOAL_ACTION,
        TASK_ACTION,
    }
}

@Composable
internal fun Screen.rememberOverviewScreenModel(): OverviewScreenModel {
    val di = TasksFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<OverviewScreenModel>() }
}