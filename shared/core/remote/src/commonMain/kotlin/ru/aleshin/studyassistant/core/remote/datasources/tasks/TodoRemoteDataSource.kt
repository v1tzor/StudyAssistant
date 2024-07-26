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
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.exists
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirestore.UserData
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojo

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoRemoteDataSource {

    suspend fun addOrUpdateTodo(todo: TodoPojo, targetUser: UID): UID
    suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<TodoPojo?>
    suspend fun fetchTodosByTimeRange(from: Long, to: Long, targetUser: UID): Flow<List<TodoPojo>>
    suspend fun fetchActiveTodos(targetUser: UID): Flow<List<TodoPojo>>
    suspend fun fetchOverdueTodos(currentDate: Long, targetUser: UID): Flow<List<TodoPojo>>
    suspend fun deleteTodo(uid: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : TodoRemoteDataSource {

        override suspend fun addOrUpdateTodo(todo: TodoPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS)

            return database.runTransaction {
                val isExist = todo.uid.isNotEmpty() && reference.document(todo.uid).exists()
                if (isExist) {
                    reference.document(todo.uid).set(data = todo)
                    return@runTransaction todo.uid
                } else {
                    val uid = reference.add(todo).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<TodoPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS).document(uid)

            return reference.snapshots.map { snapshot ->
                snapshot.data(serializer<TodoPojo?>())
            }
        }

        override suspend fun fetchTodosByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<TodoPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS).where {
                val fromDeadlineFilter = UserData.TODO_DEADLINE greaterThanOrEqualTo from
                val toDeadlineFilter = UserData.TODO_DEADLINE lessThanOrEqualTo to
                return@where fromDeadlineFilter and toDeadlineFilter
            }.orderBy(UserData.TODO_DEADLINE, Direction.DESCENDING)

            return reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<TodoPojo>()) }
            }
        }

        override suspend fun fetchActiveTodos(targetUser: UID): Flow<List<TodoPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS).where {
                val doneFilter = UserData.TODO_DONE equalTo false
                val completeDateFilter = UserData.TODO_COMPLETE_DATE equalTo null
                return@where doneFilter and completeDateFilter
            }.orderBy(UserData.TODO_DEADLINE, Direction.DESCENDING)

            return reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<TodoPojo>()) }
            }
        }

        override suspend fun fetchOverdueTodos(
            currentDate: Long,
            targetUser: UID
        ): Flow<List<TodoPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS).where {
                val deadlineFilter = UserData.TODO_DEADLINE lessThan currentDate
                val doneFilter = UserData.TODO_DONE equalTo false
                val completeDateFilter = UserData.TODO_COMPLETE_DATE equalTo null
                return@where deadlineFilter and doneFilter and completeDateFilter
            }.orderBy(UserData.TODO_DEADLINE, Direction.DESCENDING)

            return reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<TodoPojo>()) }
            }
        }

        override suspend fun deleteTodo(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.TODOS).document(uid)

            return reference.delete()
        }
    }
}