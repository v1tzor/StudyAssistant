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

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.deleteAll
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotFlowGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.UserData
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojo

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoRemoteDataSource {

    suspend fun addOrUpdateTodo(todo: TodoPojo, targetUser: UID): UID
    suspend fun addOrUpdateTodosGroup(todos: List<TodoPojo>, targetUser: UID)
    suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<TodoPojo?>
    suspend fun fetchTodosByTimeRange(from: Long, to: Long, targetUser: UID): Flow<List<TodoPojo>>
    suspend fun fetchActiveTodos(targetUser: UID): Flow<List<TodoPojo>>
    suspend fun fetchCompletedTodos(from: Long?, to: Long?, targetUser: UID): Flow<List<TodoPojo>>
    suspend fun fetchOverdueTodos(currentDate: Long, targetUser: UID): Flow<List<TodoPojo>>
    suspend fun deleteTodo(uid: UID, targetUser: UID)
    suspend fun deleteAllTodos(targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : TodoRemoteDataSource {

        override suspend fun addOrUpdateTodo(todo: TodoPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS)

            val todoId = todo.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(todoId).set(todo.copy(uid = todoId)).let {
                return@let todoId
            }
        }

        override suspend fun addOrUpdateTodosGroup(todos: List<TodoPojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS)

            database.batch().apply {
                todos.forEach { todo ->
                    val uid = todo.uid.takeIf { it.isNotBlank() } ?: randomUUID()
                    set(reference.document(uid), todo)
                }
                return@apply commit()
            }
        }

        override suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<TodoPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            return userDataRoot.collection(UserData.TODOS).document(uid).snapshotFlowGet<TodoPojo>()
        }

        override suspend fun fetchTodosByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<TodoPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val todosFlow = userDataRoot.collection(UserData.TODOS)
                .where {
                    val fromDeadlineFilter = UserData.TODO_DEADLINE greaterThanOrEqualTo from
                    val toDeadlineFilter = UserData.TODO_DEADLINE lessThanOrEqualTo to
                    val noneDeadlineFilter = UserData.TODO_DEADLINE equalTo null
                    return@where fromDeadlineFilter and toDeadlineFilter or noneDeadlineFilter
                }
                .orderBy(UserData.TODO_DEADLINE, Direction.DESCENDING)
                .snapshotListFlowGet<TodoPojo>()

            return todosFlow
        }

        override suspend fun fetchActiveTodos(targetUser: UID): Flow<List<TodoPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val todosFlow = userDataRoot.collection(UserData.TODOS)
                .where {
                    val doneFilter = UserData.TODO_DONE equalTo false
                    val completeDateFilter = UserData.TODO_COMPLETE_DATE equalTo null
                    return@where doneFilter and completeDateFilter
                }
                .orderBy(UserData.TODO_DEADLINE, Direction.DESCENDING)
                .snapshotListFlowGet<TodoPojo>()

            return todosFlow
        }

        override suspend fun fetchCompletedTodos(
            from: Long?,
            to: Long?,
            targetUser: UID
        ): Flow<List<TodoPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val todosFlow = userDataRoot.collection(UserData.TODOS)
                .where {
                    val completeDateFilter = if (from != null && to != null) {
                        (UserData.TODO_COMPLETE_DATE greaterThanOrEqualTo from) and
                            (UserData.TODO_COMPLETE_DATE lessThanOrEqualTo to)
                    } else {
                        UserData.TODO_COMPLETE_DATE notEqualTo null
                    }
                    val doneFilter = UserData.TODO_DONE equalTo true
                    return@where doneFilter and completeDateFilter
                }
                .orderBy(UserData.TODO_DEADLINE, Direction.DESCENDING)
                .snapshotListFlowGet<TodoPojo>()

            return todosFlow
        }

        override suspend fun fetchOverdueTodos(
            currentDate: Long,
            targetUser: UID
        ): Flow<List<TodoPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val todosFlow = userDataRoot.collection(UserData.TODOS)
                .where {
                    val deadlineFilter = UserData.TODO_DEADLINE lessThan currentDate
                    val doneFilter = UserData.TODO_DONE equalTo false
                    val completeDateFilter = UserData.TODO_COMPLETE_DATE equalTo null
                    return@where deadlineFilter and doneFilter and completeDateFilter
                }
                .orderBy(UserData.TODO_DEADLINE, Direction.DESCENDING)
                .snapshotListFlowGet<TodoPojo>()

            return todosFlow
        }

        override suspend fun deleteTodo(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS).document(uid)

            return reference.delete()
        }

        override suspend fun deleteAllTodos(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS)

            database.deleteAll(reference)
        }
    }
}