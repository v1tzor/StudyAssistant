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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoViewState

/**
 * @author Stanislav Aleshin on 26.07.2024
 */
internal class TodoScreenModel(
    private val workProcessor: TodoWorkProcessor,
    stateCommunicator: TodoStateCommunicator,
    effectCommunicator: TodoEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<TodoViewState, TodoEvent, TodoAction, TodoEffect, TodoDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: TodoDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(TodoEvent.Init(deps.todoId))
        }
    }

    override suspend fun WorkScope<TodoViewState, TodoAction, TodoEffect>.handleEvent(
        event: TodoEvent,
    ) {
        when (event) {
            is TodoEvent.Init -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_TODO) {
                    val command = TodoWorkCommand.LoadEditModel(todoId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.UpdateTodoName -> with(state()) {
                val updatedTodo = editableTodo?.copy(name = event.todo)
                sendAction(TodoAction.UpdateEditModel(updatedTodo))
            }
            is TodoEvent.UpdateDeadline -> with(state()) {
                val updatedTodo = editableTodo?.copy(deadline = event.deadline)
                sendAction(TodoAction.UpdateEditModel(updatedTodo))
            }
            is TodoEvent.UpdatePriority -> with(state()) {
                val updatedTodo = editableTodo?.copy(priority = event.priority)
                sendAction(TodoAction.UpdateEditModel(updatedTodo))
            }
            is TodoEvent.UpdateNotifications -> with(state()) {
                val updatedTodo = editableTodo?.copy(notifications = event.notifications)
                sendAction(TodoAction.UpdateEditModel(updatedTodo))
            }
            is TodoEvent.SaveTodo -> with(state()) {
                launchBackgroundWork(BackgroundKey.TODO_ACTION) {
                    val todo = checkNotNull(editableTodo)
                    val command = TodoWorkCommand.SaveTodo(todo)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.DeleteTodo -> with(state()) {
                launchBackgroundWork(BackgroundKey.TODO_ACTION) {
                    val todo = checkNotNull(editableTodo)
                    val command = TodoWorkCommand.DeleteTodo(todo)
                    workProcessor.work(command).collectAndHandleWork()
                }
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
        is TodoAction.SetupEditModel -> currentState.copy(
            editableTodo = action.editModel,
            isLoading = false,
        )
        is TodoAction.UpdateEditModel -> currentState.copy(
            editableTodo = action.editModel,
        )
        is TodoAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is TodoAction.UpdateLoadingSave -> currentState.copy(
            isLoadingSave = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_TODO, TODO_ACTION
    }
}

@Composable
internal fun Screen.rememberTodoScreenModel(): TodoScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<TodoScreenModel>() }
}