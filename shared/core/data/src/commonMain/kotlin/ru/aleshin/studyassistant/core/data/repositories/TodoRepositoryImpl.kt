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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.Instant.Companion.DISTANT_FUTURE
import kotlinx.datetime.Instant.Companion.DISTANT_PAST
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.payments.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.tasks.TodoLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.remote.datasources.tasks.TodoRemoteDataSource

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
class TodoRepositoryImpl(
    private val remoteDataSource: TodoRemoteDataSource,
    private val localDataSource: TodoLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : TodoRepository {

    override suspend fun addOrUpdateTodo(todo: Todo, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateTodo(todo.mapToRemoteData(), targetUser)
        } else {
            localDataSource.addOrUpdateTodo(todo.mapToLocalData())
        }
    }

    override suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<Todo?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchTodoById(uid, targetUser).map { todoPojo -> todoPojo?.mapToDomain() }
        } else {
            localDataSource.fetchTodoById(uid).map { todoEntity -> todoEntity?.mapToDomain() }
        }
    }

    override suspend fun fetchTodosByDate(date: Instant, targetUser: UID): Flow<List<Todo>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = date.startThisDay().toEpochMilliseconds()
        val timeEnd = date.endThisDay().toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchTodosByTimeRange(timeStart, timeEnd, targetUser).map { todos ->
                todos.map { todoPojo -> todoPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchTodosByTimeRange(timeStart, timeEnd).map { todos ->
                todos.map { todoEntity -> todoEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchTodosByTimeRange(
        timeRange: TimeRange,
        targetUser: UID
    ): Flow<List<Todo>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = timeRange.from.toEpochMilliseconds()
        val timeEnd = timeRange.to.toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchTodosByTimeRange(timeStart, timeEnd, targetUser).map { todos ->
                todos.map { todoPojo -> todoPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchTodosByTimeRange(timeStart, timeEnd).map { todos ->
                todos.map { todoEntity -> todoEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchActiveTodos(targetUser: UID): Flow<List<Todo>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchActiveTodos(targetUser).map { todos ->
                todos.map { todoPojo -> todoPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchActiveTodos().map { todos ->
                todos.map { todoEntity -> todoEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchCompletedTodos(completeTimeRange: TimeRange?, targetUser: UID): Flow<List<Todo>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = completeTimeRange?.from?.toEpochMilliseconds()
        val timeEnd = completeTimeRange?.to?.toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchCompletedTodos(timeStart, timeEnd, targetUser).map { todos ->
                todos.map { todoPojo -> todoPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchCompletedTodos(timeStart, timeEnd).map { todos ->
                todos.map { todoEntity -> todoEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchOverdueTodos(
        currentDate: Instant,
        targetUser: UID
    ): Flow<List<Todo>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val date = currentDate.endThisDay().toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchOverdueTodos(date, targetUser).map { todos ->
                todos.map { todoPojo -> todoPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchOverdueTodos(date).map { todos ->
                todos.map { todoEntity -> todoEntity.mapToDomain() }
            }
        }
    }

    override suspend fun deleteTodo(uid: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteTodo(uid, targetUser)
        } else {
            localDataSource.deleteTodo(uid)
        }
    }

    override suspend fun deleteAllTodos(targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteAllTodos(targetUser)
        } else {
            localDataSource.deleteAllTodos()
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, targetUser: UID) {
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allTodos = remoteDataSource.fetchTodosByTimeRange(
                    from = DISTANT_PAST.toEpochMilliseconds(),
                    to = DISTANT_FUTURE.toEpochMilliseconds(),
                    targetUser = targetUser,
                ).let { todosFlow ->
                    return@let todosFlow.first().map { it.mapToDomain().mapToLocalData() }
                }
                localDataSource.deleteAllTodos()
                localDataSource.addOrUpdateTodosGroup(allTodos)
                remoteDataSource.deleteAllTodos(targetUser)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allTodos = localDataSource.fetchTodosByTimeRange(
                    from = DISTANT_PAST.toEpochMilliseconds(),
                    to = DISTANT_FUTURE.toEpochMilliseconds(),
                ).let { todosFlow ->
                    return@let todosFlow.first().map { it.mapToDomain().mapToRemoteData() }
                }
                remoteDataSource.deleteAllTodos(targetUser)
                remoteDataSource.addOrUpdateTodosGroup(allTodos, targetUser)
            }
        }
    }
}