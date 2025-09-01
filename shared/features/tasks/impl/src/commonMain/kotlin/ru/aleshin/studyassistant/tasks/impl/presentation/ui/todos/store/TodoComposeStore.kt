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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoState

/**
 * @author Stanislav Aleshin on 28.07.2024
 */
internal class TodoComposeStore(
    private val workProcessor: TodoDetailsWorkProcessor,
    stateCommunicator: StateCommunicator<TodoState>,
    effectCommunicator: EffectCommunicator<TodoEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<TodoState, TodoEvent, TodoAction, TodoEffect, TodoOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(TodoEvent.Started)
    }

    override suspend fun WorkScope<TodoState, TodoAction, TodoEffect, TodoOutput>.handleEvent(
        event: TodoEvent,
    ) {
        when (event) {
            is TodoEvent.Started -> {
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
            is TodoEvent.ClickTodoTask -> with(event) {
                val config = EditorConfig.Todo(todoId = todo?.uid)
                consumeOutput(TodoOutput.NavigateToTodoEditor(config))
            }
            is TodoEvent.ClickBack -> {
                consumeOutput(TodoOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: TodoAction,
        currentState: TodoState,
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

    class Factory(
        private val workProcessor: TodoDetailsWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<TodoComposeStore, TodoState> {

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