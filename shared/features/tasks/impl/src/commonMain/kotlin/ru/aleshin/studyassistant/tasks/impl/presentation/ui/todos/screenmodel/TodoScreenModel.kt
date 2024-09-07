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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startOfWeek
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureDIHolder
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksScreenProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoViewState

/**
 * @author Stanislav Aleshin on 28.07.2024
 */
internal class TodoScreenModel(
    private val workProcessor: TodoWorkProcessor,
    private val screenProvider: TasksScreenProvider,
    private val dateManager: DateManager,
    stateCommunicator: TodoStateCommunicator,
    effectCommunicator: TodoEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<TodoViewState, TodoEvent, TodoAction, TodoEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(TodoEvent.Init)
        }
    }

    override suspend fun WorkScope<TodoViewState, TodoAction, TodoEffect>.handleEvent(
        event: TodoEvent,
    ) {
        when (event) {
            is TodoEvent.Init -> {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val targetTimeRange = TimeRange(
                    from = currentDate.startOfWeek().shiftWeek(-1),
                    to = currentDate.endOfWeek().shiftWeek(+1),
                )
                sendAction(TodoAction.UpdateTimeRange(targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_TODOS) {
                    val command = TodoWorkCommand.LoadTodos(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.CurrentTimeRange -> {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val targetTimeRange = TimeRange(
                    from = currentDate.startOfWeek().shiftWeek(-1),
                    to = currentDate.endOfWeek().shiftWeek(+1),
                )
                sendAction(TodoAction.UpdateTimeRange(targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_TODOS) {
                    val command = TodoWorkCommand.LoadTodos(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.NextTimeRange -> with(state()) {
                val currentTimeRange = checkNotNull(selectedTimeRange)
                val targetTimeRange = TimeRange(
                    from = currentTimeRange.to,
                    to = currentTimeRange.to.shiftWeek(+3),
                )
                sendAction(TodoAction.UpdateTimeRange(targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_TODOS) {
                    val command = TodoWorkCommand.LoadTodos(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.PreviousTimeRange -> with(state()) {
                val currentTimeRange = checkNotNull(selectedTimeRange)
                val targetTimeRange = TimeRange(
                    from = currentTimeRange.from.shiftWeek(-3),
                    to = currentTimeRange.from,
                )
                sendAction(TodoAction.UpdateTimeRange(targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_TODOS) {
                    val command = TodoWorkCommand.LoadTodos(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.UpdateTodoDone -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedTodo = todo.copy(
                    isDone = isDone,
                    completeDate = if (isDone) currentTime else null,
                )
                launchBackgroundWork(BackgroundKey.TODO_ACTION) {
                    val command = TodoWorkCommand.UpdateTodo(updatedTodo)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.NavigateToTodoEditor -> with(event) {
                val featureScreen = EditorScreen.Todo(todoId = todo?.uid)
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(TodoEffect.NavigateToGlobal(screen))
            }
            is TodoEvent.NavigateToBack -> {
                sendEffect(TodoEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: TodoAction,
        currentState: TodoViewState,
    ) = when (action) {
        is TodoAction.UpdateTodos -> currentState.copy(
            todos = action.todos,
            isLoading = false,
        )
        is TodoAction.UpdateTimeRange -> currentState.copy(
            selectedTimeRange = action.selectedTimeRange,
        )
        is TodoAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_TODOS, TODO_ACTION
    }
}

@Composable
internal fun Screen.rememberTodoScreenModel(): TodoScreenModel {
    val di = TasksFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<TodoScreenModel>() }
}