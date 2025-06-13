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

package ru.aleshin.studyassistant.core.domain.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoRepository {
    suspend fun addOrUpdateTodo(todo: Todo, targetUser: UID): UID
    suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<Todo?>
    suspend fun fetchTodosByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<Todo>>
    suspend fun fetchActiveTodos(targetUser: UID): Flow<List<Todo>>
    suspend fun fetchCompletedTodos(completeTimeRange: TimeRange? = null, targetUser: UID): Flow<List<Todo>>
    suspend fun fetchOverdueTodos(currentDate: Instant, targetUser: UID): Flow<List<Todo>>
    suspend fun fetchTodosByDate(date: Instant, targetUser: UID): Flow<List<Todo>>
    suspend fun deleteTodo(uid: UID, targetUser: UID)
    suspend fun deleteAllTodos(targetUser: UID)
    suspend fun transferData(direction: DataTransferDirection, targetUser: UID)
}