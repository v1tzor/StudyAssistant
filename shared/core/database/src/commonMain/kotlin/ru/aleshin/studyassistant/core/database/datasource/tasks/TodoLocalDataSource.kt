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

import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.datasource.tasks.TodoLocalDataSource.OfflineStorage
import ru.aleshin.studyassistant.core.database.datasource.tasks.TodoLocalDataSource.SyncStorage
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToEntity
import ru.aleshin.studyassistant.core.database.models.tasks.BaseTodoEntity
import ru.aleshin.studyassistant.core.database.utils.CombinedLocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalMultipleDocumentsCommands
import ru.aleshin.studyassistant.sqldelight.tasks.TodoQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
interface TodoLocalDataSource : CombinedLocalDataSource<BaseTodoEntity, OfflineStorage, SyncStorage> {

    interface Commands : LocalMultipleDocumentsCommands<BaseTodoEntity> {

        suspend fun fetchTodosByTimeRange(from: Long, to: Long): Flow<List<BaseTodoEntity>>
        suspend fun fetchActiveTodos(): Flow<List<BaseTodoEntity>>
        suspend fun fetchCompletedTodos(from: Long?, to: Long?): Flow<List<BaseTodoEntity>>
        suspend fun fetchOverdueTodos(currentDate: Long): Flow<List<BaseTodoEntity>>
        suspend fun fetchAllTodos(): Flow<List<BaseTodoEntity>>

        abstract class Abstract(
            isCacheSource: Boolean,
            protected val todoQueries: TodoQueries,
            protected val coroutineManager: CoroutineManager,
        ) : Commands {

            protected val coroutineContext: CoroutineContext
                get() = coroutineManager.backgroundDispatcher

            protected val isCacheData = if (isCacheSource) 1L else 0L

            override suspend fun addOrUpdateItem(item: BaseTodoEntity) {
                val uid = item.uid.ifEmpty { randomUUID() }
                val updatedItem = item.copy(uid = uid, isCacheData = isCacheData).mapToEntity()
                todoQueries.addOrUpdateTodo(updatedItem).await()
            }

            override suspend fun addOrUpdateItems(items: List<BaseTodoEntity>) {
                items.forEach { addOrUpdateItem(it) }
            }

            override suspend fun fetchItemById(id: String): Flow<BaseTodoEntity?> {
                val query = todoQueries.fetchTodoById(id, isCacheData)
                return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchItemsById(ids: List<String>): Flow<List<BaseTodoEntity>> {
                val query = todoQueries.fetchTodoByIds(ids, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchTodosByTimeRange(from: Long, to: Long): Flow<List<BaseTodoEntity>> {
                val query = todoQueries.fetchTodosByTimeRange(from, to, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchActiveTodos(): Flow<List<BaseTodoEntity>> {
                val query = todoQueries.fetchActiveTodos(isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchCompletedTodos(from: Long?, to: Long?): Flow<List<BaseTodoEntity>> {
                return if (from != null && to != null) {
                    val query = todoQueries.fetchCompletedTodosByTimeRange(from, to, isCacheData)
                    query.mapToListFlow(coroutineContext) { it.mapToBase() }
                } else {
                    val query = todoQueries.fetchCompletedTodos(isCacheData)
                    query.mapToListFlow(coroutineContext) { it.mapToBase() }
                }
            }

            override suspend fun fetchOverdueTodos(currentDate: Long): Flow<List<BaseTodoEntity>> {
                val query = todoQueries.fetchOverdueTodos(currentDate, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchAllTodos(): Flow<List<BaseTodoEntity>> {
                val query = todoQueries.fetchAllTodos(isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchAllMetadata(): List<MetadataModel> {
                val query = todoQueries.fetchEmptyTodos()
                return query.awaitAsList().map { entity ->
                    MetadataModel(entity.uid, entity.updated_at)
                }
            }

            override suspend fun deleteItemsById(ids: List<String>) {
                todoQueries.deleteTodosById(ids, isCacheData).await()
            }

            override suspend fun deleteAllItems() {
                todoQueries.deleteAllTodos(isCacheData).await()
            }
        }
    }

    interface OfflineStorage : LocalDataSource.OnlyOffline, Commands {

        class Base(
            todoQueries: TodoQueries,
            coroutineManager: CoroutineManager
        ) : OfflineStorage, Commands.Abstract(
            isCacheSource = false,
            todoQueries = todoQueries,
            coroutineManager = coroutineManager,
        )
    }

    interface SyncStorage : LocalDataSource.FullSynced.MultipleDocuments<BaseTodoEntity>, Commands {

        class Base(
            todoQueries: TodoQueries,
            coroutineManager: CoroutineManager
        ) : SyncStorage, Commands.Abstract(
            isCacheSource = true,
            todoQueries = todoQueries,
            coroutineManager = coroutineManager,
        )
    }

    class Base(
        private val offlineStorage: OfflineStorage,
        private val syncStorage: SyncStorage
    ) : TodoLocalDataSource {
        override fun offline() = offlineStorage
        override fun sync() = syncStorage
    }
}