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

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.firstRightOrNull
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.editor.impl.domain.interactors.TodoInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.EditTodoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.convertToEdit
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEffect

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
internal interface TodoWorkProcessor : FlowWorkProcessor<TodoWorkCommand, TodoAction, TodoEffect> {

    class Base(
        private val todoInteractor: TodoInteractor,
    ) : TodoWorkProcessor {

        override suspend fun work(command: TodoWorkCommand) = when (command) {
            is TodoWorkCommand.LoadEditModel -> loadEditModelWork(command.todoId)
            is TodoWorkCommand.SaveTodo -> saveTodoWork(command.todo)
            is TodoWorkCommand.DeleteTodo -> deleteTodoWork(command.todo)
        }

        private fun loadEditModelWork(todoId: UID?) = flow {
            val todoModel = todoInteractor.fetchTodoById(todoId ?: "").firstRightOrNull {
                emit(EffectResult(TodoEffect.ShowError(it)))
            }

            val editModel = todoModel?.mapToUi()?.convertToEdit() ?: EditTodoUi.createEditModel(todoId)

            emit(ActionResult(TodoAction.SetupEditModel(editModel)))
        }

        private fun saveTodoWork(todo: EditTodoUi) = flow<TodoWorkResult> {
            todoInteractor.addOrUpdateTodo(todo.convertToBase().mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(TodoEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(TodoEffect.NavigateToBack)) },
            )
        }.onStart {
            emit(ActionResult(TodoAction.UpdateLoadingSave(true)))
        }.onCompletion {
            emit(ActionResult(TodoAction.UpdateLoadingSave(false)))
        }

        private fun deleteTodoWork(todo: EditTodoUi) = flow {
            todoInteractor.deleteTodo(todo.uid).handle(
                onLeftAction = { emit(EffectResult(TodoEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(TodoEffect.NavigateToBack)) },
            )
        }
    }
}

internal sealed class TodoWorkCommand : WorkCommand {
    data class LoadEditModel(val todoId: UID?) : TodoWorkCommand()
    data class SaveTodo(val todo: EditTodoUi) : TodoWorkCommand()
    data class DeleteTodo(val todo: EditTodoUi) : TodoWorkCommand()
}

internal typealias TodoWorkResult = WorkResult<TodoAction, TodoEffect>