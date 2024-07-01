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

package database.tasks

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import extensions.randomUUID
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import managers.CoroutineManager
import mappers.tasks.mapToBaseData
import mappers.tasks.mapToLocalData
import models.tasks.TodoData
import ru.aleshin.studyassistant.sqldelight.tasks.TodoQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoLocalDataSource {

    suspend fun addOrUpdateTodo(todo: TodoData): UID
    suspend fun fetchTodoById(uid: UID): Flow<TodoData?>
    suspend fun fetchTodosByTimeRange(from: Long, to: Long): Flow<List<TodoData>>
    suspend fun fetchOverdueTodos(currentDate: Long): Flow<List<TodoData>>
    suspend fun deleteTodo(uid: UID)

    class Base(
        private val todoQueries: TodoQueries,
        private val coroutineManager: CoroutineManager,
    ) : TodoLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateTodo(todo: TodoData): UID {
            val uid = todo.uid.ifEmpty { randomUUID() }
            val todoEntity = todo.mapToLocalData()
            todoQueries.addOrUpdateTodo(todoEntity.copy(uid = uid))

            return uid
        }

        override suspend fun fetchTodoById(uid: UID): Flow<TodoData?> {
            val query = todoQueries.fetchTodoById(uid)
            val todoEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return todoEntityFlow.map { todoEntity ->
                if (todoEntity == null) return@map null
                return@map todoEntity.mapToBaseData()
            }
        }

        override suspend fun fetchTodosByTimeRange(from: Long, to: Long): Flow<List<TodoData>> {
            val query = todoQueries.fetchTodosByTimeRange(from, to)
            val todoEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return todoEntityListFlow.map { todoEntityList ->
                todoEntityList.map { todoEntity -> todoEntity.mapToBaseData() }
            }
        }

        override suspend fun fetchOverdueTodos(currentDate: Long): Flow<List<TodoData>> {
            val query = todoQueries.fetchOverdueTodos(currentDate)
            val todoEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return todoEntityListFlow.map { todoEntityList ->
                todoEntityList.map { todoEntity -> todoEntity.mapToBaseData() }
            }
        }

        override suspend fun deleteTodo(uid: UID) {
            todoQueries.deleteTodo(uid)
        }
    }
}