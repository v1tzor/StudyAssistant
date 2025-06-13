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
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
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
    private val workProcessor: TodoDetailsWorkProcessor,
    private val screenProvider: TasksScreenProvider,
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
                launchBackgroundWork(BackgroundKey.LOAD_TODOS) {
                    val command = TodoDetailsWorkCommand.LoadCompletedTodos
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.UpdateTodoDone -> with(event) {
                launchBackgroundWork(BackgroundKey.TODO_ACTION) {
                    val command = TodoDetailsWorkCommand.UpdateTodoDone(todo)
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
            completedTodos = action.todos,
            isLoading = false,
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