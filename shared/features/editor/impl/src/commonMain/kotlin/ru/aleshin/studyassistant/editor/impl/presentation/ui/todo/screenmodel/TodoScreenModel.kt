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
import ru.aleshin.studyassistant.billing.api.navigation.BillingScreen
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoAction.UpdateEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel.TodoWorkCommand.DeleteTodo
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel.TodoWorkCommand.LoadEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel.TodoWorkCommand.SaveTodo

/**
 * @author Stanislav Aleshin on 26.07.2024
 */
internal class TodoScreenModel(
    private val workProcessor: TodoWorkProcessor,
    private val screenProvider: EditorScreenProvider,
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
                    val command = LoadEditModel(todoId)
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
            is TodoEvent.NavigateToBack -> {
                sendEffect(TodoEffect.NavigateToBack)
            }
            is TodoEvent.NavigateToBilling -> {
                val screen = screenProvider.provideBillingScreen(BillingScreen.Subscription)
                sendEffect(TodoEffect.NavigateToGlobal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: TodoAction,
        currentState: TodoViewState,
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
}

@Composable
internal fun Screen.rememberTodoScreenModel(): TodoScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<TodoScreenModel>() }
}