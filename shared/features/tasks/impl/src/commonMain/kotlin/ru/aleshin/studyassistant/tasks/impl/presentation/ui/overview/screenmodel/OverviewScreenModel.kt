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
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureDIHolder
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksScreenProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewViewState

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
internal class OverviewScreenModel(
    private val workProcessor: OverviewWorkProcessor,
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
            is OverviewEvent.Init, OverviewEvent.Refresh -> {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                sendAction(OverviewAction.UpdateCurrentDate(currentDate))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = OverviewWorkCommand.LoadHomeworks(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ERRORS) {
                    val command = OverviewWorkCommand.LoadHomeworkErrors(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_TASKS) {
                    val command = OverviewWorkCommand.LoadTodos(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ACTIVE_SCHEDULE) {
                    val command = OverviewWorkCommand.LoadActiveSchedule(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.DoHomework -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedHomework = homework.copy(isDone = true, completeDate = currentTime)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = OverviewWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.SkipHomework -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedHomework = homework.copy(isDone = false, completeDate = currentTime)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = OverviewWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.RepeatHomework -> with(event) {
                val updatedHomework = homework.copy(isDone = false, completeDate = null)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = OverviewWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.UpdateTodoDone -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedTodo = todo.copy(
                    isDone = isDone,
                    completeDate = if (isDone) currentTime else null,
                )
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = OverviewWorkCommand.UpdateTodo(updatedTodo)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.NavigateToHomeworkEditor -> with(event) {
                val featureScreen = EditorScreen.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(OverviewEffect.NavigateToGlobal(screen))
            }
            is OverviewEvent.NavigateToTodoEditor -> with(event) {
                val featureScreen = EditorScreen.Todo(todoId = todo?.uid)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(OverviewEffect.NavigateToGlobal(screen))
            }
            is OverviewEvent.AddHomeworkInEditor -> with(state()) {
                val currentTime = dateManager.fetchCurrentInstant()
                val activeClass = activeSchedule?.classes?.find {
                    it.timeRange.containsTime(currentTime)
                }
                val featureScreen = EditorScreen.Homework(
                    homeworkId = null,
                    date = null,
                    subjectId = activeClass?.subject?.uid,
                    organizationId = activeClass?.organization?.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(OverviewEffect.NavigateToGlobal(screen))
            }
            is OverviewEvent.NavigateToHomeworks -> with(event) {
                val featureScreen = TasksScreen.Homeworks(
                    targetDate = homework?.deadline?.startThisDay()?.toEpochMilliseconds(),
                )
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(OverviewEffect.NavigateToLocal(screen))
            }
            is OverviewEvent.NavigateToTodos -> {
                val screen = screenProvider.provideFeatureScreen(TasksScreen.Todos)
                sendEffect(OverviewEffect.NavigateToLocal(screen))
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
        is OverviewAction.UpdateTaskErrors -> currentState.copy(
            homeworkErrors = action.homeworkErrors,
            todoErrors = action.todoErrors,
            isLoadingErrors = false,
        )
        is OverviewAction.UpdateTodos -> currentState.copy(
            todos = action.todos,
            isLoadingTasks = false,
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
        is OverviewAction.UpdateErrorsLoading -> currentState.copy(
            isLoadingErrors = action.isLoading,
        )
        is OverviewAction.UpdateTasksLoading -> currentState.copy(
            isLoadingTasks = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_HOMEWORKS, LOAD_ACTIVE_SCHEDULE, LOAD_ERRORS, LOAD_TASKS, HOMEWORK_ACTION,
    }
}

@Composable
internal fun Screen.rememberOverviewScreenModel(): OverviewScreenModel {
    val di = TasksFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<OverviewScreenModel>() }
}