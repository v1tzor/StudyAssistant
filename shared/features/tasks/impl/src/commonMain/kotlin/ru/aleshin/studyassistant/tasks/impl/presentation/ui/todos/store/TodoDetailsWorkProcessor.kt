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

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.TodoInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoOutput

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
internal interface TodoDetailsWorkProcessor :
    FlowWorkProcessor<TodoDetailsWorkCommand, TodoAction, TodoEffect, TodoOutput> {

    class Base(
        private val todoInteractor: TodoInteractor,
    ) : TodoDetailsWorkProcessor {

        override suspend fun work(command: TodoDetailsWorkCommand) = when (command) {
            is TodoDetailsWorkCommand.LoadCompletedTodos -> loadCompletedTodosWork()
            is TodoDetailsWorkCommand.UpdateTodoDone -> updateTodoDoneWork(command.todo)
        }

        private fun loadCompletedTodosWork() = flow<TodoWorkResult> {
            todoInteractor.fetchCompletedTodos().collectAndHandle(
                onLeftAction = { emit(EffectResult(TodoEffect.ShowError(it))) },
                onRightAction = { todos ->
                    emit(ActionResult(TodoAction.UpdateTodos(todos.map { it.mapToUi() })))
                }
            )
        }.onStart {
            emit(ActionResult(TodoAction.UpdateLoading(true)))
        }

        private fun updateTodoDoneWork(todo: TodoUi) = flow {
            todoInteractor.updateTodoDone(todo.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(TodoEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class TodoDetailsWorkCommand : WorkCommand {
    data object LoadCompletedTodos : TodoDetailsWorkCommand()
    data class UpdateTodoDone(val todo: TodoUi) : TodoDetailsWorkCommand()
}

internal typealias TodoWorkResult = WorkResult<TodoAction, TodoEffect, TodoOutput>