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

@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.tasks.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.tasks.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.tasks.TodoLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType.UPSERT
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.managers.sync.TodoSourceSyncManager.Companion.TODO_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.remote.datasources.tasks.TodoRemoteDataSource

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
class TodoRepositoryImpl(
    private val remoteDataSource: TodoRemoteDataSource,
    private val localDataSource: TodoLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler,
) : TodoRepository {

    override suspend fun addOrUpdateTodo(todo: Todo): UID {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModel = todo.copy(uid = todo.uid.ifBlank { randomUUID() })

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(upsertModel.mapToLocalData())
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModel.mapToRemoteData(userId = currentUser),
                type = UPSERT,
                sourceKey = TODO_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(upsertModel.mapToLocalData())
        }

        return upsertModel.uid
    }

    override suspend fun fetchTodoById(uid: UID): Flow<Todo?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchItemById(uid).map { it?.mapToDomain() }
            } else {
                localDataSource.offline().fetchItemById(uid).map { it?.mapToDomain() }
            }
        }
    }

    override suspend fun fetchTodosByDate(date: Instant): Flow<List<Todo>> {
        val timeStart = date.startThisDay().toEpochMilliseconds()
        val timeEnd = date.endThisDay().toEpochMilliseconds()

        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchTodosByTimeRange(timeStart, timeEnd).map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchTodosByTimeRange(timeStart, timeEnd).map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchTodosByTimeRange(timeRange: TimeRange): Flow<List<Todo>> {
        val timeStart = timeRange.from.toEpochMilliseconds()
        val timeEnd = timeRange.to.toEpochMilliseconds()

        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchTodosByTimeRange(timeStart, timeEnd).map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchTodosByTimeRange(timeStart, timeEnd).map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchActiveTodos(): Flow<List<Todo>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchActiveTodos().map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchActiveTodos().map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchCompletedTodos(completeTimeRange: TimeRange?): Flow<List<Todo>> {
        val timeStart = completeTimeRange?.from?.toEpochMilliseconds()
        val timeEnd = completeTimeRange?.to?.toEpochMilliseconds()

        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchCompletedTodos(timeStart, timeEnd).map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchCompletedTodos(timeStart, timeEnd).map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchOverdueTodos(currentDate: Instant): Flow<List<Todo>> {
        val date = currentDate.endThisDay().toEpochMilliseconds()

        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchOverdueTodos(date).map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchOverdueTodos(date).map { todos ->
                    todos.map { todoEntity -> todoEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun deleteTodo(uid: UID) {
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        return if (isSubscriber) {
            localDataSource.sync().deleteItemsById(listOf(uid))
            resultSyncHandler.executeOrAddToQueue(
                documentId = uid,
                type = UPSERT,
                sourceKey = TODO_SOURCE_KEY,
            ) {
                remoteDataSource.deleteItemById(uid)
            }
        } else {
            localDataSource.offline().deleteItemsById(listOf(uid))
        }
    }

    override suspend fun transferData(direction: DataTransferDirection) {
        val currentUser = userSessionProvider.getCurrentUserId()
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allTodosFlow = remoteDataSource.fetchAllItems(currentUser)
                val todos = allTodosFlow.first().map { it.convertToLocal() }

                localDataSource.offline().deleteAllItems()
                localDataSource.offline().addOrUpdateItems(todos)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allTodos = localDataSource.offline().fetchAllTodos().first()
                val remoteTodos = allTodos.map { it.convertToRemote(currentUser) }

                remoteDataSource.deleteAllItems(currentUser)
                remoteDataSource.addOrUpdateItems(remoteTodos)

                localDataSource.sync().deleteAllItems()
                localDataSource.sync().addOrUpdateItems(allTodos)
            }
        }
    }
}