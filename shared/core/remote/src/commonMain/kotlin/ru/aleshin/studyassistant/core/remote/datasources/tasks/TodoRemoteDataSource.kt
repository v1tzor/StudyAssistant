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

package ru.aleshin.studyassistant.core.remote.datasources.tasks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.AppwriteApi.Todo
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.mappers.tasks.convertToBase
import ru.aleshin.studyassistant.core.remote.mappers.tasks.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojoDetails

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoRemoteDataSource {

    suspend fun addOrUpdateTodo(todo: TodoPojoDetails, targetUser: UID): UID
    suspend fun addOrUpdateTodosGroup(todos: List<TodoPojoDetails>, targetUser: UID)
    suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<TodoPojoDetails?>
    suspend fun fetchTodosByTimeRange(from: Long, to: Long, targetUser: UID): Flow<List<TodoPojoDetails>>
    suspend fun fetchActiveTodos(targetUser: UID): Flow<List<TodoPojoDetails>>
    suspend fun fetchCompletedTodos(from: Long?, to: Long?, targetUser: UID): Flow<List<TodoPojoDetails>>
    suspend fun fetchOverdueTodos(currentDate: Long, targetUser: UID): Flow<List<TodoPojoDetails>>
    suspend fun deleteTodo(uid: UID, targetUser: UID)
    suspend fun deleteAllTodos(targetUser: UID)

    class Base(
        private val database: DatabaseApi,
    ) : TodoRemoteDataSource {

        override suspend fun addOrUpdateTodo(todo: TodoPojoDetails, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val todoId = todo.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            database.upsertDocument(
                databaseId = Todo.DATABASE_ID,
                collectionId = Todo.COLLECTION_ID,
                documentId = todoId,
                data = todo.convertToBase().copy(uid = todoId),
                permissions = Permission.onlyUserData(targetUser),
                nestedType = TodoPojo.serializer(),
            )

            return todoId
        }

        override suspend fun addOrUpdateTodosGroup(todos: List<TodoPojoDetails>, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            todos.forEach { todo -> addOrUpdateTodo(todo, targetUser) }
        }

        override suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<TodoPojoDetails?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val todoFlow = database.getDocumentFlow(
                databaseId = Todo.DATABASE_ID,
                collectionId = Todo.COLLECTION_ID,
                documentId = uid,
                nestedType = TodoPojo.serializer(),
            )

            return todoFlow.map { todo -> todo?.convertToDetails() }
        }

        override suspend fun fetchTodosByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<TodoPojoDetails>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val todosWithDeadlineFlow = database.listDocumentsFlow(
                databaseId = Todo.DATABASE_ID,
                collectionId = Todo.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Todo.USER_ID, targetUser),
                    Query.between(Todo.DEADLINE, from, to),
                    Query.orderDesc(Todo.DEADLINE),
                ),
                nestedType = TodoPojo.serializer(),
            )

            val freeTodosFlow = database.listDocumentsFlow(
                databaseId = Todo.DATABASE_ID,
                collectionId = Todo.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Todo.USER_ID, targetUser),
                    Query.isNull(Todo.DEADLINE),
                    Query.orderDesc(Todo.DEADLINE),
                ),
                nestedType = TodoPojo.serializer(),
            )

            val todosFlow = combine(todosWithDeadlineFlow, freeTodosFlow) { deadlinedTodos, freeTodos ->
                deadlinedTodos + freeTodos
            }

            return todosFlow.map { todos ->
                todos.map { it.convertToDetails() }
            }
        }

        override suspend fun fetchActiveTodos(targetUser: UID): Flow<List<TodoPojoDetails>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val todosFlow = database.listDocumentsFlow(
                databaseId = Todo.DATABASE_ID,
                collectionId = Todo.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Todo.USER_ID, targetUser),
                    Query.equal(Todo.DONE, false),
                    Query.isNull(Todo.COMPLETE_DATE),
                ),
                nestedType = TodoPojo.serializer(),
            )

            return todosFlow.map { todos ->
                todos.map { it.convertToDetails() }
            }
        }

        override suspend fun fetchCompletedTodos(
            from: Long?,
            to: Long?,
            targetUser: UID
        ): Flow<List<TodoPojoDetails>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val todosFlow = database.listDocumentsFlow(
                databaseId = Todo.DATABASE_ID,
                collectionId = Todo.COLLECTION_ID,
                queries = if (from != null && to != null) {
                    listOf(
                        Query.equal(Todo.USER_ID, targetUser),
                        Query.equal(Todo.DONE, true),
                        Query.between(Todo.COMPLETE_DATE, from, to),
                    )
                } else {
                    listOf(
                        Query.equal(Todo.USER_ID, targetUser),
                        Query.equal(Todo.DONE, true),
                        Query.isNotNull(Todo.COMPLETE_DATE),
                    )
                },
                nestedType = TodoPojo.serializer(),
            )

            return todosFlow.map { todos ->
                todos.map { it.convertToDetails() }
            }
        }

        override suspend fun fetchOverdueTodos(
            currentDate: Long,
            targetUser: UID
        ): Flow<List<TodoPojoDetails>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val todosFlow = database.listDocumentsFlow(
                databaseId = Todo.DATABASE_ID,
                collectionId = Todo.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Todo.USER_ID, targetUser),
                    Query.lessThan(Todo.DEADLINE, currentDate),
                    Query.equal(Todo.DONE, false),
                    Query.isNull(Todo.COMPLETE_DATE),
                    Query.orderDesc(Todo.DEADLINE)
                ),
                nestedType = TodoPojo.serializer(),
            )

            return todosFlow.map { todos ->
                todos.map { it.convertToDetails() }
            }
        }

        override suspend fun deleteTodo(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.deleteDocument(
                databaseId = Todo.DATABASE_ID,
                collectionId = Todo.COLLECTION_ID,
                documentId = targetUser,
            )
        }

        override suspend fun deleteAllTodos(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val todosFlow = fetchTodosByTimeRange(Long.MIN_VALUE, Long.MAX_VALUE, targetUser)
            todosFlow.first().forEach { todo -> deleteTodo(todo.uid, targetUser) }
        }
    }
}