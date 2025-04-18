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

package ru.aleshin.studyassistant.tasks.impl.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.managers.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TodoErrors

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
internal interface TodoInteractor {

    suspend fun fetchTodosByTimeRange(timeRange: TimeRange): FlowDomainResult<TasksFailures, List<Todo>>
    suspend fun fetchActiveAndTimeRangeTodos(timeRange: TimeRange): FlowDomainResult<TasksFailures, List<Todo>>
    suspend fun fetchTodoErrors(targetDate: Instant): FlowDomainResult<TasksFailures, TodoErrors>
    suspend fun updateTodo(todo: Todo): UnitDomainResult<TasksFailures>

    class Base(
        private val todoRepository: TodoRepository,
        private val todoReminderManager: TodoReminderManager,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: TasksEitherWrapper,
    ) : TodoInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchTodosByTimeRange(timeRange: TimeRange) = eitherWrapper.wrapFlow {
            todoRepository.fetchTodosByTimeRange(timeRange, targetUser).map { todoList ->
                todoList.sortedBy { it.deadline }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchActiveAndTimeRangeTodos(timeRange: TimeRange) = eitherWrapper.wrapFlow {
            todoRepository.fetchTodosByTimeRange(timeRange, targetUser).flatMapLatest { timeRangeTodos ->
                todoRepository.fetchActiveTodos(targetUser).map { activeTodos ->
                    val filteredTimeRangeTodos = timeRangeTodos.filter { todo ->
                        activeTodos.find { it.uid == todo.uid } == null
                    }
                    return@map (activeTodos + filteredTimeRangeTodos).sortedBy { it.deadline }
                }
            }
        }

        override suspend fun fetchTodoErrors(targetDate: Instant) = eitherWrapper.wrapFlow {
            todoRepository.fetchOverdueTodos(targetDate, targetUser).map { todoList ->
                return@map TodoErrors(overdueTodos = todoList.sortedBy { it.deadline })
            }
        }

        override suspend fun updateTodo(todo: Todo) = eitherWrapper.wrapUnit {
            todoRepository.addOrUpdateTodo(todo, targetUser)
            if (todo.isDone) {
                todoReminderManager.clearAllReminders(todo.uid)
            } else {
                todoReminderManager.scheduleReminders(todo.uid, todo.name, todo.deadline, todo.notifications)
            }
        }
    }
}