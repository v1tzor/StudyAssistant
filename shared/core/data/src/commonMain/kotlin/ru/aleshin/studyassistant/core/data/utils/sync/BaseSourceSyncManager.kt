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

import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
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
    abstract suspend fun uploadOfflineChanges()

    /**
     * Reconciles the local database with the remote state.
     * - Deletes items locally that were removed remotely.
     * - Updates items locally that are outdated.
     * - Adds items that exist remotely but not locally.
     */
    abstract suspend fun syncLocalDatabase()

    /**
     * Subscribes to remote event stream and applies changes to local database
     * in real-time as the events occur on the server.
     */
    abstract suspend fun collectOnlineChanges()

    /**
     * Starts two-directional synchronization between the local and remote data sources.
     *
     * This function performs synchronization in three phases:
     * 1. Attempts to upload any offline changes to the remote source.
     * 2. Pulls the current remote state and updates the local database.
     * 3. Subscribes to live remote updates and applies them locally in real time.
     *
     * If the initial sync (upload + download) fails due to network issues or other exceptions,
     * the process will resume when connectivity is restored.
     *
     * The function monitors network connectivity via [connectionManger] and automatically restarts
     * sync attempts when the device goes back online.
     *
     * This method is safe to call multiple times — it will cancel any ongoing sync before restarting.
     */

    override suspend fun startSourceSync() {
        var isFirstSync: Boolean
        try {
            uploadOfflineChanges()
            syncLocalDatabase()
            isFirstSync = true
        } catch (_: Exception) {
            isFirstSync = false
        }

        connectJob?.cancel()
        connectJob = scope.launch(SupervisorJob()) {
            connectionManger.observeHasConnection().collect { hasConnection ->
                taskJob?.cancel()
                taskJob = null

                if (!hasConnection) {
                    isFirstSync = false
                    return@collect
                }

                taskJob = scope.launch {
                    try {
                        if (!isFirstSync) {
                            uploadOfflineChanges()
                            syncLocalDatabase()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        collectOnlineChanges()
                    }
                }
            }
        }
    }

    /**
     * Single two-directional synchronization between local and remote sources.
     * Local changes are pushed first, then local data is updated with remote state
     *
     * @return [Boolean] Success sync status
     */
    override suspend fun singleSyncRound(): Boolean {
        try {
            uploadOfflineChanges()
            syncLocalDatabase()
        } catch (_: Exception) {
            return false
        }
        return true
    }

    /**
     * Stops all currently running sync operations.
     */
    override fun stopSourceSync() {
        mainJob.cancelChildren()
        taskJob = null
        connectJob = null
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
        } catch (_: IOException) {
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
            stopSourceSync()
            changeQueueStorage.deleteAllSourceChanges(sourceSyncKey)
            localDataSource.deleteAllItems()
        }

        override suspend fun uploadOfflineChanges() {
            val currentUser = userSessionProvider.getCurrentUserId()

            val changeQueue = changeQueueStorage.fetchAllSourceChanges(sourceSyncKey)
            if (changeQueue.isEmpty()) return

            val actualRemoteModels = remoteDataSource.fetchAllMetadata(
                targetUser = currentUser,
                ids = changeQueue.map { it.documentId }.distinct()
            ).associate { model ->
                Pair(model.id, model.updatedAt)
            }

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
                    remoteDataSource.deleteItemsByIds(changes.map { it.documentId }, false)
                }
            }

            if (upsertChanges.isNotEmpty()) {
                handleRemoteSyncResult(upsertChanges) { changes ->
                    val localModelsFlow = localDataSource.fetchItemsById(changes.map { it.documentId })
                    val upsertRemoteModels = localModelsFlow.first().map { localModel ->
                        mappers.localToRemote.invoke(localModel, currentUser)
                    }
                    remoteDataSource.addOrUpdateItems(upsertRemoteModels, false)
                }
            }
            remoteDataSource.sendBatchCallback()
        }

        override suspend fun syncLocalDatabase() {
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
            }

            if (upsertIds.isNotEmpty()) {
                val upsertRemoteModels = remoteDataSource.fetchOnceItemsByIds(upsertIds.toList())
                localDataSource.addOrUpdateItems(upsertRemoteModels.map { mappers.remoteToLocal(it) })
            }
        }

        override suspend fun collectOnlineChanges() {
            remoteDataSource.observeEvents().collect { event ->
                val localModel = localDataSource.fetchItemById(event.documentId).first()
                when (event) {
                    is DatabaseEvent.Create<P> -> {
                        if (localModel?.updatedAt == null || localModel.updatedAt < event.data.updatedAt) {
                            localDataSource.addOrUpdateItems(listOf(mappers.remoteToLocal(event.data)))
                        }
                    }
                    is DatabaseEvent.Delete<P> -> {
                        if (localModel != null) {
                            localDataSource.deleteItemsById(listOf(event.documentId))
                        }
                    }
                    is DatabaseEvent.Update<P> -> {
                        if (localModel?.updatedAt == null || localModel.updatedAt < event.data.updatedAt) {
                            localDataSource.addOrUpdateItems(listOf(mappers.remoteToLocal(event.data)))
                        }
                    }
                    is DatabaseEvent.BatchUpdate<*> -> {
                        syncLocalDatabase()
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
            stopSourceSync()
            changeQueueStorage.deleteAllSourceChanges(sourceSyncKey)
            localDataSource.deleteItem()
        }

        override suspend fun uploadOfflineChanges() {
            val changeQueue = changeQueueStorage.fetchAllSourceChanges(sourceSyncKey)
            if (changeQueue.isEmpty()) return

            val actualRemoteModel = remoteDataSource.fetchMetadata()

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
            } else if (isUpserted) {
                handleRemoteSyncResult(changeQueue) {
                    val localModel = localDataSource.fetchItem().first()
                    val upsertRemoteModels = mappers.localToRemote.invoke(checkNotNull(localModel))
                    remoteDataSource.addOrUpdateItem(upsertRemoteModels)
                }
            }
        }

        override suspend fun syncLocalDatabase() {
            val currentLocalModel = localDataSource.fetchMetadata()
            val actualRemoteModel = remoteDataSource.fetchMetadata()

            if (actualRemoteModel == null && currentLocalModel != null) {
                localDataSource.deleteItem()
            } else if (actualRemoteModel != null) {
                if (currentLocalModel == null || actualRemoteModel.updatedAt > currentLocalModel.updatedAt) {
                    val upsertRemoteModel = remoteDataSource.fetchOnceItem() ?: return
                    localDataSource.addOrUpdateItem(mappers.remoteToLocal(upsertRemoteModel))
                }
            }
        }

        override suspend fun collectOnlineChanges() {
            remoteDataSource.observeEvents().collect { event ->
                val localModel = localDataSource.fetchMetadata()
                when (event) {
                    is DatabaseEvent.Create<P> -> {
                        if (localModel?.updatedAt == null || localModel.updatedAt < event.data.updatedAt) {
                            localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                        }
                    }
                    is DatabaseEvent.Delete<P> -> {
                        if (localModel != null) {
                            localDataSource.deleteItem()
                        }
                    }
                    is DatabaseEvent.Update<P> -> {
                        if (localModel?.updatedAt == null || localModel.updatedAt < event.data.updatedAt) {
                            localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                        }
                    }
                    is DatabaseEvent.BatchUpdate<*> -> syncLocalDatabase()
                }
            }
        }
    }
}