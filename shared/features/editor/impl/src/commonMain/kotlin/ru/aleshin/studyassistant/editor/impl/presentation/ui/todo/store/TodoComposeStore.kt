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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoAction.UpdateEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoWorkCommand.DeleteTodo
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoWorkCommand.LoadEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoWorkCommand.SaveTodo

/**
 * @author Stanislav Aleshin on 26.07.2024
 */
internal class TodoComposeStore(
    private val workProcessor: TodoWorkProcessor,
    stateCommunicator: StateCommunicator<TodoState>,
    effectCommunicator: EffectCommunicator<TodoEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<TodoState, TodoEvent, TodoAction, TodoEffect, TodoInput, TodoOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: TodoInput, isRestore: Boolean) {
        if (!isRestore) {
            dispatchEvent(TodoEvent.Started(input))
        }
    }

    override suspend fun WorkScope<TodoState, TodoAction, TodoEffect, TodoOutput>.handleEvent(
        event: TodoEvent,
    ) {
        when (event) {
            is TodoEvent.Started -> with(event) {
                launchBackgroundWork(BackgroundKey.LOAD_TODO) {
                    val command = LoadEditModel(inputData.todoId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.UpdateTodoName -> with(state()) {
                val updatedTodo = editableTodo?.copy(name = event.todo)
                sendAction(UpdateEditModel(updatedTodo))
            }
            is TodoEvent.UpdateTodoDescription -> with(state()) {
                val updatedTodo = editableTodo?.copy(description = event.description)
                sendAction(UpdateEditModel(updatedTodo))
            }
            is TodoEvent.UpdateDeadline -> with(state()) {
                val updatedTodo = editableTodo?.copy(deadline = event.deadline)
                sendAction(UpdateEditModel(updatedTodo))
            }
            is TodoEvent.UpdatePriority -> with(state()) {
                val updatedTodo = editableTodo?.copy(priority = event.priority)
                sendAction(UpdateEditModel(updatedTodo))
            }
            is TodoEvent.UpdateNotifications -> with(state()) {
                val updatedTodo = editableTodo?.copy(notifications = event.notifications)
                sendAction(UpdateEditModel(updatedTodo))
            }
            is TodoEvent.SaveTodo -> with(state()) {
                launchBackgroundWork(BackgroundKey.TODO_ACTION) {
                    val todo = checkNotNull(editableTodo)
                    val command = SaveTodo(todo)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.DeleteTodo -> with(state()) {
                launchBackgroundWork(BackgroundKey.TODO_ACTION) {
                    val todo = checkNotNull(editableTodo)
                    val command = DeleteTodo(todo)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is TodoEvent.NavigateToBilling -> {
                consumeOutput(TodoOutput.NavigateToBilling)
            }
            is TodoEvent.NavigateToBack -> {
                consumeOutput(TodoOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: TodoAction,
        currentState: TodoState,
    ) = when (action) {
        is TodoAction.SetupEditModel -> currentState.copy(
            editableTodo = action.editModel,
            isPaidUser = action.isPaidUser,
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

    class Factory(
        private val workProcessor: TodoWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<TodoComposeStore, TodoState> {

        override fun create(savedState: TodoState): TodoComposeStore {
            return TodoComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}