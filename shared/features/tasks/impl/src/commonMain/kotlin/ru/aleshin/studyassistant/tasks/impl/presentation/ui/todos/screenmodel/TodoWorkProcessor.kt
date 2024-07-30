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

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoStatus
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.TodoInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEffect

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
internal interface TodoWorkProcessor : FlowWorkProcessor<TodoWorkCommand, TodoAction, TodoEffect> {

    class Base(
        private val todoInteractor: TodoInteractor,
        private val dateManager: DateManager,
    ) : TodoWorkProcessor {

        override suspend fun work(command: TodoWorkCommand) = when (command) {
            is TodoWorkCommand.LoadTodos -> loadTodosWork(command.timeRange)
            is TodoWorkCommand.UpdateTodo -> updateTodoWork(command.todo)
        }

        private fun loadTodosWork(timeRange: TimeRange) = channelFlow<TodoWorkResult> {
            var cycleUpdateJob: Job? = null
            todoInteractor.fetchActiveAndTimeRangeTodos(timeRange).collect { todoEither ->
                cycleUpdateJob?.cancelAndJoin()
                todoEither.handle(
                    onLeftAction = { send(EffectResult(TodoEffect.ShowError(it))) },
                    onRightAction = { todoList ->
                        val todos = todoList.map { todo ->
                            val currentTime = dateManager.fetchCurrentInstant()
                            val duration = todo.deadline?.let { it - currentTime }
                            val status = TodoStatus.calculate(
                                isDone = todo.isDone,
                                deadline = todo.deadline,
                                currentTime = currentTime,
                            )
                            return@map todo.mapToUi(status, duration)
                        }

                        send(ActionResult(TodoAction.UpdateTodos(todos)))

                        cycleUpdateJob = cycleUpdateTodoStatus(todos)
                            .onEach { send(it) }
                            .launchIn(this)
                            .apply { start() }
                    },
                )
            }
        }.onStart {
            emit(ActionResult(TodoAction.UpdateLoading(true)))
        }

        private fun updateTodoWork(todo: TodoDetailsUi) = flow {
            todoInteractor.updateTodo(todo.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(TodoEffect.ShowError(it))) },
            )
        }
        private fun cycleUpdateTodoStatus(todos: List<TodoDetailsUi>) = flow {
            while (currentCoroutineContext().isActive) {
                delay(Constants.Delay.UPDATE_TASK_STATUS)

                val currentInstant = dateManager.fetchCurrentInstant()

                val updatedTodos = todos.map { todo ->
                    val newStatus = TodoStatus.calculate(todo.isDone, todo.deadline, currentInstant)
                    val duration = if (todo.status != TodoStatus.COMPLETE) {
                        todo.deadline?.let { it - currentInstant }
                    } else {
                        null
                    }
                    return@map todo.copy(status = newStatus, toDeadlineDuration = duration)
                }
                emit(ActionResult(TodoAction.UpdateTodos(updatedTodos)))
            }
        }
    }
}

internal sealed class TodoWorkCommand : WorkCommand {
    data class LoadTodos(val timeRange: TimeRange) : TodoWorkCommand()
    data class UpdateTodo(val todo: TodoDetailsUi) : TodoWorkCommand()
}

internal typealias TodoWorkResult = WorkResult<TodoAction, TodoEffect>