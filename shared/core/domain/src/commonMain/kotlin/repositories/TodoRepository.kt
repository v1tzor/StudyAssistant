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

package repositories

import entities.tasks.Todo
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoRepository {
    suspend fun addOrUpdateTodo(todo: Todo, targetUser: UID): UID
    suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<Todo?>
    suspend fun fetchTodosByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<Todo>>
    suspend fun fetchOverdueTodos(currentDate: Instant, targetUser: UID): Flow<List<Todo>>
    suspend fun fetchTodosByDate(date: Instant, targetUser: UID): Flow<List<Todo>>
    suspend fun deleteTodo(uid: UID, targetUser: UID)
}