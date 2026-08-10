/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.tasks.impl.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.tasks.DetailsGroupedTodos
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.convertToDetails
import ru.aleshin.studyassistant.core.domain.interactors.TodoCompletionInteractor
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
internal interface TodoInteractor {

    suspend fun fetchWeekGroupedTodosByTimeRange(timeRange: TimeRange): FlowDomainResult<TasksFailures, DetailsGroupedTodos>
    suspend fun fetchCompletedTodos(): FlowDomainResult<TasksFailures, List<Todo>>
    suspend fun updateTodoDone(todo: Todo): UnitDomainResult<TasksFailures>

    class Base(
        private val todoRepository: TodoRepository,
        private val goalsRepository: DailyGoalsRepository,
        private val dateManager: DateManager,
        private val todoCompletionInteractor: TodoCompletionInteractor,
        private val eitherWrapper: TasksEitherWrapper,
    ) : TodoInteractor {

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchWeekGroupedTodosByTimeRange(timeRange: TimeRange) =
            eitherWrapper.wrapFlow {
                val ticker = dateManager.secondTicker()
                val shortGoalsFlow = goalsRepository.fetchShortActiveDailyGoals()
                val weekCompletedTodosFlow =
                    todoRepository.fetchCompletedTodos(timeRange).map { todos ->
                        todos.sortedBy { it.deadline }
                    }
                val activeTodosFlow = todoRepository.fetchActiveTodos().map { todos ->
                    todos.sortedBy { it.deadline }
                }

                return@wrapFlow combine(
                    weekCompletedTodosFlow,
                    activeTodosFlow,
                    shortGoalsFlow,
                    ticker,
                ) { completedTodos, activeTodos, goals, _ ->
                    val currentTime = dateManager.fetchCurrentInstant()
                    val runningTodos = mutableListOf<Todo>()
                    val errorTodos = mutableListOf<Todo>()
                    activeTodos.forEach { todo ->
                        val deadline = todo.deadline
                        if (deadline != null) {
                            if (deadline >= currentTime) runningTodos.add(todo) else errorTodos.add(
                                todo
                            )
                        } else {
                            runningTodos.add(todo)
                        }
                    }

                    DetailsGroupedTodos(
                        completedTodos = completedTodos.map { todo ->
                            todo.convertToDetails(
                                deadlineTimeLeft = null,
                                status = TodoStatus.COMPLETE,
                                progress = 1f,
                                linkedGoal = null,
                            )
                        },
                        runningTodos = runningTodos.map { todo ->
                            val createdAt = todo.createdAt.toEpochMilliseconds()
                            val currentTime = currentTime.toEpochMilliseconds()
                            val deadline = todo.deadline?.toEpochMilliseconds()

                            val leftTime = if (deadline != null) deadline - currentTime else null

                            val progress = if (deadline != null) {
                                ((currentTime - createdAt).toFloat() / (deadline - createdAt).toFloat()).coerceIn(
                                    0f,
                                    1f
                                )
                            } else {
                                0f
                            }

                            todo.convertToDetails(
                                deadlineTimeLeft = leftTime?.let { it - it % 10000 },
                                status = TodoStatus.IN_PROGRESS,
                                progress = progress,
                                linkedGoal = goals.find { it.contentId == todo.uid },
                            )
                        },
                        errorTodos = errorTodos.map { todo ->
                            val expiredTime = todo.deadline?.let { deadline ->
                                deadline.toEpochMilliseconds() - currentTime.toEpochMilliseconds()
                            }
                            todo.convertToDetails(
                                deadlineTimeLeft = expiredTime?.let { it - it % 10000 },
                                status = TodoStatus.NOT_COMPLETE,
                                progress = 0f,
                                linkedGoal = goals.find { it.contentId == todo.uid },
                            )
                        },
                    )
                }.distinctUntilChanged()
            }

        override suspend fun fetchCompletedTodos() = eitherWrapper.wrapFlow {
            todoRepository.fetchCompletedTodos(null)
        }

        override suspend fun updateTodoDone(todo: Todo) = eitherWrapper.wrapUnit {
            todoCompletionInteractor.setDone(todo.uid, !todo.isDone)
        }
    }
}
