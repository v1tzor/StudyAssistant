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

package ru.aleshin.studyassistant.core.data.utils.sync

import co.touchlab.kermit.Logger
import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.database.utils.BaseLocalEntity
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChange
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType.DELETE
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType.UPSERT
import ru.aleshin.studyassistant.core.domain.utils.sync.SourceSyncManager
import ru.aleshin.studyassistant.core.remote.utils.BaseMultipleRemotePojo
import ru.aleshin.studyassistant.core.remote.utils.BaseRemotePojo
import ru.aleshin.studyassistant.core.remote.utils.DatabaseEvent
import ru.aleshin.studyassistant.core.remote.utils.RemoteDataSource

/**
 * Custom SyncManager for StudyAssistant
 *
 * Interface representing a synchronization manager responsible for syncing local and remote data.
 *
 * Used when the app supports offline changes which are later synchronized with a remote database
 * once the device regains internet connectivity.
 *
 * @author Stanislav Aleshin on 22.07.2025.
 */
abstract class BaseSourceSyncManager(
    protected val changeQueueStorage: ChangeQueueStorage,
    protected val connectionManger: Konnection,
    protected val coroutineManager: CoroutineManager,
) : SourceSyncManager {

    protected var connectJob: Job? = null
    protected var taskJob: Job? = null
    protected val mainJob = SupervisorJob()

    protected val scope = CoroutineScope(mainJob + coroutineManager.backgroundDispatcher)

    /**
     * Pushes queued offline changes to the remote database.
     * Skips conflicting changes where server has a newer update timestamp.
     */
    abstract suspend fun pushOfflineChanges()

    /**
     * Reconciles the local database with the remote state.
     * - Deletes items locally that were removed remotely.
     * - Updates items locally that are outdated.
     * - Adds items that exist remotely but not locally.
     */
    abstract suspend fun updateLocalDatabase()

    /**
     * Subscribes to remote event stream and applies changes to local database
     * in real-time as the events occur on the server.
     */
    abstract suspend fun collectServerUpdates()

    /**
     * Starts two-directional synchronization between local and remote sources.
     * Local changes are pushed first, then local data is updated with remote state,
     * and finally live remote updates are collected and applied.
     */
    override fun startSourceSync() {
        connectJob?.cancel()
        connectJob = scope.launch {
            Logger.d("test2") { "$sourceSyncKey: startTwoDirectSync" }
            connectionManger.observeHasConnection().collect { hasConnection ->
                Logger.d("test2") { "$sourceSyncKey: startTwoDirectSync -> collect" }
                taskJob?.cancel()

                if (!hasConnection) return@collect

                taskJob = scope.launch {
                    pushOfflineChanges()
                    updateLocalDatabase()
                    collectServerUpdates()
                }
            }
        }.apply {
            invokeOnCompletion {
                Logger.e("test2") { "$sourceSyncKey: end startTwoDirectSync" }
            }
        }
    }

    /**
     * Stops all currently running sync operations.
     */
    override fun stopSourceSync() {
        mainJob.cancelChildren()
    }

    /**
     * Executes a synchronization task and deletes processed changes from queue on success.
     * If an InternetConnectionException is thrown, the task is skipped but not deleted.
     * Any other exception causes both a stack trace and removal of the failed changes.
     *
     * @param changes The list of offline changes to process.
     * @param work The suspend lambda to perform the actual sync operation.
     */
    protected suspend fun handleRemoteSyncResult(
        changes: List<OfflineChange>,
        work: suspend (List<OfflineChange>) -> Unit
    ) {
        try {
            work.invoke(changes)
            changeQueueStorage.deleteChangesByIds(changes.map { it.id })
        } catch (_: InternetConnectionException) {
            return
        } catch (exception: Exception) {
            exception.printStackTrace()
            changeQueueStorage.deleteChangesByIds(changes.map { it.id })
        }
    }

    /**
     * Implementation used when a user owns a list of documents.
     *
     * Examples:
     * - Sync todos
     * - Sync notes
     * - Sync schedules
     */
    abstract class MultipleDocuments<E : BaseLocalEntity, P : BaseMultipleRemotePojo>(
        private val localDataSource: LocalDataSource.FullSynced.MultipleDocuments<E>,
        private val remoteDataSource: RemoteDataSource.FullSynced.MultipleDocuments<P>,
        private val userSessionProvider: UserSessionProvider,
        private val mappers: MultipleSyncMapper<E, P>,
        changeQueueStorage: ChangeQueueStorage,
        connectionManger: Konnection,
        coroutineManager: CoroutineManager,
    ) : BaseSourceSyncManager(
        changeQueueStorage = changeQueueStorage,
        connectionManger = connectionManger,
        coroutineManager = coroutineManager,
    ) {

        override suspend fun clearSourceData() {
            changeQueueStorage.deleteAllSourceChanges(sourceSyncKey)
            localDataSource.deleteAllItems()
            mainJob.cancelChildren()
        }

        override suspend fun pushOfflineChanges() {
            val currentUser = userSessionProvider.getCurrentUserId()

            val changeQueue = changeQueueStorage.fetchAllSourceChanges(sourceSyncKey)
            if (changeQueue.isEmpty()) return

            val actualRemoteModels = remoteDataSource.fetchAllMetadata(currentUser).associate { model ->
                Pair(model.id, model.updatedAt)
            }
            Logger.i("test2") { "$sourceSyncKey: pushOfflineChanges: changeQueue -> $changeQueue" }

            val deleteChanges = changeQueue.filter { localChange ->
                val typeFilter = localChange.type == DELETE
                val updatedAtFilter = localChange.updatedAt >= (actualRemoteModels[localChange.documentId] ?: Long.MAX_VALUE)
                return@filter typeFilter && updatedAtFilter
            }
            val upsertChanges = changeQueue.filter { localChange ->
                val typeFilter = localChange.type == UPSERT
                val isNotDeleted = deleteChanges.none { it.documentId == localChange.documentId }
                val updatedAtFilter = localChange.updatedAt >= (actualRemoteModels[localChange.documentId] ?: Long.MIN_VALUE)
                return@filter typeFilter && isNotDeleted && updatedAtFilter
            }.distinctBy { localChange ->
                localChange.documentId
            }

            val plannedChangesIds = (deleteChanges + upsertChanges).map { it.id }
            val dirtyChanges = changeQueue.filter { !plannedChangesIds.contains(it.id) }
            changeQueueStorage.deleteChangesByIds(dirtyChanges.map { it.id })

            if (deleteChanges.isNotEmpty()) {
                handleRemoteSyncResult(deleteChanges) { changes ->
                    remoteDataSource.deleteItemsByIds(changes.map { it.documentId })
                }
                Logger.i("test2") { "$sourceSyncKey: pushOfflineChanges: deleteChanges -> $deleteChanges" }
            }

            if (upsertChanges.isNotEmpty()) {
                handleRemoteSyncResult(upsertChanges) { changes ->
                    val localModelsFlow = localDataSource.fetchItemsById(changes.map { it.documentId })
                    val upsertRemoteModels = localModelsFlow.first().map { localModel ->
                        mappers.localToRemote.invoke(localModel, currentUser)
                    }
                    remoteDataSource.addOrUpdateItems(upsertRemoteModels)
                }
                Logger.i("test2") { "$sourceSyncKey: pushOfflineChanges: upsertChanges -> $upsertChanges" }
            }
        }

        override suspend fun updateLocalDatabase() {
            val currentUser = userSessionProvider.getCurrentUserId()

            val localModels = localDataSource.fetchAllMetadata()
            val actualRemoteModels = remoteDataSource.fetchAllMetadata(currentUser).associate { model ->
                Pair(model.id, model.updatedAt)
            }

            val deletedModels = localModels.filter { it.id !in actualRemoteModels.keys }

            val updatedModels = localModels.filter { local ->
                local.updatedAt < (actualRemoteModels[local.id] ?: Long.MIN_VALUE)
            }
            val addedModels = actualRemoteModels.filterKeys { id ->
                localModels.none { it.id == id }
            }
            val upsertIds = updatedModels.map { it.id } + addedModels.keys

            if (deletedModels.isNotEmpty()) {
                localDataSource.deleteItemsById(deletedModels.map { it.id })
                Logger.i("test2") { "$sourceSyncKey: updateLocalDatabase: deleteModels -> $deletedModels" }
            }

            if (upsertIds.isNotEmpty()) {
                val upsertRemoteModels = remoteDataSource.fetchItemsByIds(upsertIds.toList()).first()
                localDataSource.addOrUpdateItems(upsertRemoteModels.map { mappers.remoteToLocal(it) })
                Logger.i("test2") { "$sourceSyncKey: updateLocalDatabase: upsertRemoteModels -> $upsertRemoteModels" }
            }
        }

        override suspend fun collectServerUpdates() {
            remoteDataSource.observeEvents().collect { event ->
                val localModel = localDataSource.fetchItemById(event.documentId).first()
                when (event) {
                    is DatabaseEvent.Create<P> -> {
                        if (localModel?.updatedAt == null || localModel.updatedAt < event.data.updatedAt) {
                            localDataSource.addOrUpdateItems(listOf(mappers.remoteToLocal(event.data)))
                            Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                        }
                    }
                    is DatabaseEvent.Delete<P> -> {
                        if (localModel != null) {
                            localDataSource.deleteItemsById(listOf(event.documentId))
                            Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                        }
                    }
                    is DatabaseEvent.Update<P> -> {
                        if (localModel?.updatedAt == null || localModel.updatedAt < event.data.updatedAt) {
                            localDataSource.addOrUpdateItems(listOf(mappers.remoteToLocal(event.data)))
                            Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                        }
                    }
                }
            }
        }
    }

    /**
     * Implementation used when a user owns exactly one document.
     *
     * Examples:
     * - Sync user settings
     * - Sync user profile
     */
    abstract class SingleDocument<E : BaseLocalEntity, P : BaseRemotePojo>(
        protected val localDataSource: LocalDataSource.FullSynced.SingleDocument<E>,
        protected val remoteDataSource: RemoteDataSource.FullSynced.SingleDocument<P>,
        protected val mappers: SingleSyncMapper<E, P>,
        changeQueueStorage: ChangeQueueStorage,
        connectionManger: Konnection,
        coroutineManager: CoroutineManager,
    ) : BaseSourceSyncManager(
        changeQueueStorage = changeQueueStorage,
        connectionManger = connectionManger,
        coroutineManager = coroutineManager,
    ) {

        override suspend fun clearSourceData() {
            changeQueueStorage.deleteAllSourceChanges(sourceSyncKey)
            localDataSource.deleteItem()
            mainJob.cancelChildren()
        }

        override suspend fun pushOfflineChanges() {
            val changeQueue = changeQueueStorage.fetchAllSourceChanges(sourceSyncKey)
            if (changeQueue.isEmpty()) return

            val actualRemoteModel = remoteDataSource.fetchMetadata()
            Logger.i("test2") { "$sourceSyncKey: pushOfflineChanges: changeQueue -> $changeQueue" }

            val isDeleted = changeQueue.any { localChange ->
                val typeFilter = localChange.type == DELETE
                val updatedAtFilter = localChange.updatedAt >= (actualRemoteModel?.updatedAt ?: Long.MAX_VALUE)
                return@any typeFilter && updatedAtFilter
            }
            val isUpserted = changeQueue.any { localChange ->
                val typeFilter = localChange.type == UPSERT
                val updatedAtFilter = localChange.updatedAt >= (actualRemoteModel?.updatedAt ?: Long.MIN_VALUE)
                return@any typeFilter && !isDeleted && updatedAtFilter
            }

            if (isDeleted) {
                handleRemoteSyncResult(changeQueue) {
                    remoteDataSource.deleteItem()
                }
                Logger.i("test2") { "$sourceSyncKey: pushOfflineChanges: deleteChange" }
            } else if (isUpserted) {
                handleRemoteSyncResult(changeQueue) {
                    val localModel = localDataSource.fetchItem().first()
                    val upsertRemoteModels = mappers.localToRemote.invoke(checkNotNull(localModel))
                    remoteDataSource.addOrUpdateItem(upsertRemoteModels)
                }
                Logger.i("test2") { "$sourceSyncKey: pushOfflineChanges: upsertChange" }
            }
        }

        override suspend fun updateLocalDatabase() {
            val currentLocalModel = localDataSource.fetchMetadata()
            val actualRemoteModel = remoteDataSource.fetchMetadata()

            if (actualRemoteModel == null && currentLocalModel != null) {
                localDataSource.deleteItem()
                Logger.i("test2") { "$sourceSyncKey: updateLocalDatabase: deleteModel" }
            } else if (actualRemoteModel != null) {
                if (currentLocalModel == null || actualRemoteModel.updatedAt > currentLocalModel.updatedAt) {
                    val upsertRemoteModel = remoteDataSource.fetchItem().first() ?: return
                    localDataSource.addOrUpdateItem(mappers.remoteToLocal(upsertRemoteModel))
                    Logger.i("test2") { "$sourceSyncKey: updateLocalDatabase: upsertRemoteModel" }
                }
            }
        }

        override suspend fun collectServerUpdates() {
            remoteDataSource.observeEvents().collect { event ->
                val localModel = localDataSource.fetchMetadata()
                when (event) {
                    is DatabaseEvent.Create<P> -> {
                        if (localModel?.updatedAt == null || localModel.updatedAt < event.data.updatedAt) {
                            localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                            Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                        }
                    }
                    is DatabaseEvent.Delete<P> -> {
                        if (localModel != null) {
                            localDataSource.deleteItem()
                            Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                        }
                    }
                    is DatabaseEvent.Update<P> -> {
                        if (localModel?.updatedAt == null || localModel.updatedAt < event.data.updatedAt) {
                            localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                            Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                        }
                    }
                }
            }
        }
    }
}