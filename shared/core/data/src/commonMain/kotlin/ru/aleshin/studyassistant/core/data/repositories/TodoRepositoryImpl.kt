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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.tasks.TodoLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
class TodoRepositoryImpl(
    private val localDataSource: TodoLocalDataSource,
) : TodoRepository {

    override suspend fun addOrUpdateTodo(todo: Todo): UID {
        val updatedTodo = todo.copy(uid = todo.uid.ifBlank { randomUUID() })
        localDataSource.addOrUpdateTodo(updatedTodo.mapToLocalData())
        return updatedTodo.uid
    }

    override suspend fun fetchTodoById(uid: UID): Flow<Todo?> {
        return localDataSource.fetchTodoById(uid).map { todo -> todo?.mapToDomain() }
    }

    override suspend fun fetchTodosByDate(date: Instant): Flow<List<Todo>> {
        return localDataSource.fetchTodosByTimeRange(
            from = date.startThisDay().toEpochMilliseconds(),
            to = date.endThisDay().toEpochMilliseconds(),
        ).map { todos -> todos.map { it.mapToDomain() } }
    }

    override suspend fun fetchTodosByTimeRange(timeRange: TimeRange): Flow<List<Todo>> {
        return localDataSource.fetchTodosByTimeRange(
            from = timeRange.from.toEpochMilliseconds(),
            to = timeRange.to.toEpochMilliseconds(),
        ).map { todos -> todos.map { it.mapToDomain() } }
    }

    override suspend fun fetchActiveTodos(): Flow<List<Todo>> {
        return localDataSource.fetchActiveTodos().map { todos -> todos.map { it.mapToDomain() } }
    }

    override suspend fun fetchCompletedTodos(completeTimeRange: TimeRange?): Flow<List<Todo>> {
        return localDataSource.fetchCompletedTodos(
            from = completeTimeRange?.from?.toEpochMilliseconds(),
            to = completeTimeRange?.to?.toEpochMilliseconds(),
        ).map { todos -> todos.map { it.mapToDomain() } }
    }

    override suspend fun fetchOverdueTodos(currentDate: Instant): Flow<List<Todo>> {
        return localDataSource.fetchOverdueTodos(
            currentDate.endThisDay().toEpochMilliseconds()
        ).map { todos -> todos.map { it.mapToDomain() } }
    }

    override suspend fun deleteTodo(uid: UID) {
        localDataSource.deleteTodosByIds(listOf(uid))
    }
}
