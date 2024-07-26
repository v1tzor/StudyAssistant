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

package ru.aleshin.studyassistant.core.database.datasource.tasks

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.sqldelight.tasks.TodoEntity
import ru.aleshin.studyassistant.sqldelight.tasks.TodoQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoLocalDataSource {

    suspend fun addOrUpdateTodo(todo: TodoEntity): UID
    suspend fun fetchTodoById(uid: UID): Flow<TodoEntity?>
    suspend fun fetchTodosByTimeRange(from: Long, to: Long): Flow<List<TodoEntity>>
    suspend fun fetchActiveTodos(): Flow<List<TodoEntity>>
    suspend fun fetchOverdueTodos(currentDate: Long): Flow<List<TodoEntity>>
    suspend fun deleteTodo(uid: UID)

    class Base(
        private val todoQueries: TodoQueries,
        private val coroutineManager: CoroutineManager,
    ) : TodoLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateTodo(todo: TodoEntity): UID {
            val uid = todo.uid.ifEmpty { randomUUID() }
            todoQueries.addOrUpdateTodo(todo.copy(uid = uid))

            return uid
        }

        override suspend fun fetchTodoById(uid: UID): Flow<TodoEntity?> {
            val query = todoQueries.fetchTodoById(uid)
            return query.asFlow().mapToOneOrNull(coroutineContext)
        }

        override suspend fun fetchTodosByTimeRange(from: Long, to: Long): Flow<List<TodoEntity>> {
            val query = todoQueries.fetchTodosByTimeRange(from, to)
            return query.asFlow().mapToList(coroutineContext)
        }

        override suspend fun fetchActiveTodos(): Flow<List<TodoEntity>> {
            val query = todoQueries.fetchActiveTodos()
            return query.asFlow().mapToList(coroutineContext)
        }

        override suspend fun fetchOverdueTodos(currentDate: Long): Flow<List<TodoEntity>> {
            val query = todoQueries.fetchOverdueTodos(currentDate)
            return query.asFlow().mapToList(coroutineContext)
        }

        override suspend fun deleteTodo(uid: UID) {
            todoQueries.deleteTodo(uid)
        }
    }
}