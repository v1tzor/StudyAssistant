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

package ru.aleshin.studyassistant.core.database.datasource.tasks

import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToEntity
import ru.aleshin.studyassistant.core.database.models.tasks.BaseTodoEntity
import ru.aleshin.studyassistant.sqldelight.tasks.TodoQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoLocalDataSource {

    suspend fun addOrUpdateTodo(item: BaseTodoEntity)
    suspend fun fetchTodoById(id: String): Flow<BaseTodoEntity?>
    suspend fun deleteTodosByIds(ids: List<String>)

    suspend fun fetchTodosByTimeRange(from: Long, to: Long): Flow<List<BaseTodoEntity>>
    suspend fun fetchActiveTodos(): Flow<List<BaseTodoEntity>>
    suspend fun fetchCompletedTodos(from: Long?, to: Long?): Flow<List<BaseTodoEntity>>
    suspend fun fetchOverdueTodos(currentDate: Long): Flow<List<BaseTodoEntity>>

    class Base(
        protected val todoQueries: TodoQueries,
        protected val coroutineManager: CoroutineManager,
    ) : TodoLocalDataSource {

        protected val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher


        override suspend fun addOrUpdateTodo(item: BaseTodoEntity) {
            val uid = item.uid.ifEmpty { randomUUID() }
            val updatedItem = item.copy(uid = uid).mapToEntity()
            todoQueries.addOrUpdateTodo(updatedItem).await()
        }

        override suspend fun fetchTodoById(id: String): Flow<BaseTodoEntity?> {
            val query = todoQueries.fetchTodoById(id)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchTodosByTimeRange(
            from: Long,
            to: Long
        ): Flow<List<BaseTodoEntity>> {
            val query = todoQueries.fetchTodosByTimeRange(from, to)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchActiveTodos(): Flow<List<BaseTodoEntity>> {
            val query = todoQueries.fetchActiveTodos()
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchCompletedTodos(
            from: Long?,
            to: Long?
        ): Flow<List<BaseTodoEntity>> {
            return if (from != null && to != null) {
                val query = todoQueries.fetchCompletedTodosByTimeRange(from, to)
                query.mapToListFlow(coroutineContext) { it.mapToBase() }
            } else {
                val query = todoQueries.fetchCompletedTodos()
                query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }
        }

        override suspend fun fetchOverdueTodos(currentDate: Long): Flow<List<BaseTodoEntity>> {
            val query = todoQueries.fetchOverdueTodos(currentDate)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun deleteTodosByIds(ids: List<String>) {
            todoQueries.deleteTodosById(ids).await()
        }

    }

}
