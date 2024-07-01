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

import database.tasks.TodoLocalDataSource
import entities.tasks.Todo
import extensions.endThisDay
import extensions.startThisDay
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import mappers.tasks.mapToData
import mappers.tasks.mapToDomain
import payments.SubscriptionChecker
import remote.tasks.TodoRemoteDataSource

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
            remoteDataSource.addOrUpdateTodo(todo.mapToData(), targetUser)
        } else {
            localDataSource.addOrUpdateTodo(todo.mapToData())
        }
    }

    override suspend fun fetchTodoById(uid: UID, targetUser: UID): Flow<Todo?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val todoFlow = if (isSubscriber) {
            remoteDataSource.fetchTodoById(uid, targetUser)
        } else {
            localDataSource.fetchTodoById(uid)
        }

        return todoFlow.map { todoData ->
            todoData?.mapToDomain()
        }
    }

    override suspend fun fetchTodosByDate(date: Instant, targetUser: UID): Flow<List<Todo>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = date.startThisDay().toEpochMilliseconds()
        val timeEnd = date.endThisDay().toEpochMilliseconds()

        val todoListFlow = if (isSubscriber) {
            remoteDataSource.fetchTodosByTimeRange(timeStart, timeEnd, targetUser)
        } else {
            localDataSource.fetchTodosByTimeRange(timeStart, timeEnd)
        }

        return todoListFlow.map { todoListData ->
            todoListData.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchTodosByTimeRange(
        timeRange: TimeRange,
        targetUser: UID
    ): Flow<List<Todo>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = timeRange.from.toEpochMilliseconds()
        val timeEnd = timeRange.to.toEpochMilliseconds()

        val todoListFlow = if (isSubscriber) {
            remoteDataSource.fetchTodosByTimeRange(timeStart, timeEnd, targetUser)
        } else {
            localDataSource.fetchTodosByTimeRange(timeStart, timeEnd)
        }

        return todoListFlow.map { todoListData ->
            todoListData.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchOverdueTodos(
        currentDate: Instant,
        targetUser: UID
    ): Flow<List<Todo>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val date = currentDate.endThisDay().toEpochMilliseconds()

        val todoListFlow = if (isSubscriber) {
            remoteDataSource.fetchOverdueTodos(date, targetUser)
        } else {
            localDataSource.fetchOverdueTodos(date)
        }

        return todoListFlow.map { todoListData ->
            todoListData.map { it.mapToDomain() }
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
}